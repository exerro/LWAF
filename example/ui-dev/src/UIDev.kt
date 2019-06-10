
fun main() {
    val b = Button()

    b.onPropertyChanged(b::text) { old, new ->
        println("Button value changed from '$old' to '$new'")
    }

    b.text = "new text"
    b.text = "newer text"

    println(b.properties.map { "$it => ${it.value}" } .joinToString("\n") { it })
}
