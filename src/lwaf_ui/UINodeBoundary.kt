import lwaf_core.vec2

data class Boundary(
        val left: Float = 0f,
        val top: Float = left,
        val right: Float = left,
        val bottom: Float = top
)

operator fun Boundary.plus(other: Boundary)
        = Boundary(left + other.left, top + other.top, right + other.right, bottom + other.bottom)

fun Boundary.totalSize()
        = vec2(left + right, top + bottom)
