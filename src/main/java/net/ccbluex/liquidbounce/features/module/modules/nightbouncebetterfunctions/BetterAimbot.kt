package net.ccbluex.liquidbounce.features.module.modules.nightbouncebetterfunctions

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.player.Reach
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.features.module.modules.combat.*
import net.ccbluex.liquidbounce.features.module.modules.exploit.*
import net.ccbluex.liquidbounce.features.module.modules.misc.*
import net.ccbluex.liquidbounce.features.module.modules.movement.*
import net.ccbluex.liquidbounce.features.module.modules.player.*
import net.ccbluex.liquidbounce.features.module.modules.render.*
import net.ccbluex.liquidbounce.features.module.modules.world.*
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.currentRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.isFaced
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.performAngleChange
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.rotationDifference
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.searchCenter
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.toRotation
import net.ccbluex.liquidbounce.utils.simulation.SimulatedPlayer
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.entity.Entity
import java.util.*
import kotlin.math.atan

object BetterAimbot : Module("BetterAimbot", Category.NIGHTBOUNCEBETTERFUNCTIONS) {

    private val range by float("Range", 4.4F, 1F..8F)
    private val horizontalAim by boolean("HorizontalAim", true)
    private val verticalAim by boolean("VerticalAim", true)
    private val legitimize by boolean("Legitimize", true) { horizontalAim || verticalAim }
    private val maxAngleChange by float("MaxAngleChange", 7f, 1F..180F) { horizontalAim || verticalAim }
    private val inViewMaxAngleChange by float("InViewMaxAngleChange", 25f, 1f..180f) { horizontalAim || verticalAim }
    private val smoothRotation by float("SmoothRotation", 0.7f, 0.1f..1f) { horizontalAim || verticalAim }
    private val generateSpotBasedOnDistance by boolean("GenerateSpotBasedOnDistance", true) { horizontalAim || verticalAim }
    private val predictClientMovement by int("PredictClientMovement", 3, 0..5)
    private val predictEnemyPosition by float("PredictEnemyPosition", 1.2f, -1f..2f)

    private val highestBodyPointToTargetValue = choices(
        "HighestBodyPointToTarget", arrayOf("Head", "Body", "Feet"), "Head"
    ) { verticalAim }.onChange { _, new ->
        val newPoint = RotationUtils.BodyPoint.fromString(new)
        val lowestPoint = RotationUtils.BodyPoint.fromString(lowestBodyPointToTarget)
        val coercedPoint = RotationUtils.coerceBodyPoint(newPoint, lowestPoint, RotationUtils.BodyPoint.HEAD)
        coercedPoint.displayName
    }
    private val highestBodyPointToTarget: String by highestBodyPointToTargetValue

    private val lowestBodyPointToTargetValue = choices(
        "LowestBodyPointToTarget", arrayOf("Head", "Body", "Feet"), "Feet"
    ) { verticalAim }.onChange { _, new ->
        val newPoint = RotationUtils.BodyPoint.fromString(new)
        val highestPoint = RotationUtils.BodyPoint.fromString(highestBodyPointToTarget)
        val coercedPoint = RotationUtils.coerceBodyPoint(newPoint, RotationUtils.BodyPoint.FEET, highestPoint)
        coercedPoint.displayName
    }
    private val lowestBodyPointToTarget: String by lowestBodyPointToTargetValue

    private val horizontalBodySearchRange by floatRange("HorizontalBodySearchRange", 0f..1f, 0f..1f) { horizontalAim }
    private val minRotationDifference by float("MinRotationDifference", 0f, 0f..2f) { verticalAim || horizontalAim }
    private val minRotationDifferenceResetTiming by choices(
        "MinRotationDifferenceResetTiming", arrayOf("OnStart", "Always"), "OnStart"
    ) { verticalAim || horizontalAim }

    private val fov by float("FOV", 180F, 1F..180F)
    private val lock by boolean("Lock", true) { horizontalAim || verticalAim }
    private val onClick by boolean("OnClick", false) { horizontalAim || verticalAim }
    private val jitter by boolean("Jitter", false)
    private val yawJitterMultiplier by float("JitterYawMultiplier", 1f, 0.1f..2.5f)
    private val pitchJitterMultiplier by float("JitterPitchMultiplier", 1f, 0.1f..2.5f)
    private val center by boolean("Center", false)
    private val headLock by boolean("Headlock", false) { center && lock }
    private val headLockBlockHeight by float("HeadBlockHeight", -1f, -2f..0f) { headLock && center && lock }
    private val breakBlocks by boolean("BreakBlocks", true)

    private val clickTimer = MSTimer()

    val onMotion = handler<MotionEvent> { event ->
        if (event.eventState != EventState.POST) return@handler
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler
        if (mc.gameSettings.keyBindAttack.isKeyDown) clickTimer.reset()
        if (onClick && (clickTimer.hasTimePassed(150) || !mc.gameSettings.keyBindAttack.isKeyDown && AutoClicker.handleEvents())) return@handler
        val entity = world.loadedEntityList.filter {
            Backtrack.runWithNearestTrackedDistance(it) {
                isSelected(it, true) && player.canEntityBeSeen(it) && player.getDistanceToEntityBox(it) <= range && rotationDifference(it) <= fov
            }
        }.minByOrNull { player.getDistanceToEntityBox(it) } ?: return@handler
        if (!lock && isFaced(entity, range.toDouble())) return@handler
        val random = Random()
        if (Backtrack.runWithNearestTrackedDistance(entity) { !findRotation(entity, random) }) return@handler
        if (jitter) {
            if (random.nextBoolean()) player.fixedSensitivityYaw += ((random.nextGaussian() - 0.5f) * yawJitterMultiplier).toFloat()
            if (random.nextBoolean()) player.fixedSensitivityPitch += ((random.nextGaussian() - 0.5f) * pitchJitterMultiplier).toFloat()
        }
    }

    private fun findRotation(entity: Entity, random: Random): Boolean {
        val player = mc.thePlayer ?: return false
        if (mc.playerController.isHittingBlock && breakBlocks) return false
        val velocity = entity.currPos.subtract(entity.prevPos)
        val distance = player.getDistanceToEntityBox(entity)
        val predictFactor = (0.5 + (distance / range) * 0.7) * predictEnemyPosition
        val prediction = velocity.times(predictFactor.toDouble())
        val boundingBox = entity.hitBox.offset(prediction)
        val (currPos, oldPos) = player.currPos to player.prevPos
        val simPlayer = SimulatedPlayer.fromClientPlayer(RotationUtils.modifiedInput)
        simPlayer.rotationYaw = (currentRotation ?: player.rotation).yaw
        repeat(predictClientMovement) { simPlayer.tick() }
        player.setPosAndPrevPos(simPlayer.pos)
        val destinationRotation = if (center) {
            toRotation(boundingBox.center, true)
        } else {
            searchCenter(
                boundingBox,
                generateSpotBasedOnDistance,
                outborder = false,
                predict = true,
                lookRange = range,
                attackRange = if (Reach.handleEvents()) Reach.combatReach else 3f,
                bodyPoints = listOf(highestBodyPointToTarget, lowestBodyPointToTarget),
                horizontalSearch = horizontalBodySearchRange
            )
        } ?: run {
            player.setPosAndPrevPos(currPos, oldPos)
            return false
        }
        if (headLock && center && lock) {
            val pitchOffset = Math.toDegrees(atan((headLockBlockHeight + player.eyeHeight) / distance)).toFloat()
            destinationRotation.pitch -= pitchOffset
        }
        val rotationDiff = rotationDifference(player.rotation, destinationRotation)
        val baseTurnSpeed = if (rotationDiff < mc.gameSettings.fovSetting) inViewMaxAngleChange else maxAngleChange
        val gaussian = (random.nextGaussian() * 0.1)
        val acceleration = (1.0 - (rotationDiff / fov)).coerceIn(0.2, 1.0)
        val realisticTurnSpeed = (baseTurnSpeed * acceleration + gaussian).toFloat()
        if (jitter && rotationDiff < 5) {
            destinationRotation.yaw += ((random.nextGaussian() - 0.5) * yawJitterMultiplier).toFloat()
            destinationRotation.pitch += ((random.nextGaussian() - 0.5) * pitchJitterMultiplier).toFloat()
        }
        val rotation = performAngleChange(
            player.rotation,
            destinationRotation,
            realisticTurnSpeed,
            legitimize = legitimize,
            minRotationDiff = minRotationDifference,
            minRotationDiffResetTiming = minRotationDifferenceResetTiming,
        )
        rotation.toPlayer(player, horizontalAim, verticalAim)
        player.setPosAndPrevPos(currPos, oldPos)
        return true
    }
}
