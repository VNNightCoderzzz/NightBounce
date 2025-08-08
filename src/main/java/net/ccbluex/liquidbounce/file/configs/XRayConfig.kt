package net.ccbluex.liquidbounce.file.configs

import com.google.gson.JsonArray
import net.ccbluex.liquidbounce.features.module.modules.render.XRay
import net.ccbluex.liquidbounce.file.FileConfig
import net.ccbluex.liquidbounce.utils.block.blockById
import net.ccbluex.liquidbounce.utils.block.id
import net.ccbluex.liquidbounce.utils.io.readJson
import net.ccbluex.liquidbounce.utils.io.writeJson
import net.minecraft.init.Blocks
import java.io.*

class XRayConfig(file: File) : FileConfig(file) {

        @Throws(IOException::class)
    override fun loadConfig() {
        val json = file.readJson() as? JsonArray ?: return

        XRay.xrayBlocks.clear()

        json.mapNotNullTo(XRay.xrayBlocks) {
            it.asInt.blockById.takeIf { b -> b != Blocks.air }
        }
    }

        @Throws(IOException::class)
    override fun saveConfig() {
        file.writeJson(XRay.xrayBlocks.map { it.id }.sorted())
    }
}