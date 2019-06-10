import lwaf_core.*
import lwaf_util.PolynomialCoefficientSolver
import java.lang.Math.pow
import kotlin.math.ceil

private const val ARC_SEGMENT_LENGTH = 3f
private const val CURVE_SEGMENT_LENGTH = 10f
private const val BEZIER_CURVE_SEGMENT_LENGTH = 20f

class Path(val start: vec2, init: Path.() -> Unit) {
    private val children = mutableListOf<PathStep>()
    constructor(x: Float, y: Float, init: Path.() -> Unit): this(vec2(x, y), init)

    init { init(this) }

    fun lineTo(point: vec2) { children.add(LineToStep(point)) }
    fun lineTo(x: Float, y: Float) = lineTo(vec2(x, y))
    fun arcTo(point: vec2, radius: Float) { children.add(ArcToStep(point, radius)) }
    fun arcTo(x: Float, y: Float, radius: Float) = arcTo(vec2(x, y), radius)
    fun curveTo(point: vec2, controls: Curve.() -> Unit) { Curve(point).let { controls(it); children.add(CurveToStep(it)) } }
    fun curveTo(x: Float, y: Float, controls: Curve.() -> Unit) = curveTo(vec2(x, y), controls)
    fun bezierCurveTo(point: vec2, controls: Curve.() -> Unit) { Curve(point).let { controls(it); children.add(BezierCurveToStep(it)) } }
    fun bezierCurveTo(x: Float, y: Float, controls: Curve.() -> Unit) = bezierCurveTo(vec2(x, y), controls)
    fun close() { lineTo(start) }

    fun computePoints(closed: Boolean = false, detail: Float = 1f): List<vec2> {
        val points = mutableListOf(start)

        children.forEach { step -> when (step) {
            is LineToStep -> points.add(step.point)
            is ArcToStep -> TODO()
            is CurveToStep -> points.addAll(generateCurvePoints(
                    points.last(),
                    step.curve.to,
                    step.curve.controls.mapIndexed { i, v ->
                        (i + 1) / (step.curve.controls.size + 1f) to v
                    },
                    estimateCurveLength(points.last(), step.curve.to, step.curve.controls).let { len ->
                        ceil(len / CURVE_SEGMENT_LENGTH * detail).toInt().let { points ->
                            (1 until points).map { it.toFloat() / points }
                        }
                    }
            ))
            is BezierCurveToStep -> points.addAll(generateBezierCurvePoints(
                    listOf(points.last()) + step.curve.controls + listOf(step.curve.to),
                    estimateBezierCurveLength(points.last(), step.curve.to, step.curve.controls).let { len ->
                        ceil(len / BEZIER_CURVE_SEGMENT_LENGTH * detail).toInt().let { points ->
                            (1 until points).map { it.toFloat() / points }
                        }
                    }
            ))
        } }

        if (closed && points[0] != points.last()) {
            points.add(points[0])
        }

        return points
    }
}

class Curve(val to: vec2) {
    internal val controls = mutableListOf<vec2>()

    fun controlPoint(point: vec2) { controls.add(point) }
    fun controlPoint(x: Float, y: Float) = controlPoint(vec2(x, y))
}

private sealed class PathStep
private data class LineToStep(val point: vec2): PathStep()
private data class ArcToStep(val point: vec2, val radius: Float): PathStep()
private data class CurveToStep(val curve: Curve): PathStep()
private data class BezierCurveToStep(val curve: Curve): PathStep()

fun generateCurvePoints(first: vec2, last: vec2, controls: List<Pair<Float, vec2>>, samples: List<Float>): List<vec2> {
    val n = controls.size + 2
    val xSolver = PolynomialCoefficientSolver(n)
    val ySolver = PolynomialCoefficientSolver(n)

    xSolver.setValues(first.x, last.x, *controls.map { (_, v) -> v.x } .toFloatArray())
    xSolver.setRow(0, *powers(0f, n))
    xSolver.setRow(1, *powers(1f, n))
    controls.mapIndexed { i, (t, _) -> xSolver.setRow(i + 2, *powers(t, n)) }

    ySolver.setValues(first.y, last.y, *controls.map { (_, v) -> v.y } .toFloatArray())
    ySolver.setRow(0, *powers(0f, n))
    ySolver.setRow(1, *powers(1f, n))
    controls.mapIndexed { i, (t, _) -> ySolver.setRow(i + 2, *powers(t, n)) }

    val xPoly = xSolver.solve()
    val yPoly = ySolver.solve()

    return (samples + 1f).map { t -> vec2(poly(xPoly, t), poly(yPoly, t)) }
}

fun generateBezierCurvePoints(controls: List<vec2>, samples: List<Float>): List<vec2> {
    return (samples + 1f).map { t -> evaluateBezierCurvePoint(t, controls) }
}

fun evaluateBezierCurvePoint(t: Float, controls: List<vec2>): vec2 {
    if (controls.size == 1) return controls[0]

    return evaluateBezierCurvePoint(t, controls.zip(controls.drop(1)).map { (a, b) ->
        a * (1 - t) + b * t
    } )
}

fun estimateCurveLength(first: vec2, last: vec2, controls: List<vec2>): Float
        = (listOf(first) + controls).zip(controls + listOf(last)).map { (a, b) -> (b - a).length() } .sum()

fun estimateBezierCurveLength(first: vec2, last: vec2, controls: List<vec2>): Float
        = (listOf(first) + controls).zip(controls + listOf(last)).map { (a, b) -> (b - a).length() } .sum()

fun powers(t: Float, n: Int): FloatArray = Array(n) { 1f } .let { result ->
    for (i in 1 until result.size) {
        result[i] = result[i - 1] * t
    }
    result
} .toFloatArray()

fun poly(poly: FloatArray, t: Float): Float
        = poly.mapIndexed { i, x -> x * pow(t.toDouble(), i.toDouble()).toFloat() } .sum()
