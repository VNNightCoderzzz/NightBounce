package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.LiquidBounce.CLIENT_NAME
import net.ccbluex.liquidbounce.utils.client.ClientUtils
import net.ccbluex.liquidbounce.utils.io.flipSafely
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.Display
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.*
import javax.imageio.ImageIO

@SideOnly(Side.CLIENT)
object IconUtils {

    @JvmStatic
fun initLwjglIcon(): Boolean {
    val icons = favicon?.filterNotNull()?.toTypedArray()

    if (icons.isNullOrEmpty()) {
        ClientUtils.LOGGER.warn("❌ Không load được bất kỳ icon nào!")
        return false
    }

    return try {
        Display.setIcon(icons)
        true
    } catch (e: Exception) {
        ClientUtils.LOGGER.warn("❌ Display.setIcon failed!", e)
        false
    }
}

val favicon by lazy {
    IconUtils::class.java.runCatching {
        val paths = listOf(
            "/assets/minecraft/rinbounce/logo_16x16.png",
            "/assets/minecraft/rinbounce/logo_32x32.png",
            "/assets/minecraft/rinbounce/logo_64x64.png"
        )

        paths.mapIndexed { index, path ->
            val stream = getResourceAsStream(path)
            val buffer = readImageToBuffer(stream)
            if (buffer == null)
                ClientUtils.LOGGER.warn("❌ Icon $index ($path) = NULL")
            buffer
        }.toTypedArray()
    }.onFailure {
        ClientUtils.LOGGER.warn("Failed to load icons", it)
    }.getOrNull()
}


    @Throws(IOException::class)
    private fun readImageToBuffer(imageStream: InputStream?): ByteBuffer? {
        val bufferedImage = imageStream?.let(ImageIO::read) ?: return null
        val rgb = bufferedImage.getRGB(0, 0, bufferedImage.width, bufferedImage.height, null, 0, bufferedImage.width)
        val byteBuffer = ByteBuffer.allocate(4 * rgb.size)

        for (i in rgb)
            byteBuffer.putInt(i shl 8 or (i ushr 24 and 255))

        byteBuffer.flipSafely()
        return byteBuffer
    }
}
