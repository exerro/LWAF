package lwaf_core

import org.lwjgl.opengl.GL11.glViewport

class GLView(val offset: vec2 = vec2(0f, 0f), val size: vec2) {
    fun setViewport() {
        glViewport(offset.x.toInt(), offset.y.toInt(), size.x.toInt(), size.y.toInt())
    }
}
