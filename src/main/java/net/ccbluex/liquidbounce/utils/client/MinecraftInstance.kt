package net.ccbluex.liquidbounce.utils.client

import net.minecraft.client.Minecraft
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.util.ResourceLocation

interface MinecraftInstance {
    val mc: Minecraft
        get() = Companion.mc

    companion object {
        @JvmField
        val mc: Minecraft = Minecraft.getMinecraft()
    }
}

fun Minecraft.playSound(
    resourceLocation: ResourceLocation,
    pitch: Float = 1.0f,
) = synchronized(this.soundHandler) {
    this.soundHandler.playSound(PositionedSoundRecord.create(resourceLocation, pitch))
}

fun String.asResourceLocation() = ResourceLocation(this)
