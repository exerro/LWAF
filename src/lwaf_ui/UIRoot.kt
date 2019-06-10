
class UIRoot {
    private lateinit var node: UINode

    val nodes: List<UINode>
        get() = listOf(node) + node.getAllChildren()

    fun setRoot(node: UINode) {
        if (this::node.isInitialized) {
            this.node.root = null
        }
        this.node = node
        node.root = this
    }
}
