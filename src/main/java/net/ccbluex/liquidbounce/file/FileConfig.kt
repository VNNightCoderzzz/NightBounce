package net.ccbluex.liquidbounce.file

import java.io.File
import java.io.IOException

abstract class FileConfig(val file: File) {

        @Throws(IOException::class)
    abstract fun loadConfig()

        @Throws(IOException::class)
    abstract fun saveConfig()

        @Throws(IOException::class)
    fun createConfig() = file.createNewFile()

        fun hasConfig() = file.exists() && file.length() > 0

        open fun loadDefault() {}
}