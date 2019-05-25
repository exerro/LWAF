package lwaf_demo

import lwaf_3D.DrawContext3D
import lwaf_3D.Material
import lwaf_3D.property.rotateBy
import lwaf_3D.property.scaleBy
import lwaf_3D.property.scaleTo
import lwaf_3D.property.translateTo
import lwaf_core.*
import lwaf_graph.Graph3D
import lwaf_math.noise
import lwaf_model.Model
import lwaf_model.OBJModelLoader
import lwaf_primitive.*
import java.util.*

class Models(private val context: DrawContext3D) {
    private val models = ArrayList<Model<*>>()

    private fun <T: GLVAO> add(model: Model<T>): Model<T> {
        models.add(model)
        return model
    }

    init {
        val tx = loadTexture("lwaf_demo/2k_earth_daymap.png")

        for (i in 0..4) {
            add(Model(IcoSphereVAO(i + 1)))
                    .translateTo((i * 2).toFloat(), -3f, -2f)
        }

        for (i in 0..9) {
            for (j in 0..9) {
                add(Model(UVSphereVAO(j + 1, i + 3)))
                        .translateTo((18 - i * 2).toFloat(), (21 - 3 - j * 2).toFloat(), -2f).setMaterial(Material()
                        .setTexture(tx)
                        .setSpecularLightingIntensity(0f))
            }
        }

        add(Model(IcoSphereVAO(1)))
                .setMaterial(Material()
                        .setColour(0f, 1f, 1f))
                .translateTo(0f, 0f, 0f)

        add(Model(CubeVAO()))
                .setMaterial(Material()
                        .setTexture(loadTexture("lwaf_res/img/no-texture-light.png")))
//                .setTranslation(2f, 0f, 0f)

        add(Model(UVSphereVAO(40, 80)))
                .setMaterial(Material()
                        .setTexture(loadTexture("lwaf_demo/2k_earth_daymap.png")))
                .translateTo(4f, 0f, 0f)

        add(Model(IcoSphereVAO(7)))
                .setMaterial(Material()
                        .setColour(1f, 1f, 0f))
                .translateTo(6f, 0f, 0f)

        add(OBJModelLoader.safeLoadModel("lwaf_demo/models/stall/stall.obj"))
                .setMaterial(Material()
                        .setTexture(loadTexture("lwaf_demo/models/stall/stall_texture.png"))
                        .setSpecularLightingIntensity(0f))
                .translateTo(0f, 0f, 10f)

        add(OBJModelLoader.safeLoadModel("lwaf_demo/models/deer/deer.obj"))
                .translateTo(10f, 0f, 10f)
                .setMaterial(Material()
                        .setSpecularLightingIntensity(0.1f))
                .rotateBy(0f, 3.14159f, 0f)
                .scaleBy(0.002f)

        //        models.add(OBJModelLoader.safeLoadModel("lwaf_demo/models/bugatti/bugatti.obj"))
        //                .setMaterial(new Material()
        //                        .setSpecularLightingIntensity(0.1f))
        //                .setTranslation(20, 0, 10)
        //        ;
        //
        //        models.add(OBJModelLoader.safeLoadModel("lwaf_demo/models/cottage/cottage_obj.obj"))
        //                .setMaterial(new Material()
        //                        .setSpecularLightingIntensity(0.1f)
        //                        .setTexture(Texture.load("lwaf_demo/models/cottage/cottage_diffuse.png")))
        //                .setTranslation(30, 0, 10)
        //        ;
        //
        //        models.add(OBJModelLoader.safeLoadModel("lwaf_demo/models/buildings/low poly buildings.obj"))
        //                .setMaterial(new Material()
        //                        .setSpecularLightingIntensity(0.1f))
        //                .setTranslation(40, 0, 10)
        //                .setScale(0.005f)
        //        ;
        //
        //        models.add(OBJModelLoader.safeLoadModel("lwaf_demo/models/trees/Tree1.obj"))
        //                .removeObject("Plane")
        //                .setMaterial(new Material()
        //                        .setSpecularLightingIntensity(0.1f))
        //                .setTranslation(50, -1, 10)
        //        ;

        val graph = Graph3D { v ->
            // (float) 1 / (0.1f + v.length())
            Math.sin((1 / (1 + v.length2() / 2) * 25 + 10 * v.y / 2).toDouble()).toFloat()
        }
                .setColouring { (x, y, z) -> vec3(0.5f + y * 0.5f, vec2(x, z).length(), 1f - y * 0.5f) }

        val scale = vec3(20f, 1f, 20f)
        val res = 50

        add(Model(graph.getTriangulatedVAO(Graph3D.UniformGridStrategy(res))))
                .translateTo(0f, -10f, scale.x / 2 + 1).scaleTo(scale)

        add(Model(CubeVAO())).translateTo(0f, -10f, scale.x / 2 + 1)
        add(Model(CubeVAO())).translateTo(scale.x * 0.5f, -10f, scale.x / 2 + 1)

        add(Model(graph.getTriangulatedVAO(Graph3D.GradientPullStrategy(res))))
                .translateTo(0f, -10f, -scale.x / 2 - 1).scaleTo(scale)

        add(Model(graph.getSmoothVAO(Graph3D.UniformGridStrategy(res))))
                .translateTo(-scale.x - 1, -10f, scale.x / 2 + 1).scaleTo(scale)

        add(Model(graph.getSmoothVAO(Graph3D.GradientPullStrategy(res))))
                .translateTo(-scale.x - 1, -10f, -scale.x / 2 - 1).scaleTo(scale)

        add(Model(ConeVAO(360)))
                .translateTo(-2f, 0f, 0f)

        add(Model(CylinderVAO(100)))
                .translateTo(-2f, 2f, 0f)

        add(Model(PyramidVAO(4)))
                .translateTo(-2f, 4f, 0f)
    }

    fun draw(shader: GLShaderProgram) {
        val t = System.currentTimeMillis()

        for (model in models) {
            model.draw(shader, context)
        }

        val sea = Graph3D { (x, y) ->
            (2.00 * noise(x * 0.5 + t * 0.05, y * 0.5, t * 0.05)
                    + 0.50 * noise(x + t * 0.1, y.toDouble(), t * 0.1)
                    + 0.50 * noise(x * 3 + t * 0.3, (y * 3).toDouble(), t * 0.3)).toFloat()
        }
                .setColouring { (_, y) -> (vec3(0.3f, 0.6f, 0.9f) + vec3(1f, 1f, 1f) * y * 0.1f) }

        Model(sea.getSmoothVAO(Graph3D.UniformGridStrategy(50)))
                .translateTo(50f, -10f, 0f)
                .scaleTo(40f, 1f, 40f)
                .draw(shader, context)
    }
}
