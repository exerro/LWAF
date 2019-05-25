package lwaf_core

data class vec4(val x: Float, val y: Float, val z: Float, val w: Float = 1f)
data class vec3(val x: Float, val y: Float = x, val z: Float = y)
data class vec2(val x: Float, val y: Float = x)

fun vec3.cross(v: vec3) = vec3(
        y*v.z - z*v.y,
        z*v.x - x*v.z,
        x*v.y - y*v.x
)

fun vec4.vec3() = vec3(x, y, z)
fun vec3.vec2() = vec2(x, y)

fun vec3.vec4(w: Float) = vec4(x, y, z, w)
fun vec2.vec3(z: Float) = vec3(x, y, z)

fun vec2.length2() = x * x + y * y
fun vec3.length2() = x * x + y * y + z * z

fun vec2.length() = Math.sqrt(length2().toDouble()).toFloat()
fun vec3.length() = Math.sqrt(length2().toDouble()).toFloat()

fun vec2.normalise() = this / length()
fun vec3.normalise() = this / length()

fun vec2.dot(v: vec2) = x * v.x + y * v.y
fun vec3.dot(v: vec3) = x * v.x + y * v.y + z * v.z

operator fun vec3.plus(v: vec3) = vec3(x + v.x, y + v.y, z + v.z)
operator fun vec3.minus(v: vec3) = vec3(x - v.x, y - v.y, z - v.z)
operator fun vec3.unaryMinus() = vec3(-x, -y, -z)
operator fun vec3.times(v: vec3) = vec3(x * v.x, y * v.y, z * v.z)
operator fun vec3.times(s: Float) = vec3(x * s, y * s, z * s)
operator fun vec3.div(v: vec3) = vec3(x / v.x, y / v.y, z / v.z)
operator fun vec3.div(s: Float) = this * (1/s)

operator fun vec2.plus(v: vec2) = vec2(x + v.x, y + v.y)
operator fun vec2.minus(v: vec2) = vec2(x - v.x, y - v.y)
operator fun vec2.unaryMinus() = vec2(-x, -y)
operator fun vec2.times(v: vec2) = vec2(x * v.x, y * v.y)
operator fun vec2.times(s: Float) = vec2(x * s, y * s)
operator fun vec2.div(v: vec2) = vec2(x / v.x, y / v.y)
operator fun vec2.div(s: Float) = this * (1/s)

fun vec3.toRotationMatrix(): mat3
        = mat3_rotate(y, vec3(0f, 1f, 0f)) *
          mat3_rotate(x, vec3(1f, 0f, 0f)) *
          mat3_rotate(z, vec3(0f, 0f, 1f))

fun vec3.toInverseRotationMatrix(): mat3
        = mat3_rotate(-z, vec3(0f, 0f, 1f)) *
          mat3_rotate(-x, vec3(1f, 0f, 0f)) *
          mat3_rotate(-y, vec3(0f, 1f, 0f))
