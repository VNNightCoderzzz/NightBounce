package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.features.module.*
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.attack.CombatCheck
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.world.WorldSettings
import java.util.*
import kotlin.random.Random

object NightReach : Module("NightReach", Category.COMBAT) {

    private val modeValue by choices("Mode", arrayOf("NightIntave", "NightFakePlayer"), "NightFakePlayer")
    private val pulseDelayValue by int("PulseDelay", 200, 50..500) { modeValue != "None" }
    private val intavePackets by int("IntavePackets", 5, 0..20) { modeValue == "NightIntave" }
    private val randomDelay by boolean("RandomDelay", true)

    private var fakePlayer: EntityOtherPlayerMP? = null
    private var currentTarget: EntityLivingBase? = null
    private var shown = false
    private val pulseTimer = MSTimer()

    override fun onDisable() = removeFake()

    private fun removeFake() {
        fakePlayer?.let {
            MinecraftInstance.mc.theWorld?.removeEntity(it)
            fakePlayer = null
            shown = false
        }
    }

    private fun createFake(target: EntityLivingBase) {
        val world = MinecraftInstance.mc.theWorld ?: return
        val playerInfo = MinecraftInstance.mc.netHandler.getPlayerInfo(target.uniqueID) ?: return
        val faker = EntityOtherPlayerMP(world, playerInfo.gameProfile).apply {
            copyLocationAndAnglesFrom(target)
            rotationYawHead = target.rotationYawHead
            renderYawOffset = target.renderYawOffset
            health = target.health
            (0..4).forEach { i -> setCurrentItemOrArmor(i, target.getEquipmentInSlot(i)) }
        }
        world.addEntityToWorld(-69420, faker)
        fakePlayer = faker
        shown = true
    }

    private fun attack(entity: EntityLivingBase) {
        MinecraftInstance.mc.thePlayer?.apply {
            swingItem()
            MinecraftInstance.mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))
            if (MinecraftInstance.mc.playerController.currentGameType != WorldSettings.GameType.SPECTATOR) {
                attackTargetEntityWithCurrentItem(entity)
            }
        }
    }

    val onAttack = handler<AttackEvent> { event ->
        val target = event.targetEntity as? EntityLivingBase ?: return@handler
        CombatCheck.setTarget(target)

        if (modeValue == "NightFakePlayer" || modeValue == "NightIntave") {
            if (fakePlayer == null) {
                currentTarget = target
                createFake(target)
            } else if (event.targetEntity == fakePlayer) {
                currentTarget?.let { attack(it) }
                event.cancelEvent()
            } else {
                removeFake()
                currentTarget = target
                createFake(target)
            }
        }
    }

    val onUpdate = handler<UpdateEvent> {
        CombatCheck.updateCombatState()
        if (!CombatCheck.inCombat || currentTarget?.isEntityAlive != true) {
            removeFake()
            return@handler
        }

        val delay = if (randomDelay) pulseDelayValue + Random.nextInt(-20, 20) else pulseDelayValue

        fakePlayer?.let { faker ->
            currentTarget?.let { target ->
                if (!faker.isEntityAlive || target.isDead) {
                    removeFake()
                } else {
                    faker.health = target.health
                    (0..4).forEach { i -> target.getEquipmentInSlot(i)?.let { faker.setCurrentItemOrArmor(i, it) } }
                    if (modeValue == "NightFakePlayer" && pulseTimer.hasTimePassed(delay.toLong())) {
                        faker.rotationYawHead = target.rotationYawHead
                        faker.renderYawOffset = target.renderYawOffset
                        faker.copyLocationAndAnglesFrom(target)
                        pulseTimer.reset()
                    }
                    if (modeValue == "NightIntave" && MinecraftInstance.mc.thePlayer.ticksExisted % intavePackets == 0) {
                        faker.rotationYawHead = target.rotationYawHead
                        faker.renderYawOffset = target.renderYawOffset
                        faker.copyLocationAndAnglesFrom(target)
                        pulseTimer.reset()
                    }
                }
            }
        }

        if (!shown && currentTarget != null) createFake(currentTarget!!)
    }

}