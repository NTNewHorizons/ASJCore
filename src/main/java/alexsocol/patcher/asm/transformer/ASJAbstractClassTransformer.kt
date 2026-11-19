package alexsocol.patcher.asm.transformer

import gloomyfolken.hooklib.asm.HookLogger
import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.*
import org.objectweb.asm.tree.ClassNode

abstract class ASJAbstractClassTransformer: IClassTransformer {
	
	/**
	 * State of the transform() call currently running on this thread.
	 *
	 * Transformer instances are shared, and transform() can run on several threads at once
	 * (e.g. ForgeMultipart's ASMMixinCompiler invokes LaunchClassLoader.runTransformers outside
	 * the class loading lock while another thread is loading classes).
	 * Keeping this state in plain fields let one call return the bytes of another class, which
	 * then failed with "NoClassDefFoundError: ... (wrong name: ...)".
	 */
	private class Frame(var transformedName: String, var basicClass: ByteArray)
	
	private val frame = ThreadLocal<Frame>()
	
	protected var transformedName: String
		get() = frame.get()?.transformedName ?: ""
		set(value) { currentFrame().transformedName = value }
	
	protected var basicClass: ByteArray
		get() = frame.get()?.basicClass ?: byteArrayOf()
		set(value) { currentFrame().basicClass = value }
	
	protected open val logger = HookLogger.Log4JLogger("ASJCore")
	
	private fun currentFrame() = frame.get() ?: throw IllegalStateException("ASJ transformer state used outside of transform()")
	
	final override fun transform(name: String?, transformedName: String, basicClass: ByteArray?): ByteArray? {
		if (basicClass == null || basicClass.isEmpty()) return basicClass
		
		val outer = frame.get()
		frame.set(Frame(transformedName, basicClass))
		try {
			return transform(transformedName, basicClass)
		} finally {
			if (outer == null) frame.remove() else frame.set(outer)
		}
	}
	
	abstract fun transform(transformedName: String, basicClass: ByteArray): ByteArray
	
	protected inline fun core(acceptFlags: Int = ClassReader.EXPAND_FRAMES, cwFlags: Int = ClassWriter.COMPUTE_MAXS, lambda: (ClassVisitor) -> ClassVisitor): ByteArray {
		logger.debug("Transforming $transformedName")
		val cr = ClassReader(basicClass)
		val cw = ClassWriter(cwFlags)
		
		val transformer = lambda(cw)
		
		cr.accept(transformer, acceptFlags)
		return cw.toByteArray()
	}
	
	protected inline fun tree(acceptFlags: Int = ClassReader.SKIP_FRAMES, cwFlags: Int = ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES, lambda: (ClassNode) -> Unit): ByteArray {
		logger.debug("Transforming $transformedName")
		val cr = ClassReader(basicClass)
		val cw = ClassWriter(cwFlags)
		val cn = ClassNode()
		cr.accept(cn, acceptFlags)
		
		lambda(cn)
		
		cn.accept(cw)
		return cw.toByteArray()
	}
}