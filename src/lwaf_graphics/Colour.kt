import lwaf_core.vec3

object Colour {
    val black = vec3(0f)
    val darkGrey = vec3(0.2f)
    val grey = vec3(0.4f)
    val lightGrey = vec3(0.7f)
    val white = vec3(1f)

    val brightRed = vec3(1f, 0f, 0f)
    val brightGreen = vec3(0f, 1f, 0f)
    val brightBlue = vec3(0f, 0f, 1f)

    // shamelessly stolen from material colours (https://materialuicolors.co/)
    val red = rgb(244, 67, 54)
    val pink = rgb(233, 30, 99)
    val purple = rgb(156, 39, 176)
    val deepPurple = rgb(103, 58, 183)
    val indigo = rgb(63, 81, 181)
    val blue = rgb(33, 150, 243)
    val lightBlue = rgb(3, 169, 244)
    val cyan = rgb(0, 188, 212)
    val teal = rgb(0, 150, 136)
    val green = rgb(76, 175, 80)
    val lightGreen = rgb(139, 195, 74)
    val lime = rgb(205, 220, 57)
    val yellow = rgb(255, 235, 59)
    val amber = rgb(255, 193, 7)
    val orange = rgb(255, 152, 0)
    val deepOrange = rgb(255, 87, 34)
    val brown = rgb(121, 85, 72)
    val blueGrey = rgb(96, 125, 139)
}

private fun rgb(r: Int, g: Int, b: Int) = vec3(r / 255f, g / 255f, b / 255f)
