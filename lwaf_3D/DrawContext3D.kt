package lwaf_3D

import lwaf_core.*
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14
import org.lwjgl.opengl.GL30

open class DrawContext3D(
        val view: GLView,
        val camera: Camera = Camera()
): GLResource {
    private val framebuffer = GLFramebuffer(view.size.x.toInt(), view.size.y.toInt())
    val buffer = GBuffer(view.size.x.toInt(), view.size.y.toInt())
    val texture = createEmptyTexture(view.size.x.toInt(), view.size.y.toInt())
    val aspectRatio = view.size.x / view.size.y

    init {
        framebuffer.attachTexture(texture, GL30.GL_COLOR_ATTACHMENT0)
        framebuffer.setDrawBuffers(GL30.GL_COLOR_ATTACHMENT0)
    }

    fun begin() {
        buffer.bind()
        GL11.glDepthMask(true)
        GL11.glClearColor(0f, 0f, 0f, 0.0f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
        buffer.unbind()

        framebuffer.bind()
        GL11.glClearColor(0f, 0f, 0f, 1f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)
        framebuffer.unbind()
    }

    fun drawObjects(shader: GLShaderProgram, objects: List<Object3D>) {
        val viewMatrix = camera.viewMatrix
        val projectionMatrix = camera.projectionMatrix

        buffer.bind()
        view.setViewport()

        GL11.glEnable(GL11.GL_CULL_FACE)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glDepthMask(true)

        shader.start()
        shader.setUniform("projectionTransform", projectionMatrix)
        shader.setUniform("viewTransform", viewMatrix)
        shader.setUniform("screenSize", view.size)

        for (o in objects) o.draw(this, shader)

        shader.stop()
        buffer.unbind()
    }

    fun drawObjects(shader: GLShaderProgram, vararg objects: Object3D) {
        drawObjects(shader, objects.toList())
    }

    fun render(vararg renderers: () -> Unit) {
        framebuffer.bind()
        buffer.bindReading()

        GL11.glDepthMask(false)
        GL11.glDisable(GL11.GL_CULL_FACE)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_BLEND)
        GL14.glBlendEquation(GL14.GL_FUNC_ADD)
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE)

        for (renderer in renderers) renderer()

        framebuffer.unbind()
        buffer.unbindReading()

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    }

    fun light(light: Light) {
        render({
            light.shader.start()
            light.shader.setUniform("screenSize", view.size)
            light.shader.setUniform("viewTransform", camera.viewMatrix)
            light.shader.setUniform("projectionTransform", camera.projectionMatrix)
            light.render(light.shader, this)
            light.shader.stop()
        })
    }

    fun directionalLight(direction: vec3, intensity: Float = 0.4f, colour: vec3 = vec3(1f)) {
        light(DirectionalLight(direction, intensity, colour))
    }

    fun ambientLight(intensity: Float = 0.1f, colour: vec3 = vec3(1f)) {
        light(AmbientLight(intensity, colour))
    }

    fun pointLight(position: vec3, intensity: Float = 4f, attenuation: vec3 = PointLight.ATTENUATION, colour: vec3 = vec3(1f)) {
        light(PointLight(position, intensity, attenuation, colour))
    }

    fun spotLight(position: vec3, direction: vec3, intensity: Float = 4f, spread: vec2 = vec2(0.5f, 0.6f), attenuation: vec3 = PointLight.ATTENUATION, colour: vec3 = vec3(1f)) {
        light(SpotLight(position, direction, spread, intensity, attenuation, colour))
    }

    fun drawIndexedVAO(vao: GLVAO) {
        view.setViewport()
        vao.load()
        GL11.glDrawElements(GL11.GL_TRIANGLES, vao.vertexCount, GL11.GL_UNSIGNED_INT, 0)
        vao.unload()
    }

    override fun destroy() {
        buffer.destroy()
        framebuffer.destroy()
        texture.destroy()
    }
}
