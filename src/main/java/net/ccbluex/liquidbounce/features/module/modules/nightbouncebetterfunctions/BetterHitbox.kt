package net.ccbluex.liquidbounce.features.module.modules.nightbouncebetterfunctions

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot.isBot
import net.ccbluex.liquidbounce.features.module.modules.misc.Teams
import net.ccbluex.liquidbounce.utils.extensions.*
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.passive.*
import net.minecraft.entity.player.EntityPlayer

object BetterHitBox : Module("BetterHitBox", Category.NIGHTBOUNCEBETTERFUNCTIONS) {

    // Players
    private val targetPlayers by boolean("TargetPlayers", true)
    private val playerSize by float("PlayerSize", 0.4F, 0F..1F) { targetPlayers }
    private val friendSize by float("FriendSize", 0.4F, 0F..1F) { targetPlayers }
    private val teamMateSize by float("TeamMateSize", 0.4F, 0F..1F) { targetPlayers }
    private val botSize by float("BotSize", 0.4F, 0F..1F) { targetPlayers }

    // Mobs
    private val targetHostileMobs by boolean("TargetHostileMobs", false)
    private val hostileMobSize by float("HostileMobSize", 0.4F, 0F..1F) { targetHostileMobs }

    private val targetPassiveMobs by boolean("TargetPassiveMobs", false)
    private val passiveMobSize by float("PassiveMobSize", 0.4F, 0F..1F) { targetPassiveMobs }

    private val targetBosses by boolean("TargetBosses", false)
    private val bossSize by float("BossSize", 0.6F, 0F..2F) { targetBosses }

    fun determineSize(entity: Entity): Float {
        return when (entity) {
            is EntityPlayer -> {
                if (!targetPlayers || entity.isSpectator) return 0F

                when {
                    isBot(entity) -> botSize
                    entity.isClientFriend() -> friendSize
                    Teams.handleEvents() && Teams.isInYourTeam(entity) -> teamMateSize
                    else -> playerSize
                }
            }

            is EntityDragon, is EntityWither -> if (targetBosses) bossSize else 0F

            else -> {
                when {
                    entity.isMob() && targetHostileMobs -> hostileMobSize
                    entity.isAnimal() && targetPassiveMobs -> passiveMobSize
                    else -> 0F
                }
            }
        }
    }
}
