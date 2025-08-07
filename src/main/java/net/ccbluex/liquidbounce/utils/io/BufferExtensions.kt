package net.ccbluex.liquidbounce.utils.io

import java.nio.Buffer
import java.nio.ByteBuffer

fun ByteBuffer.flipSafely() {
    try {
        flip()
    } catch (ex: Exception) {
        try {
            (this as Buffer).flip()
        } catch (any: Exception) {
            any.printStackTrace()
        }
    }
}