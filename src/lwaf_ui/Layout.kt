import lwaf_core.minus
import lwaf_core.plus
import lwaf_core.times
import lwaf_core.vec2
import kotlin.properties.Delegates

sealed class Layout {
    internal val nodes = mutableSetOf<UINode>()

    internal fun changed() {
        nodes.map { it.needsRepositioning = true }
    }
}

class GridLayout: Layout() {
    private val hLines = mutableMapOf<String, GridLine>()
    private val vLines = mutableMapOf<String, GridLine>()

    fun addHorizontalLine(name: String, line: GridLine) {
        hLines[name] = line
        changed()
    }

    fun addVerticalLine(name: String, line: GridLine) {
        vLines[name] = line
        changed()
    }

    // TODO: regions spanning gridlines and nodes mapped to regions
}

class FlowLayout: Layout() {
    var verticalSpacing by Delegates.observable(0f) { _, _, _ -> changed() }
    var horizontalSpacing by Delegates.observable(0f) { _, _, _ -> changed() }
    var verticalDirection by Delegates.observable(FlowDirection.Default) { _, _, _ -> changed() }
    var horizontalDirection by Delegates.observable(FlowDirection.Default) { _, _, _ -> changed() }
}

class ListLayout: Layout() {
    var spacing by Delegates.observable(0f) { _, _, _ -> changed() }
    var direction by Delegates.observable(FlowDirection.Default) { _, _, _ -> changed() }
}

class FillLayout: Layout() {
    var alignment by Delegates.observable(vec2(0.5f)) { _, _, _ -> changed() }
}

class FreeLayout: Layout() {
    // TODO: anchor points set and node bounds set to anchor points
}

// TODO: need to take margin and padding into account
fun positionNode(node: UINode, widthConstraint: Float?, heightConstraint: Float?): vec2 {
    val marginSize by lazy { node.margin.totalSize() }
    val boundarySize by lazy { marginSize + node.padding.totalSize() }
    val nodeWidth = node.width ?.let { it + marginSize.x } ?: widthConstraint ?: 0f

    when (val layout = node.layout) {
        //  child-size :: parent-size * k
        is GridLayout -> TODO()
        is FlowLayout -> {
            var x = 0f
            var y = 0f
            val children = node.getChildren()
            val rows = mutableListOf(mutableListOf<UINode>())

            children.map {
                val size = positionNode(it, nodeWidth - x, null)

                if (x + size.x > nodeWidth) {
                    x = 0f
                    rows.add(mutableListOf())
                }

                x += size.x + layout.horizontalSpacing
                rows.last().add(it)
                it.computedSize = size
            }

            val childrenHeight = rows.map { it.map { it.computedSize.y } .max() ?: 0f } .sum() + (rows.size - 1) * layout.verticalSpacing
            val nodeHeight = node.height ?.let { it + marginSize.y } ?: childrenHeight + boundarySize.y

            // TODO: compute offset vector for alignment within content area

            rows.filter { it.isNotEmpty() } .map { row ->
                x = 0f

                row.map {
                    val px = when (layout.horizontalDirection) {
                        FlowDirection.Default -> x
                        FlowDirection.Reversed -> nodeWidth - it.computedSize.x - x
                    }
                    val py = when (layout.verticalDirection) {
                        FlowDirection.Default -> y
                        FlowDirection.Reversed -> nodeHeight - it.computedSize.y - y
                    }
                    it.computedLocalPosition = vec2(px, py)
                    x += it.computedSize.x + layout.horizontalSpacing
                }
                y += (row.map { it.computedSize.y } .max() ?: 0f) + layout.verticalSpacing
            }

            return vec2(nodeWidth, nodeHeight)
        }
        is ListLayout -> {
            var y = 0f
            val children = node.getChildren()
            val childrenHeight = children.map {
                val size = positionNode(it, nodeWidth, null)
                it.computedSize = vec2(nodeWidth, size.y)
                size
            } .map { it.y } .sum() + (children.size - 1) * layout.spacing
            val nodeHeight = node.height ?: childrenHeight

            // TODO: compute offset vector for alignment within content area

            children.map {
                val py = when (layout.direction) {
                    FlowDirection.Default -> y
                    FlowDirection.Reversed -> nodeHeight - it.computedSize.y - y
                }

                it.computedLocalPosition = vec2(0f, py)
                y += it.computedSize.y + layout.spacing
            }

            return vec2(nodeWidth, nodeHeight)
        }
        is FillLayout -> {
            val nodeHeight = node.height ?: heightConstraint ?: 0f
            val childSize = vec2(nodeWidth, nodeHeight) - (node.margin + node.padding).totalSize()

            node.getChildren().map {
                val desiredSize = positionNode(it, childSize.x, childSize.y)
                it.computedSize = desiredSize
                it.computedLocalPosition = (vec2(nodeWidth, nodeHeight) - desiredSize) * layout.alignment
            }

            return vec2(nodeWidth, nodeHeight)
        }
        //  child-size :: k
        is FreeLayout -> TODO()
    }
}

sealed class GridLine
data class FixedGridLine(val size: Float): GridLine()
data class ProportionGridLine(val ratio: Float): GridLine()

enum class FlowDirection {
    Default,
    Reversed
}
