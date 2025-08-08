package net.ccbluex.liquidbounce.event

import net.ccbluex.liquidbounce.features.module.modules.render.FreeCam
import net.ccbluex.liquidbounce.utils.extensions.withY
import net.minecraft.block.Block
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.entity.Entity
import net.minecraft.network.Packet
import net.minecraft.util.*

class AttackEvent(val targetEntity: Entity?) : CancellableEvent()

class BlockBBEvent(blockPos: BlockPos, val block: Block, var boundingBox: AxisAlignedBB?) : Event() {
    val x = blockPos.x
    val y = blockPos.y
    val z = blockPos.z
}

class ClickBlockEvent(val clickedBlock: BlockPos?, val enumFacing: EnumFacing?) : Event()

object ClientShutdownEvent : Event()
object ClickUpdateEvent : CancellableEvent() {
    fun reInit() {
        isCancelled = false;
    }
}
data class EntityMovementEvent(val movedEntity: Entity) : Event()

class JumpEvent(var motion: Float, val eventState: EventState) : CancellableEvent()

class KeyEvent(val key: Int) : Event()

class MotionEvent(var x: Double, var y: Double, var z: Double, var onGround: Boolean, val eventState: EventState) : Event()

class SlowDownEvent(var strafe: Float, var forward: Float) : Event()

class SneakSlowDownEvent(var strafe: Float, var forward: Float) : Event()

class MovementInputEvent(var originalInput: MovementInput) : Event()

object PostSprintUpdateEvent : Event()

class StrafeEvent(val strafe: Float, val forward: Float, val friction: Float) : CancellableEvent()

class MoveEvent(var x: Double, var y: Double, var z: Double) : CancellableEvent() {
    var isSafeWalk = false

    fun zero() {
        x = 0.0
        y = 0.0
        z = 0.0
    }

    fun zeroXZ() {
        x = 0.0
        z = 0.0
    }
}

class PacketEvent(val packet: Packet<*>, val eventType: EventState) : CancellableEvent()

class BlockPushEvent : CancellableEvent()

class Render2DEvent(val partialTicks: Float) : Event()

object GameLoopEvent : Event()

class Render3DEvent(val partialTicks: Float) : Event()

class ScreenEvent(val guiScreen: GuiScreen?) : Event()

object SessionUpdateEvent : Event()

class StepEvent(var stepHeight: Float) : Event()

object StepConfirmEvent : Event()

object GameTickEvent : Event()

object TickEndEvent : Event()

class PlayerTickEvent(val state: EventState) : CancellableEvent()

object RotationUpdateEvent : Event()

class RotationSetEvent(var yawDiff: Float, var pitchDiff: Float) : CancellableEvent()

class CameraPositionEvent(
    private val currPos: Vec3, private val prevPos: Vec3, private val lastTickPos: Vec3,
    var result: FreeCam.PositionPair? = null,
) : Event() {
    fun withY(value: Double) {
        result = FreeCam.PositionPair(currPos.withY(value), prevPos.withY(value), lastTickPos.withY(value))
    }
}

class ClientSlotChangeEvent(var supposedSlot: Int, var modifiedSlot: Int) : Event()

class DelayedPacketProcessEvent : CancellableEvent()

object UpdateEvent : Event()

class WorldEvent(val worldClient: WorldClient?) : Event()

class ClickWindowEvent(val windowId: Int, val slotId: Int, val mouseButtonClicked: Int, val mode: Int) : CancellableEvent()

object StartupEvent : Event()

internal val ALL_EVENT_CLASSES = arrayOf(
    PlayerTickEvent::class.java,
    StepConfirmEvent::class.java,
    SessionUpdateEvent::class.java,
    MovementInputEvent::class.java,
    GameLoopEvent::class.java,
    Render2DEvent::class.java,
    ClickWindowEvent::class.java,
    StartupEvent::class.java,
    SneakSlowDownEvent::class.java,
    PostSprintUpdateEvent::class.java,
    KeyEvent::class.java,
    SlowDownEvent::class.java,
    TickEndEvent::class.java,
    JumpEvent::class.java,
    MoveEvent::class.java,
    ClientShutdownEvent::class.java,
    GameTickEvent::class.java,
    StepEvent::class.java,
    BlockBBEvent::class.java,
    ClickBlockEvent::class.java,
    UpdateEvent::class.java,
    RotationSetEvent::class.java,
    EntityMovementEvent::class.java,
    ClientSlotChangeEvent::class.java,
    PacketEvent::class.java,
    CameraPositionEvent::class.java,
    RotationUpdateEvent::class.java,
    StrafeEvent::class.java,
    ScreenEvent::class.java,
    AttackEvent::class.java,
    BlockPushEvent::class.java,
    Render3DEvent::class.java,
    MotionEvent::class.java,
    WorldEvent::class.java,
    DelayedPacketProcessEvent::class.java,
    ClickUpdateEvent::class.java
)
