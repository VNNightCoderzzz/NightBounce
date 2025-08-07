package net.ccbluex.liquidbounce.script.remapper.injection.utils

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode

object ClassUtils {

        fun toClassNode(bytes : ByteArray) : ClassNode {
        val classReader = ClassReader(bytes)
        val classNode = ClassNode()
        classReader.accept(classNode, 0)

        return classNode
    }

        fun toBytes(classNode : ClassNode) : ByteArray {
        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
        classNode.accept(classWriter)

        return classWriter.toByteArray()
    }
}