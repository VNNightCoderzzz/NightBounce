package net.ccbluex.liquidbounce.utils.login

import net.ccbluex.liquidbounce.utils.io.HttpClient
import net.ccbluex.liquidbounce.utils.io.get
import net.ccbluex.liquidbounce.utils.io.jsonBody

object UserUtils {

    private val uuidCache = hashMapOf<String, String>()
    private val usernameCache = hashMapOf<String, String>()

        fun isValidTokenOffline(token: String) = token.length >= 32

    fun getUsername(uuid: String): String? {
        uuidCache[uuid]?.let { return it }

        return HttpClient.get(
            "https://api.minecraftservices.com/minecraft/profile/lookup/$uuid"
        ).jsonBody<Profile>()?.name?.also {
            usernameCache[uuid] = it
        }
    }

        fun getUUID(username: String): String? {
        usernameCache[username]?.let { return it }

        return HttpClient.get(
            "https://api.minecraftservices.com/minecraft/profile/lookup/name/$username"
        ).jsonBody<Profile>()?.id?.also {
            usernameCache[username] = it
        }
    }
}

private class Profile(val id: String, val name: String)