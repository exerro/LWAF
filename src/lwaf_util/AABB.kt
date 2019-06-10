package lwaf_util

import kotlin.math.max
import lwaf_core.plus
import lwaf_core.vec2
import kotlin.math.min

data class AABB(val min: vec2, val max: vec2)

fun AABB.translate(dp: vec2) = AABB(min + dp, max + dp)

infix fun AABB.intersects(b: AABB): Boolean {
    val a = this
    return a.max.x > b.min.x && b.max.x > a.min.x && a.max.y > b.min.y && b.max.y > a.min.y
}

infix fun AABB.intersection(b: AABB): AABB {
    val a = this
    if (!(a intersects b)) return AABB(vec2(0f), vec2(-1f))
    val min = vec2(max(a.min.x, b.min.x), max(a.min.y, b.min.y))
    val max = vec2(min(a.max.x, b.max.x), min(a.max.y, b.max.y))
    return AABB(min, max)
}
