package net.ccbluex.liquidbounce.utils.extensions

import net.minecraft.util.MovingObjectPosition.MovingObjectType

val MovingObjectType.isMiss
    get() = this == MovingObjectType.MISS

val MovingObjectType.isBlock
    get() = this == MovingObjectType.BLOCK

val MovingObjectType.isEntity
    get() = this == MovingObjectType.ENTITY