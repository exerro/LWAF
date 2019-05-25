package lwaf_demo

import lwaf_3D.*
import lwaf_3D.property.moveTo
import lwaf_3D.property.rotateBy
import lwaf_core.*
import org.lwjgl.glfw.GLFW.*

private lateinit var texture: GLTexture
private lateinit var font: Font
private lateinit var models: Models
private lateinit var shader: GLShaderProgram
private lateinit var context3D: DrawContext3D
private var textureToDraw = 0
private lateinit var view: GLView
private lateinit var context2D: DrawContext2D

object Demo3D {
    @JvmStatic
    fun main(args: Array<String>) {
        Logging.enable("shader.compile.notice")
        Logging.enable("texture")
        Logging.enable("framebuffer.create")
        Logging.enable("font.load")

        val display = Display()
        var text = "Hello world"

        display.attachLoadCallback {
            font = loadFont("lwaf_res/font/open-sans/OpenSans-Regular.fnt")
            texture = loadTexture("lwaf_res/img/no-texture-dark.png")
            view = GLView(vec2(0f, 0f), display.getWindowSize())
            context2D = DrawContext2D(view)
            context3D = DrawContext3D(view)
            models = Models(context3D)

            shader = GBuffer.loadShader(
                    "lwaf_res/shader/vertex-3D.glsl",
                    false
            )

            context3D.camera.setPerspectiveProjection(context3D.aspectRatio)
        }

        display.attachUnloadCallback {
            context3D.destroy()
            texture.destroy()
            shader.destroy()
        }

        display.attachDrawCallback {
            context3D.begin()

            context3D.drawObjects(shader, object: Object3D() {
                override val translation: vec3 = vec3(0f)
                override val rotation: vec3 = vec3(0f)
                override val scale: vec3 = vec3(0f)

                override fun draw(context: DrawContext3D, shader: GLShaderProgram) {
                    models.draw(shader)
                }
            })

            context3D.ambientLight(0.2f)
            context3D.directionalLight(vec3(-1f, -1f, -1f), 0.4f, vec3(0f, 1f, 0f))
            context3D.directionalLight(vec3(0f, 1f, 0f), 0.4f, vec3(1f, 0f, 0f))
            context3D.directionalLight(vec3(0f, -1f, 0f), 0.4f)
            context3D.pointLight(vec3(0f, 4f, 10f), 4.8f, getLightingAttenuation(25f), vec3(1f, 1f, 0f))
            context3D.spotLight(
                    vec3(-6f, -7f, -1f),
                    vec3(-3f, -1f, 2f).normalise(),
                    10f,
                    vec2(0.4f, 0.5f),
                    vec3(1f, 0.09f, 0.032f),
                    vec3(0f, 1f, 1f)
            )
            context3D.spotLight(
                    vec3(0f, -5f, 11f),
                    vec3(0f, -1f, 0f),
                    5f,
                    vec2(0.5f, 0.8f),
                    vec3(1f, 0.09f, 0.032f)
            )

            // Draw2D.view(view, new vec2f(40, 20));
            // Draw2D.buffer(context3D.getGBuffer(), new vec2f(40, 20), vec2f.one);

            val scale = vec2(1f, 1f)
            val texture = when (textureToDraw) {
                0 -> context3D.texture
                1 -> context3D.buffer.colourTexture
                2 -> context3D.buffer.normalTexture
                3 -> context3D.buffer.positionTexture
                4 -> context3D.buffer.lightingTexture
                else -> error("oh")
            }

            context2D.setColour(1f, 1f, 1f)
            context2D.drawTexture(texture, vec2(0f, 0f), scale)
            context2D.write(text, font, vec2(0f, 0f))
            context2D.drawImage(lwaf_demo.texture, vec2(100f), vec2(0.1f))
        }

        display.attachUpdateCallback { dt ->
            var translation = context3D.camera.translation
            val forward = context3D.camera.flatForward
            val right = context3D.camera.flatRight
            val speed = dt * 5

            if (display.isKeyDown(GLFW_KEY_A)) {
                translation -= right * speed
            }
            if (display.isKeyDown(GLFW_KEY_D)) {
                translation += right * speed
            }

            if (display.isKeyDown(GLFW_KEY_W)) {
                translation += forward * speed
            }
            if (display.isKeyDown(GLFW_KEY_S)) {
                translation -= forward * speed
            }

            if (display.isKeyDown(GLFW_KEY_SPACE)) {
                translation += vec3(0f, speed, 0f)
            }
            if (display.isKeyDown(GLFW_KEY_LEFT_SHIFT)) {
                translation -= vec3(0f, speed, 0f)
            }

            text = display.fps.toString()
            context3D.camera.moveTo(translation)
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
            context3D.camera.rotateBy(vec3(0f, -dx / display.getWindowSize().x * 0.5f, 0f))
            context3D.camera.rotateBy(vec3(-dy / display.getWindowSize().y * 0.5f, 0f, 0f))
        }

        display.attachKeyPressedCallback { key, modifier ->
            if (key == GLFW_KEY_TAB) {
                textureToDraw = if (modifier and GLFW_MOD_CONTROL != 0) {
                    (textureToDraw - 1) % 5
                } else {
                    (textureToDraw + 1) % 5
                }
            }

            if (textureToDraw < 0) textureToDraw += 5
        }

        display.attachResizedCallback { w, h ->
            view = GLView(view.offset, vec2(w.toFloat(), h.toFloat()))
            context2D = DrawContext2D(view)
            context3D = DrawContext3D(view, context3D.camera)
            context3D.camera.setPerspectiveProjection(context3D.aspectRatio)
            models = Models(context3D)
        }

        display.run()
    }
}
