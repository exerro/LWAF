package lwaf_core

import org.lwjgl.opengl.GL11.glViewport

class GLView(val offset: vec2, val size: vec2) {
    constructor(size: vec2): this(vec2(0f), size)

    fun setViewport() {
        glViewport(offset.x.toInt(), -offset.y.toInt(), size.x.toInt(), size.y.toInt())
    }
}
