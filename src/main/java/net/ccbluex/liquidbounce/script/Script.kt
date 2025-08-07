package net.ccbluex.liquidbounce.script

import jdk.internal.dynalink.beans.StaticClass
import jdk.nashorn.api.scripting.JSObject
import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import jdk.nashorn.api.scripting.ScriptUtils
import net.ccbluex.liquidbounce.LiquidBounce.commandManager
import net.ccbluex.liquidbounce.LiquidBounce.moduleManager
import net.ccbluex.liquidbounce.LiquidBounce.scriptManager
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.script.ScriptManager.scriptsFolder
import net.ccbluex.liquidbounce.script.api.ScriptCommand
import net.ccbluex.liquidbounce.script.api.ScriptModule
import net.ccbluex.liquidbounce.script.api.ScriptTab
import net.ccbluex.liquidbounce.script.api.global.Chat
import net.ccbluex.liquidbounce.script.api.global.Item
import net.ccbluex.liquidbounce.script.api.global.Setting
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import java.io.File
import java.util.function.Function
import javax.script.ScriptEngine

class Script(val scriptFile: File) : MinecraftInstance {

    private val scriptEngine: ScriptEngine
    private val scriptText = scriptFile.readText()

    // Script information
    lateinit var scriptName: String
    lateinit var scriptVersion: String
    lateinit var scriptAuthors: Array<String>

    private var state = false

    private val events = mutableMapOf<String, JSObject>()

    private val registeredModules = mutableListOf<Module>()
    private val registeredCommands = mutableListOf<Command>()

    init {
        val engineFlags = getMagicComment("engine_flags")?.split(',')?.toTypedArray() ?: emptyArray()
        scriptEngine = NashornScriptEngineFactory().getScriptEngine(*engineFlags)

        // Global classes
        scriptEngine.run {
            put("Chat", StaticClass.forClass(Chat::class.java))
            put("Setting", StaticClass.forClass(Setting::class.java))
            put("Item", StaticClass.forClass(Item::class.java))

            // Global instances
            put("mc", mc)

            put("moduleManager", moduleManager)
            put("commandManager", commandManager)
            put("scriptManager", scriptManager)

            // Global functions
            put("registerScript", RegisterScript())
        }
    }

    fun initScript() {
        scriptEngine.eval(scriptText)

        callEvent("load")

        LOGGER.info("[ScriptAPI] Successfully loaded script '${scriptFile.name}'.")
    }

    @Suppress("UNCHECKED_CAST")
    inner class RegisterScript : Function<JSObject, Script> {
                override fun apply(scriptObject: JSObject): Script {
            scriptName = scriptObject.getMember("name") as String
            scriptVersion = scriptObject.getMember("version") as String
            scriptAuthors = ScriptUtils.convert(scriptObject.getMember("authors"), Array<String>::class.java) as Array<String>

            return this@Script
        }
    }

        @Suppress("unused")
    fun registerModule(moduleObject: JSObject, callback: JSObject) {
        val name = moduleObject.getMember("name") as String
        val description = moduleObject.getMember("description") as String
        val categoryString = moduleObject.getMember("category") as String
        val category = Category.entries.find {
            it.displayName.equals(categoryString, true)
        } ?: Category.FUN


        val module = ScriptModule(name, category, description, moduleObject)
        moduleManager.registerModule(module)
        registeredModules += module
        callback.call(moduleObject, module)
    }

        @Suppress("unused")
    fun registerCommand(commandObject: JSObject, callback: JSObject) {
        val command = ScriptCommand(commandObject)
        commandManager.registerCommand(command)
        registeredCommands += command
        callback.call(commandObject, command)
    }

        @Suppress("unused")
    fun registerTab(tabObject: JSObject) {
        ScriptTab(tabObject)
    }

        private fun getMagicComment(name: String): String? {
        val magicPrefix = "///"

        scriptText.lineSequence().forEach {
            if (!it.startsWith(magicPrefix)) return null

            val commentData = it.subSequence(magicPrefix.length, it.length).split("=", limit = 2)

            if (commentData.first().trim() == name) {
                return commentData.last().trim()
            }
        }

        return null
    }

        fun on(eventName: String, handler: JSObject) {
        events[eventName] = handler
    }

        fun onEnable() {
        if (state) return

        callEvent("enable")
        state = true
    }

        fun onDisable() {
        if (!state) return

        registeredModules.forEach { moduleManager.unregisterModule(it) }
        registeredCommands.forEach { commandManager.unregisterCommand(it) }

        callEvent("disable")
        state = false
    }

        fun import(scriptFile: String) {
        val scriptText = File(scriptsFolder, scriptFile).readText()

        scriptEngine.eval(scriptText)
    }

        private fun callEvent(eventName: String) {
        try {
            events[eventName]?.call(null)
        } catch (throwable: Throwable) {
            LOGGER.error("[ScriptAPI] Exception in script '$scriptName'!", throwable)
        }
    }
}