package net.ccbluex.liquidbounce.lang

import net.ccbluex.liquidbounce.file.configs.models.ClientConfiguration.overrideLanguage
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.io.decodeJson

fun translationMenu(key: String, vararg args: Any) = LanguageManager.getTranslation("menu.$key", *args)
fun translationButton(key: String, vararg args: Any) = LanguageManager.getTranslation("button.$key", *args)
fun translationText(key: String, vararg args: Any) = LanguageManager.getTranslation("text.$key", *args)
fun translation(key: String, vararg args: Any) = LanguageManager.getTranslation(key, *args)

object LanguageManager : MinecraftInstance {

    // Current language
    private val language: String
        get() = overrideLanguage.ifBlank { mc.gameSettings.language }

    // Common language
    private const val COMMON_UNDERSTOOD_LANGUAGE = "en_US"

    // List of all languages
    val knownLanguages = arrayOf(
        "en_US",
        "vi_VN",
        "pt_BR",
        "pt_PT",
        "zh_CN",
        "zh_TW",
        "bg_BG",
        "ru_RU"
    )
    private val languageMap = hashMapOf<String, Language>()

        fun loadLanguages() {
        for (language in knownLanguages) {
            runCatching {
                languageMap[language] = javaClass.getResourceAsStream("/assets/minecraft/rinbounce/lang/$language.json")!!
                    .bufferedReader().use { it.decodeJson() }
            }.onSuccess {
                LOGGER.info("Loaded language $language")
            }.onFailure {
                LOGGER.error("Failed to load language $language", it)
            }
        }
    }

        fun getTranslation(key: String, vararg args: Any)
        = languageMap[language]?.getTranslation(key, args = args)
        ?: languageMap[COMMON_UNDERSTOOD_LANGUAGE]?.getTranslation(key, args = args)
        ?: key
    
}

class Language(val locale: String, val contributors: List<String>, val translations: Map<String, String>) {

    fun getTranslation(key: String, vararg args: Any) = translations[key]?.format(args = args)

    override fun toString() = locale


}
