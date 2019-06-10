import lwaf_core.vec2

open class Button: UINode() {
    open var text by property("text", this, "")
    open val size by readOnlyProperty("size", vec2(3f))
    open val area by computedProperty("area", this) { size.x * size.y }

    val x: String = "hi"
}

fun main() {
    val root = UIRoot()
    val b = Button()
    val bb = Button()

    b.addChild(bb)

    root.setRoot(b)

    println(root.nodes)
}
