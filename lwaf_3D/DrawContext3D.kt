package lwaf_3D

import lwaf_core.GLVAO
import lwaf_core.GLView
import org.lwjgl.opengl.GL11

open class DrawContext3D(protected val view: GLView) {
    fun drawIndexedVAO(vao: GLVAO) {
        view.setViewport()
        vao.load()
        GL11.glDrawElements(GL11.GL_TRIANGLES, vao.vertexCount, GL11.GL_UNSIGNED_INT, 0)
        vao.unload()
    }
}
