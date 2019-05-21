package lwaf_demo

import lwaf_3D.*
import lwaf_core.*
import org.lwjgl.glfw.GLFW.*

private lateinit var scene: Scene
private lateinit var texture: GLTexture
private lateinit var text: FontText
private lateinit var font: Font
private lateinit var models: Models
private lateinit var shader: GLShaderProgram
private lateinit var renderer: Renderer
private var textureToDraw = 0
private lateinit var view: GLView
private lateinit var context2D: DrawContext2D
private lateinit var display: Display

object Demo3D {
    @JvmStatic
    fun main(args: Array<String>) {
        display = Display()
        display.attachLoadCallback {
            font = loadFont("lwaf_res/font/open-sans/OpenSans-Regular.fnt")
            text = FontText("!\"£$%^&*()_+-={}[]:@~;'#<>?,./`¬¦\\|", font)
            texture = loadTexture("lwaf_res/img/no-texture-dark.png")
            view = GLView(vec2(0f, 0f), display.getWindowSize())
            context2D = DrawContext2D(view)

            shader = GBuffer.safeLoadGeometryShader(
                    "lwaf_3D/shader/vertex-3D.glsl",
                    false
            )

            models = Models(DrawContext3D(GLView(vec2(0f, 0f), display.getWindowSize())))

            scene = object : Scene() {
                init {
                    addLight(Light.AmbientLight(0.05f))
                    addLight(Light.DirectionalLight(0.4f, vec3(-1f, -1f, -1f), vec3(0f, 1f, 0f)))
                    addLight(Light.DirectionalLight(0.4f, vec3(0f, 1f, 0f), vec3(1f, 0f, 0f)))
                    addLight(Light.DirectionalLight(0.4f, vec3(0f, -1f, 0f)))
                    addLight(Light.PointLight(10f, vec3(0f, 4f, 10f), Light.PointLight.attenuation(25f), vec3(1f, 1f, 0f)))
                }

                override fun drawObjects(viewMatrix: mat4, projectionMatrix: mat4) {
                    shader.setUniform("viewTransform", viewMatrix)
                    shader.setUniform("projectionTransform", projectionMatrix)
                    shader.start()
                    models.draw(shader)
                    shader.stop()
                }
            }

            renderer = Renderer(1200, 680)

            scene.getCamera().setPerspectiveProjection(
                    renderer.getAspectRatio(),
                    Camera.PerspectiveProjection.DEFAULT_FOV,
                    Camera.PerspectiveProjection.DEFAULT_NEAR,
                    Camera.PerspectiveProjection.DEFAULT_FAR
            )
        }

        display.attachUnloadCallback {
            renderer.destroy()
            texture.destroy()
            shader.destroy()
        }

        display.attachDrawCallback {
            renderer.draw(scene)

            // Draw2D.view(view, new vec2f(40, 20));
            // Draw2D.buffer(renderer.getGBuffer(), new vec2f(40, 20), vec2f.one);

            val scale = display.getWindowSize().div(vec2(1080f, 720f))
            val texture = when (textureToDraw) {
                0 -> renderer.texture
                1 -> renderer.gBuffer.colourTexture
                2 -> renderer.gBuffer.normalTexture
                3 -> renderer.gBuffer.positionTexture
                4 -> renderer.gBuffer.lightingTexture
                else -> error("oh")
            }

            context2D.setColour(1f, 1f, 1f)
            context2D.drawTexture(texture, vec2(0f, 0f), scale)
            context2D.write(text.text, font, vec2(0f, 0f))
        }

        display.attachUpdateCallback { dt ->
            var translation = scene.camera.translation
            var rotation = scene.camera.rotation
            val forward = scene.camera.flatForward
            val right = scene.camera.flatRight
            val speed = dt * 5
            val rspeed = dt * Math.PI.toFloat() / 2

            text = FontText(display.getMousePosition().toString(), font)

            if (display.isKeyDown(GLFW_KEY_A)) {
                translation = translation.sub(right.mul(speed))
            }
            if (display.isKeyDown(GLFW_KEY_D)) {
                translation = translation.add(right.mul(speed))
            }

            if (display.isKeyDown(GLFW_KEY_W)) {
                translation = translation.add(forward.mul(speed))
            }
            if (display.isKeyDown(GLFW_KEY_S)) {
                translation = translation.sub(forward.mul(speed))
            }

            if (display.isKeyDown(GLFW_KEY_SPACE)) {
                translation = translation.add(vec3(0f, speed, 0f))
            }
            if (display.isKeyDown(GLFW_KEY_LEFT_SHIFT)) {
                translation = translation.sub(vec3(0f, speed, 0f))
            }

            if (display.isKeyDown(GLFW_KEY_LEFT)) {
                rotation = rotation.add(vec3(0f, rspeed, 0f))
            }
            if (display.isKeyDown(GLFW_KEY_RIGHT)) {
                rotation = rotation.sub(vec3(0f, rspeed, 0f))
            }

            if (display.isKeyDown(GLFW_KEY_UP)) {
                rotation = rotation.add(vec3(rspeed, 0f, 0f))
            }
            if (display.isKeyDown(GLFW_KEY_DOWN)) {
                rotation = rotation.sub(vec3(rspeed, 0f, 0f))
            }

            scene.camera.translation = translation
            scene.camera.rotation = rotation
        }

        display.attachMouseDownCallback { _, _ ->
            display.setMouseLocked(true)
        }

        display.attachMouseUpCallback { _, _ ->
            if (!display.isMouseDown()) display.setMouseLocked(false)
        }

        display.attachMouseDragCallback { pos, last, _, _ ->
            val dx = pos.x - last.x
            val dy = pos.y - last.y
            scene.camera.rotation = scene.camera.rotation.add(vec3(0f, -dx / display.getWindowSize().x * 0.5f, 0f))
            scene.camera.rotation = scene.camera.rotation.add(vec3(-dy / display.getWindowSize().y * 0.5f, 0f, 0f))
        }

        display.attachKeyPressedCallback { key, modifier ->
            if (key == GLFW_KEY_TAB) {
                textureToDraw = if (modifier and GLFW_MOD_CONTROL != 0) {
                    (textureToDraw + 1) % 5
                } else {
                    (textureToDraw - 1) % 5
                }
            }

            if (textureToDraw < 0) textureToDraw += 5
        }

        display.attachResizedCallback { w, h ->
            view.size = vec2(w.toFloat(), h.toFloat())
            Unit
        }

        display.run()
    }
}
