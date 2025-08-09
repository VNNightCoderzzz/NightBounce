package net.ccbluex.liquidbounce.features.module.modules.nightbouncebetterfunctions

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.combat.*
import net.ccbluex.liquidbounce.features.module.modules.exploit.*
import net.ccbluex.liquidbounce.features.module.modules.misc.*
import net.ccbluex.liquidbounce.features.module.modules.movement.*
import net.ccbluex.liquidbounce.features.module.modules.player.*
import net.ccbluex.liquidbounce.features.module.modules.render.*
import net.ccbluex.liquidbounce.features.module.modules.world.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextFloat
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import kotlin.math.sqrt

object BetterVelocity : Module("BetterVelocity", Category.NIGHTBOUNCEBETTERFUNCTIONS) {

    private val mode by choices("Mode", arrayOf("Simple", "Hypixel", "Intave", "GrimReduce", "Reverse"), "Simple")

    private val horizontalMin by float("HorizontalMin", 0.6f, 0f..1f)
    private val horizontalMax by float("HorizontalMax", 0.8f, 0f..1f)
    private val verticalMin by float("VerticalMin", 0.6f, 0f..1f)
    private val verticalMax by float("VerticalMax", 0.8f, 0f..1f)

    private val delayMs by int("DelayMs", 0, 0..500)

    private val grimTicks by int("GrimTicks", 4, 1..10)

    private val limitMotion by boolean("LimitMotion", true)
    private val maxXZ by float("MaxXZ", 0.4f, 0f..1.9f)
    private val maxY by float("MaxY", 0.36f, 0f..0.46f)

    private var hasVelocity = false
    private var tickCounter = 0

    override val tag
        get() = "$mode"

    override fun onDisable() {
        hasVelocity = false
        tickCounter = 0
    }

    private fun randomFactor(min: Float, max: Float): Float {
        return if (min == max) min else nextFloat(min, max)
    }

    private fun applyMotionReduction(player: EntityPlayerSP, factorX: Float, factorY: Float, factorZ: Float) {
        player.motionX *= factorX
        player.motionZ *= factorZ
        player.motionY *= factorY

        if (limitMotion) {
            val distXZ = sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ)
            if (distXZ > maxXZ) {
                val ratioXZ = maxXZ / distXZ
                player.motionX *= ratioXZ
                player.motionZ *= ratioXZ
            }
            if (player.motionY > maxY + 0.00075) {
                player.motionY = maxY + 0.00075
            }
        }
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        when (mode.lowercase()) {
            "grimreduce" -> {
                if (hasVelocity && player.hurtTime in 1..grimTicks) {
                    applyMotionReduction(
                        player,
                        randomFactor(horizontalMin, horizontalMax),
                        randomFactor(verticalMin, verticalMax),
                        randomFactor(horizontalMin, horizontalMax)
                    )
                }
            }
            "reverse" -> {
                if (hasVelocity && !player.onGround) {
                    player.motionX *= -randomFactor(horizontalMin, horizontalMax)
                    player.motionZ *= -randomFactor(horizontalMin, horizontalMax)
                }
            }
            "hypixel" -> {
                if (hasVelocity && tickCounter < 5) {
                    tickCounter++
                    applyMotionReduction(
                        player,
                        randomFactor(0.7f, 0.85f),
                        randomFactor(0.8f, 0.9f),
                        randomFactor(0.7f, 0.85f)
                    )
                } else {
                    hasVelocity = false
                    tickCounter = 0
                }
            }
            "intave" -> {
                if (hasVelocity && player.hurtTime == 9) {
                    if (player.onGround && player.isSprinting) player.jump()
                    applyMotionReduction(player, 0.7f, 1f, 0.7f)
                }
            }
        }
    }

    val onPacket = handler<PacketEvent>(priority = 1) { event ->
        val player = mc.thePlayer ?: return@handler
        val packet = event.packet

        if (packet is S12PacketEntityVelocity && packet.entityID == player.entityId) {
            velocityLogic(event, player, packet)
        }

        if (packet is S27PacketExplosion) {
            explosionLogic(event, player, packet)
        }
    }

    private fun velocityLogic(event: PacketEvent, player: EntityPlayerSP, packet: S12PacketEntityVelocity) {
        when (mode.lowercase()) {
            "simple", "grimreduce", "reverse" -> {
                event.cancelEvent()
                hasVelocity = true
                if (delayMs > 0) {
                    Thread.sleep(delayMs.toLong())
                }
                applyMotionReduction(
                    player,
                    randomFactor(horizontalMin, horizontalMax),
                    randomFactor(verticalMin, verticalMax),
                    randomFactor(horizontalMin, horizontalMax)
                )
                keepSprint(player)
            }
            "hypixel" -> {
                event.cancelEvent()
                hasVelocity = true
                tickCounter = 0
                keepSprint(player)
            }
            "intave" -> {
                event.cancelEvent()
                hasVelocity = true
                keepSprint(player)
            }
        }
    }

    private fun explosionLogic(event: PacketEvent, player: EntityPlayerSP, packet: S27PacketExplosion) {
        when (mode.lowercase()) {
            "simple", "grimreduce", "reverse" -> {
                packet.field_149152_f *= randomFactor(horizontalMin, horizontalMax) // X
                packet.field_149153_g *= randomFactor(verticalMin, verticalMax) // Y
                packet.field_149159_h *= randomFactor(horizontalMin, horizontalMax) // Z
            }
        }
    }

    private fun keepSprint(player: EntityPlayerSP) {
        if (!player.isSprinting) {
            sendPacket(C0BPacketEntityAction(player, C0BPacketEntityAction.Action.START_SPRINTING))
        }
    }
}
