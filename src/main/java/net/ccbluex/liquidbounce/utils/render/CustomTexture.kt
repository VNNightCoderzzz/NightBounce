package net.ccbluex.liquidbounce.utils.render

import net.minecraft.client.renderer.texture.TextureUtil
import org.lwjgl.opengl.GL11.glDeleteTextures
import java.awt.image.BufferedImage

class CustomTexture(private val image: BufferedImage) {
    private var unloaded = false
    var textureId = -1
        private set

                get() {
            check(!unloaded) { "Texture unloaded" }

            if (field == -1)
                field = TextureUtil.uploadTextureImageAllocate(TextureUtil.glGenTextures(), image, true, true)

            return field
        }

    fun unload() {
        if (!unloaded) {
            glDeleteTextures(textureId)
            unloaded = true
        }
    }

    protected fun finalize() = unload()
}
