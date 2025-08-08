package net.ccbluex.liquidbounce.file.configs

import com.google.gson.JsonArray
import com.google.gson.JsonSyntaxException
import me.liuli.elixir.account.CrackedAccount
import me.liuli.elixir.account.MinecraftAccount
import me.liuli.elixir.account.MojangAccount
import me.liuli.elixir.manage.AccountSerializer.fromJson
import me.liuli.elixir.manage.AccountSerializer.toJson
import net.ccbluex.liquidbounce.file.FileConfig
import net.ccbluex.liquidbounce.file.FileManager.PRETTY_GSON
import net.ccbluex.liquidbounce.utils.io.readJson
import java.io.*

class AccountsConfig(file: File) : FileConfig(file) {

    val accounts = mutableListOf<MinecraftAccount>()

        @Throws(IOException::class)
    override fun loadConfig() {
        clearAccounts()

        val json = file.readJson() as? JsonArray ?: return

        for (accountElement in json) {
            val accountObject = accountElement.asJsonObject
            try {
                // Import Elixir account format
                accounts += fromJson(accountElement.asJsonObject)
            } catch (e: JsonSyntaxException) {
                // Import old account format
                val name = accountObject["name"]
                val password = accountObject["password"]
                val inGameName = accountObject["inGameName"]
                if (inGameName.isJsonNull && password.isJsonNull) {
                    val mojangAccount = MojangAccount()
                    mojangAccount.email = name.asString
                    mojangAccount.name = inGameName.asString
                    mojangAccount.password = password.asString
                    accounts += mojangAccount
                } else {
                    val crackedAccount = CrackedAccount()
                    crackedAccount.name = name.asString
                    accounts += crackedAccount
                }
            } catch (e: IllegalStateException) {
                val name = accountObject["name"]
                val password = accountObject["password"]
                val inGameName = accountObject["inGameName"]
                if (inGameName.isJsonNull && password.isJsonNull) {
                    val mojangAccount = MojangAccount()
                    mojangAccount.email = name.asString
                    mojangAccount.name = inGameName.asString
                    mojangAccount.password = password.asString
                    accounts += mojangAccount
                } else {
                    val crackedAccount = CrackedAccount()
                    crackedAccount.name = name.asString
                    accounts += crackedAccount
                }
            }
        }
    }

        @Throws(IOException::class)
    override fun saveConfig() {
        val jsonArray = JsonArray()

        for (minecraftAccount in accounts)
            jsonArray.add(toJson(minecraftAccount))

        file.writeText(PRETTY_GSON.toJson(jsonArray))
    }

        fun addCrackedAccount(name: String) {
        val crackedAccount = CrackedAccount()
        crackedAccount.name = name

        if (!accountExists(crackedAccount)) accounts += crackedAccount
    }

        fun addMojangAccount(name: String, password: String) {
        val mojangAccount = MojangAccount()
        mojangAccount.name = name
        mojangAccount.password = password

        if (!accountExists(mojangAccount)) accounts += mojangAccount
    }

        fun addAccount(account: MinecraftAccount) = accounts.add(account)

        fun removeAccount(selectedSlot: Int) = accounts.removeAt(selectedSlot)

        fun removeAccount(account: MinecraftAccount) = accounts.remove(account)

        fun accountExists(newAccount: MinecraftAccount) =
        accounts.any { it.javaClass == newAccount.javaClass && it.name == newAccount.name }

        fun clearAccounts() = accounts.clear()
}