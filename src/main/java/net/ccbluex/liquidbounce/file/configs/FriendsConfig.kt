package net.ccbluex.liquidbounce.file.configs

import com.google.gson.*
import net.ccbluex.liquidbounce.file.FileConfig
import net.ccbluex.liquidbounce.utils.io.decode
import net.ccbluex.liquidbounce.utils.io.readJson
import net.ccbluex.liquidbounce.utils.io.writeJson
import java.io.*

class FriendsConfig(file: File) : FileConfig(file) {
    val friends = mutableListOf<Friend>()

        @Throws(IOException::class)
    override fun loadConfig() {
        clearFriends()
        file.readJson().decode<Array<Friend>>().toCollection(friends)
    }

        @Throws(IOException::class)
    override fun saveConfig() = file.writeJson(friends)

        fun addFriend(playerName: String, alias: String = playerName): Boolean {
        if (isFriend(playerName)) return false

        friends += Friend(playerName, alias)
        return true
    }

        fun removeFriend(playerName: String) = friends.removeIf { it.playerName == playerName }

        fun isFriend(playerName: String) = friends.any { it.playerName == playerName }

        fun clearFriends() = friends.clear()

        data class Friend(val playerName: String, val alias: String)
}