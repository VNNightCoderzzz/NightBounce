package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.angleDifference
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.toRotation
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.client.C0BPacketEntityAction.Action.*
import kotlin.math.abs

object BetterSuperKnockback : Module("BetterSuperKnockback", Category.NIGHTBOUNCEBETTERFUNCTIONS) {

    private val chance by int("Chance", 100, 0..100)
    private val delay by int("Delay", 0, 0..500)
    private val hurtTime by int("HurtTime", 10, 0..10)

    private val mode by choices(
        "Mode",
        arrayOf("WTap", "SprintTap", "SprintTap2", "Old", "Silent", "Packet", "SneakPacket", "LegitFast", "LegitTest", "STap"),
        "Old"
    )

    private val ticksUntilBlock by intRange("TicksUntilBlock", 0..2, 0..5) { mode == "WTap" }
    private val reSprintTicks by intRange("ReSprintTicks", 1..2, 1..5) { mode == "WTap" }
    private val targetDistance by int("TargetDistance", 3, 1..5) { mode == "WTap" }

    private val stopTicks by int("PressBackTicks", 1, 1..5) { mode == "SprintTap2" }.onChange { _, new ->
        unSprintTicks.value = maxOf(unSprintTicks.value, new)
    }
    private val unSprintTicks by int("ReleaseBackTicks", 2, 1..5) { mode == "SprintTap2" }.onChange { _, new ->
        stopTicks.value = minOf(stopTicks.value, new)
    }

    private val minEnemyRotDiffToIgnore by float("MinRotationDiffFromEnemyToIgnore", 180f, 0f..180f)

    private val onlyGround by boolean("OnlyGround", false)
    private val onlyMove by boolean("OnlyMove", true)
    private val onlyMoveForward by boolean("OnlyMoveForward", true) { onlyMove }
    private val onlyWhenTargetGoesBack by boolean("OnlyWhenTargetGoesBack", false)

    private var ticks = 0
    private var forceSprintState = 0
    private val timer = MSTimer()

    // State holders for modes
    private val wTapState = WTapState()
    private val sprintTap2State = SprintTap2State()

    override fun onToggle(state: Boolean) {
        wTapState.reset()
        sprintTap2State.reset()
        ticks = 0
        forceSprintState = 0
    }

    private interface ModeHandler {
        fun onAttack(player: EntityLivingBase, target: EntityLivingBase)
        fun onPostSprintUpdate(player: EntityLivingBase)
        fun onUpdate()
        fun onPacket(player: EntityLivingBase, packet: Any)
    }

    // Implement each mode as an object implementing ModeHandler
    private val modeHandlers = mapOf<String, ModeHandler>(
        "Old" to object : ModeHandler {
            override fun onAttack(player: EntityLivingBase, target: EntityLivingBase) {
                if (player.isSprinting) sendPacket(C0BPacketEntityAction(player, STOP_SPRINTING))
                sendPackets(
                    C0BPacketEntityAction(player, START_SPRINTING),
                    C0BPacketEntityAction(player, STOP_SPRINTING),
                    C0BPacketEntityAction(player, START_SPRINTING)
                )
                player.isSprinting = true
                player.serverSprintState = true
            }
            override fun onPostSprintUpdate(player: EntityLivingBase) {}
            override fun onUpdate() {}
            override fun onPacket(player: EntityLivingBase, packet: Any) {}
        },
        "SprintTap" to object : ModeHandler {
            override fun onAttack(player: EntityLivingBase, target: EntityLivingBase) {
                if (player.isSprinting && player.serverSprintState) ticks = 2
            }
            override fun onPostSprintUpdate(player: EntityLivingBase) {
                when (ticks) {
                    2 -> {
                        player.isSprinting = false
                        forceSprintState = 2
                        ticks--
                    }
                    1 -> {
                        if (player.movementInput.moveForward > 0.8) player.isSprinting = true
                        forceSprintState = 1
                        ticks--
                    }
                    else -> forceSprintState = 0
                }
            }
            override fun onUpdate() {}
            override fun onPacket(player: EntityLivingBase, packet: Any) {}
        },
        "SprintTap2" to object : ModeHandler {
            override fun onAttack(player: EntityLivingBase, target: EntityLivingBase) {
                sprintTap2State.sprintTicks++
                when (sprintTap2State.sprintTicks) {
                    stopTicks.get() -> {
                        sprintTap2State.toggleSprint(player)
                        mc.thePlayer.stopXZ()
                    }
                    in (unSprintTicks.get() + 1)..Int.MAX_VALUE -> {
                        player.isSprinting = false
                        player.serverSprintState = false
                        sprintTap2State.reset()
                    }
                }
            }
            override fun onPostSprintUpdate(player: EntityLivingBase) {}
            override fun onUpdate() {}
            override fun onPacket(player: EntityLivingBase, packet: Any) {}
        },
        "Silent" to object : ModeHandler {
            override fun onAttack(player: EntityLivingBase, target: EntityLivingBase) {
                if (player.isSprinting && player.serverSprintState) ticks = 2
            }
            override fun onPostSprintUpdate(player: EntityLivingBase) {}
            override fun onUpdate() {}
            override fun onPacket(player: EntityLivingBase, packet: Any) {
                if (packet !is C03PacketPlayer) return
                when (ticks) {
                    2 -> {
                        sendPacket(C0BPacketEntityAction(player, STOP_SPRINTING))
                        ticks--
                    }
                    1 -> if (player.isSprinting) {
                        sendPacket(C0BPacketEntityAction(player, START_SPRINTING))
                        ticks--
                    }
                }
            }
        },
        "Packet" to object : ModeHandler {
            override fun onAttack(player: EntityLivingBase, target: EntityLivingBase) {
                sendPackets(
                    C0BPacketEntityAction(player, STOP_SPRINTING),
                    C0BPacketEntityAction(player, START_SPRINTING)
                )
            }
            override fun onPostSprintUpdate(player: EntityLivingBase) {}
            override fun onUpdate() {}
            override fun onPacket(player: EntityLivingBase, packet: Any) {}
        },
        "SneakPacket" to object : ModeHandler {
            override fun onAttack(player: EntityLivingBase, target: EntityLivingBase) {
                sendPackets(
                    C0BPacketEntityAction(player, STOP_SPRINTING),
                    C0BPacketEntityAction(player, START_SNEAKING),
                    C0BPacketEntityAction(player, START_SPRINTING),
                    C0BPacketEntityAction(player, STOP_SNEAKING)
                )
            }
            override fun onPostSprintUpdate(player: EntityLivingBase) {}
            override fun onUpdate() {}
            override fun onPacket(player: EntityLivingBase, packet: Any) {}
        },
        "LegitFast" to legitModeHandler,
        "LegitTest" to legitModeHandler,
        "STap" to object : ModeHandler {
            override fun onAttack(player: EntityLivingBase, target: EntityLivingBase) {
                if (player.isSprinting && player.serverSprintState) {
                    sendPacket(C0BPacketEntityAction(player, STOP_SPRINTING))
                    repeat(2) {
                        sendPackets(
                            C0BPacketEntityAction(player, START_SPRINTING),
                            C0BPacketEntityAction(player, STOP_SPRINTING)
                        )
                    }
                    sendPacket(C0BPacketEntityAction(player, START_SPRINTING))

                    player.isSprinting = true
                    player.serverSprintState = true

                    player.motionX *= 0.97
                    player.motionZ *= 0.97
                }
            }
            override fun onPostSprintUpdate(player: EntityLivingBase) {}
            override fun onUpdate() {}
            override fun onPacket(player: EntityLivingBase, packet: Any) {}
        },
        "WTap" to object : ModeHandler {
            override fun onAttack(player: EntityLivingBase, target: EntityLivingBase) {
                if (player.isSprinting && player.serverSprintState && !wTapState.blockInput && !wTapState.startWaiting) {
                    val distDiff = abs(targetDistance - player.getDistanceToEntityBox(target))
                    val delayMultiplier = 1.0 / (distDiff + 1)

                    wTapState.blockInputTicks = (ticksUntilBlock.random() * delayMultiplier).toInt()
                    wTapState.blockInput = wTapState.blockInputTicks == 0

                    if (!wTapState.blockInput) wTapState.startWaiting = true

                    wTapState.allowInputTicks = (reSprintTicks.random() * delayMultiplier).toInt()
                }
            }
            override fun onPostSprintUpdate(player: EntityLivingBase) {}
            override fun onUpdate() {
                if (wTapState.blockInput) {
                    if (++wTapState.ticksElapsed >= wTapState.allowInputTicks) {
                        wTapState.blockInput = false
                        wTapState.ticksElapsed = 0
                    }
                } else if (wTapState.startWaiting) {
                    wTapState.blockInput = wTapState.blockTicksElapsed++ >= wTapState.blockInputTicks
                    if (wTapState.blockInput) {
                        wTapState.startWaiting = false
                        wTapState.blockTicksElapsed = 0
                    }
                }
            }
            override fun onPacket(player: EntityLivingBase, packet: Any) {}
        }
    )

    // Common legit mode handler
    private val legitModeHandler = object : ModeHandler {
        override fun onAttack(player: EntityLivingBase, target: EntityLivingBase) {
            try {
                val field = player.javaClass.getDeclaredField("sprintingTicksLeft")
                field.isAccessible = true
                field.setInt(player, 0)
            } catch (_: Exception) {
                // no-op
            }
        }
        override fun onPostSprintUpdate(player: EntityLivingBase) {}
        override fun onUpdate() {}
        override fun onPacket(player: EntityLivingBase, packet: Any) {}
    }

    val onAttack = handler<AttackEvent> { event ->
        val player = mc.thePlayer ?: return@handler
        val target = event.targetEntity as? EntityLivingBase ?: return@handler

        if (!canTrigger(player, target)) return@handler

        modeHandlers[mode]?.onAttack(player, target)
        timer.reset()
    }

    val onPostSprintUpdate = handler<PostSprintUpdateEvent> {
        if (mode !in listOf("SprintTap")) return@handler
        val player = mc.thePlayer ?: return@handler

        modeHandlers[mode]?.onPostSprintUpdate(player)
    }

    val onUpdate = handler<UpdateEvent> {
        if (mode != "WTap") return@handler
        modeHandlers[mode]?.onUpdate()
    }

    val onPacket = handler<PacketEvent> { event ->
        if (mode != "Silent") return@handler
        val player = mc.thePlayer ?: return@handler
        val packet = event.packet ?: return@handler

        modeHandlers[mode]?.onPacket(player, packet)
    }

    private fun canTrigger(player: EntityLivingBase, target: EntityLivingBase): Boolean {
        val distance = player.getDistanceToEntityBox(target)
        val rotationToPlayer = toRotation(player.hitBox.center, false, target).fixedSensitivity().yaw
        val angleDifferenceToPlayer = abs(angleDifference(rotationToPlayer, target.rotationYaw))

        if (target.hurtTime > hurtTime) return false
        if (!timer.hasTimePassed(delay)) return false
        if (onlyGround && !player.onGround) return false
        if (RandomUtils.nextInt(100) > chance) return false
        if (onlyMove && (!player.isMoving || (onlyMoveForward && player.movementInput.moveStrafe != 0f))) return false
        if (angleDifferenceToPlayer > minEnemyRotDiffToIgnore && !target.hitBox.isVecInside(player.eyes)) return false

        val posDiff = target.currPos - target.lastTickPos
        val distBasedOnMotion = player.getDistanceToBox(target.hitBox.offset(posDiff))
        if (onlyWhenTargetGoesBack && distBasedOnMotion >= player.getDistanceToEntityBox(target)) return false

        return true
    }

    private class WTapState {
        var blockInputTicks = 0
        var blockTicksElapsed = 0
        var startWaiting = false
        var blockInput = false
        var allowInputTicks = 0
        var ticksElapsed = 0

        fun reset() {
            blockInput = false
            startWaiting = false
            blockTicksElapsed = 0
            ticksElapsed = 0
            allowInputTicks = 0
            blockInputTicks = 0
        }
    }

    private class SprintTap2State {
        var sprintTicks = 0

        fun reset() {
            sprintTicks = 0
        }

        fun toggleSprint(player: EntityLivingBase) {
            val sprinting = player.isSprinting && player.serverSprintState
            player.isSprinting = !sprinting
            player.serverSprintState = !sprinting
        }
    }

    fun shouldBlockInput() = handleEvents() && mode == "WTap" && wTapState.blockInput
    override val tag get() = mode
    fun breakSprint() = handleEvents() && forceSprintState == 2 && mode == "SprintTap"
    fun startSprint() = handleEvents() && forceSprintState == 1 && mode == "SprintTap"
}
