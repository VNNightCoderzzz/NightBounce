package net.ccbluex.liquidbounce.script

import net.ccbluex.liquidbounce.file.FileManager.dir
import net.ccbluex.liquidbounce.script.remapper.Remapper
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import java.io.File
import java.io.FileFilter

private val scripts = mutableListOf<Script>()

object ScriptManager : List<Script> by scripts {

    val scriptsFolder = File(dir, "scripts")

    private val SCRIPT_FILE_FILTER = FileFilter {
        it.extension.lowercase() == "js"
    }

        val availableScriptFiles: Array<File>
        get() = scriptsFolder.listFiles(SCRIPT_FILE_FILTER) ?: emptyArray()

        fun loadScripts() {
        if (!scriptsFolder.exists())
            scriptsFolder.mkdir()

        availableScriptFiles.forEach(::loadScript)
    }

        fun unloadScripts() = scripts.clear()

        fun loadScript(scriptFile: File) {
        try {
            if (!Remapper.mappingsLoaded) {
                error("The mappings were not loaded, re-start and check your internet connection.")
            }

            val script = Script(scriptFile)
            script.initScript()
            scripts += script
        } catch (t: Throwable) {
            LOGGER.error("[ScriptAPI] Failed to load script '${scriptFile.name}'.", t)
        }
    }

        fun enableScripts() = scripts.forEach { it.onEnable() }

        fun disableScripts() = scripts.forEach { it.onDisable() }

        fun importScript(file: File) {
        val scriptFile = File(scriptsFolder, file.name)
        file.copyTo(scriptFile)

        loadScript(scriptFile)
        LOGGER.info("[ScriptAPI] Successfully imported script '${scriptFile.name}'.")
    }

        fun deleteScript(script: Script) {
        script.onDisable()
        scripts.remove(script)
        script.scriptFile.delete()

        LOGGER.info("[ScriptAPI] Successfully deleted script '${script.scriptFile.name}'.")
    }

        fun reloadScripts() {
        disableScripts()
        unloadScripts()
        loadScripts()
        enableScripts()

        LOGGER.info("[ScriptAPI] Successfully reloaded scripts.")
    }
}