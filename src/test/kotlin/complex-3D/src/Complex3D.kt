import lwaf_3D.*
import lwaf_3D.poly.*
import lwaf_3D.property.*
import lwaf_core.*
import lwaf_util.noise
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL11.*

private lateinit var texture2D: GLTexture
private lateinit var context3D: DrawContext3D
private var textureToDraw = 0
private lateinit var view: GLView
private lateinit var context2D: DrawContext2D
private val objects = ArrayList<Object3D>()
private var t = 0f

fun loadModels() {
    val noTextureLight = loadResource("No texture light") { ->
        loadTextureFromInputStream(GLTexture::class.java.getResourceAsStream("/res/img/no-texture-light.png"), "no-texture-light.png")
    }

    for (i in 0..4) {
        objects.add(Sphere().toVAOObject3D(i + 1)
                .translateTo(i * 2.5f, -3f, -2f))
    }

    for (i in 0..9) {
        for (j in 0..9) {
                objects.add(Sphere(0.8f).toUVVAOObject3D(i + 3, j + 1, Material()
                                .setTexture(loadResource("/res/img/2k_earth_daymap.png") { r ->
                                    loadTextureFromInputStream(GLTexture::class.java.getResourceAsStream(r)!!, r)
                                })
                                .setSpecularLightingIntensity(0f))
                        .translateTo(18 - i * 2f, 18 - j * 2f, -5f))
        }
    }

    objects.add(Sphere(1f).toVAOObject3D(1, Material()
                    .setColour(0f, 1f, 1f))
            .translateTo(0f, 0f, 0f))

    objects.add(Box().toVAOObject3D(Material()
            .setTexture(noTextureLight))
            .translateBy(vec3(2f, 0f, 0f)))
//                .setTranslation(2f, 0f, 0f)

    objects.add(Sphere().toUVVAOObject3D(60, 40, Material()
                    .setTexture(loadResource("/res/img/2k_earth_daymap.png") { r ->
                        loadTextureFromInputStream(GLTexture::class.java.getResourceAsStream(r)!!, r)
                    }))
            .translateTo(4f, 0f, 0f))

    objects.add(Sphere().toVAOObject3D(7, Material()
                    .setColour(1f, 1f, 0f))
            .translateTo(6f, 0f, 0f))

    objects.add(VAOObject3D(LegacyConeVAO(360))
            .translateTo(-2f, 0f, 0f))

    objects.add(VAOObject3D(LegacyCylinderVAO(100))
            .translateTo(-2f, 2f, 0f))

    objects.add(VAOObject3D(LegacyPyramidVAO(4))
            .translateTo(-2f, 4f, 0f))

    objects.add(Box().toVAOObject3D().translateTo(0f, -10f, 11f))
    objects.add(Box().toVAOObject3D().translateTo(10f, -10f, 11f))

    val graph = Graph3D { v ->
        // (float) 1 / (0.1f + v.length())
        Math.sin((1 / (1 + v.length2() / 2) * 25 + 10 * v.y / 2).toDouble()).toFloat()
    }
            .setColouring { (x, y, z) -> vec3(0.5f + y * 0.5f, vec2(x, z).length(), 1f - y * 0.5f) }

    val scale = vec3(5f, 1f, 5f)
    val res = 50

    objects.add(VAOObject3D(graph.getTriangulatedVAO(false, Graph3D.UniformGridStrategy(res)))
            .translateTo(scale.x + 1, -10f, scale.x + 1).scaleTo(scale))

    objects.add(VAOObject3D(graph.getTriangulatedVAO(false, Graph3D.GradientPullStrategy(res)))
            .translateTo(scale.x + 1, -10f, -scale.x - 1).scaleTo(scale))

    objects.add(VAOObject3D(graph.getTriangulatedVAO(true, Graph3D.UniformGridStrategy(res)))
            .translateTo(-scale.x - 1, -10f, scale.x + 1).scaleTo(scale))

    objects.add(VAOObject3D(graph.getTriangulatedVAO(true, Graph3D.GradientPullStrategy(res)))
            .translateTo(-scale.x - 1, -10f, -scale.x - 1).scaleTo(scale))

//    loadResourceAsync("bugatti", { ident ->
//        ResourceWrapper(OBJModelLoader.safePreloadModel("lwaf_demo/models/bugatti/bugatti.obj"), ident)
//    }) {
//        addModel(OBJModelLoader.loadModel(it.value).setMaterial(Material()
//            .setSpecularLightingIntensity(0.1f))
//            .translateTo(20f, 0f, 10f))
//    }
//
//    loadResourceAsync("cottage_obj", { ident ->
//        ResourceWrapper(OBJModelLoader.safePreloadModel("lwaf_demo/models/cottage/cottage_obj.obj"), ident)
//    }) {
//        addModel(OBJModelLoader.loadModel(it.value)).setMaterial(Material()
//                .setSpecularLightingIntensity(0.1f)
//                .setTexture(loadResource("lwaf_demo/models/cottage/cottage_diffuse.png", ::loadTexture)))
//                .translateTo(30f, 0f, -50f)
//    }
//
//    loadResourceAsync("buildings", { ident ->
//        ResourceWrapper(OBJModelLoader.safePreloadModel("lwaf_demo/models/buildings/low poly buildings.obj"), ident)
//    }) {
//        addModel(OBJModelLoader.loadModel(it.value)).setMaterial(Material()
//                .setSpecularLightingIntensity(0.1f))
//                .translateTo(40f, 0f, 10f)
//                .scaleTo(0.005f)
//    }
//
//    loadResourceAsync("Tree1", { ident ->
//        ResourceWrapper(OBJModelLoader.safePreloadModel("lwaf_demo/models/trees/Tree1.obj"), ident)
//    }) {
//        addModel(OBJModelLoader.loadModel(it.value)).removeObject("Plane")
//                .setMaterial(Material()
//                        .setSpecularLightingIntensity(0.1f))
//                .translateTo(50f, -1f, 10f)
//    }
//
//    loadResourceAsync("stall", { ident ->
//        ResourceWrapper(OBJModelLoader.safePreloadModel("lwaf_demo/models/stall/stall.obj"), ident)
//    }) {
//        addModel(OBJModelLoader.loadModel(it.value)).setMaterial(Material()
//                .setTexture(loadResource("lwaf_demo/models/stall/stall_texture.png", ::loadTexture))
//                .setSpecularLightingIntensity(0f))
//                .translateTo(0f, 0f, 10f)
//    }

    val stall = loadResource("/res/models/stall/stall.obj") { r ->
        loadOBJModelFromInputStream(GLTexture::class.java.getResourceAsStream(r)!!, r)
    }
    stall.translateBy(vec3(0f, 0f, -30f))
    stall.objects["Cube[0]"] = stall.objects["Cube[0]"]!!.copy(second = Material().setTexture(loadResource("/res/models/stall/stall_texture.png") { r ->
        loadTextureFromInputStream(GLTexture::class.java.getResourceAsStream(r)!!, r)
    }))
    objects.add(stall)

    val deer = loadResource("/res/models/deer/deer.obj") { r ->
        loadOBJModelFromInputStream(GLTexture::class.java.getResourceAsStream(r)!!, r)
    }
    deer.translateTo(vec3(10f, 0f, 9f))
    deer.scaleBy(0.002f)
    objects.add(deer)
}

object Demo3D {
    @JvmStatic
    fun main(args: Array<String>) {
        Logging.enable("resource")

        val display = Display()
        var text = "Hello world"

        display.attachLoadCallback {
            view = GLView(vec2(0f, 0f), display.getWindowSize())
            context2D = DrawContext2D(view)
            context3D = DrawContext3D(view)
            texture2D = loadResource("No texture dark") { ->
                loadTextureFromInputStream(GLTexture::class.java.getResourceAsStream("/res/img/no-texture-dark.png")!!, "no-texture-dark.png")
            }
            context3D.camera.setPerspectiveProjection(context3D.aspectRatio)

            loadModels()
        }

        display.attachUnloadCallback {
            context3D.destroy()
            freeResources()
        }

        display.attachDrawCallback {
            val sea = Graph3D { (x, y) ->
                (2.00 * noise(x * 0.5 + t * 0.05, y * 0.5, t * 0.05)
                        + 0.50 * noise(x + t * 0.1, y.toDouble(), t * 0.1)
                        + 0.50 * noise(x * 3 + t * 0.3, (y * 3).toDouble(), t * 0.3)).toFloat()
            }

            sea.setColouring { (_, y) -> (vec3(0.3f, 0.6f, 0.9f) + vec3(1f, 1f, 1f) * y * 0.1f) }

            context3D.begin()

//            glPolygonMode( GL_FRONT_AND_BACK, GL_LINE )

            context3D.drawToGBuffer(context3D.DEFAULT_SHADER, objects)

            context3D.drawToGBuffer(context3D.DEFAULT_SHADER, VAOObject3D(sea.getTriangulatedVAO(true, Graph3D.UniformGridStrategy(50)))
                    .translateTo(50f, -10f, 0f)
                    .scaleTo(40f, 1f, 40f))

            glPolygonMode( GL_FRONT_AND_BACK, GL_FILL )

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
            context2D.write(text, Font.DEFAULT_FONT, vec2(0f, 0f))

            if (::texture2D.isInitialized) context2D.drawImage(texture2D, vec2(100f), vec2(0.1f))
        }

        display.attachUpdateCallback { dt ->
            var translation = context3D.camera.translation
            val forward = context3D.camera.flatForward
            val right = context3D.camera.flatRight
            val speed = dt * 5

            finaliseQueuedResources()

            t += dt * 1.2f

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
        }

        display.run()
    }
}
