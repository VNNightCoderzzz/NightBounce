package net.ccbluex.liquidbounce.utils.attack

class RollingArrayLongBuffer(length: Int) {
        var contents: LongArray
        private set

    private var currentIndex = 0

    init {
        contents = LongArray(length)
    }

        fun add(l: Long) {
        currentIndex = (currentIndex + 1) % contents.size
        contents[currentIndex] = l
    }

        fun getTimestampsSince(l: Long): Int {
        for (i in contents.indices) {
            if (contents[if (currentIndex < i) contents.size - i + currentIndex else currentIndex - i] < l) {
                return i
            }
        }

        // If every element is lower than l, return the array length
        return contents.size
    }
}
