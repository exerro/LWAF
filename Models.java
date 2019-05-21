import lwaf_3D.Material;
import lwaf_core.*;
import lwaf_graph.Graph3D;
import lwaf_math.SimplexNoise;
import lwaf_model.Model;
import lwaf_model.ModelRenderer;
import lwaf_model.OBJModelLoader;
import lwaf_primitive.*;

class Models {
    public final ModelRenderer models = new ModelRenderer();
    private final DrawContext3D context;

    public Models(DrawContext3D context) {
        this.context = context;
        var dark_texture = GLTextureKt.loadTexture("lwaf_res/img/no-texture-light.png");

        for (int i = 0; i < 5; ++i) {
            models.add(new Model<>(new IcoSphereVAO(i + 1)))
                    .setTranslation(i * 2, -3, -2);
        }

        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 10; ++j) {
                models.add(new Model<>(new UVSphereVAO(j + 1, i + 3)))
                        .setTranslation(i * 2, 3 + j * 2, -2)
                        .setMaterial(new Material()
                                .setTexture(dark_texture)
                                .setSpecularLightingIntensity(0)
                        )
                ;
            }
        }

        models.add(new Model<>(new IcoSphereVAO(1)))
                .setMaterial(new Material()
                        .setColour(0, 1, 1))
                .setTranslation(0, 0, 0);

        models.add(new Model<>(new CubeVAO()))
                .setMaterial(new Material()
                        .setTexture(GLTextureKt.loadTexture("lwaf_res/img/no-texture-light.png")))
                .setTranslation(2, 0, 0);

        models.add(new Model<>(new UVSphereVAO(40, 80)))
                .setMaterial(new Material()
                        .setTexture(GLTextureKt.loadTexture("lwaf_res/img/no-texture-dark.png")))
                .setTranslation(4, 0, 0);

        models.add(new Model<>(new IcoSphereVAO(7)))
                .setMaterial(new Material()
                        .setColour(1, 1, 0))
                .setTranslation(6, 0, 0);

        models.add(OBJModelLoader.safeLoadModel("models/stall/stall.obj"))
                .setMaterial(new Material()
                        .setTexture(GLTextureKt.loadTexture("models/stall/stall_texture.png"))
                        .setSpecularLightingIntensity(0))
                .setTranslation(0, 0, 10)
        ;

        models.add(OBJModelLoader.safeLoadModel("models/deer/deer.obj"))
                .setTranslation(10, 0, 10)
                .setMaterial(new Material()
                        .setSpecularLightingIntensity(0.1f))
                .scaleBy(0.002f)
        ;

//        models.add(OBJModelLoader.safeLoadModel("models/bugatti/bugatti.obj"))
//                .setMaterial(new Material()
//                        .setSpecularLightingIntensity(0.1f))
//                .setTranslation(20, 0, 10)
//        ;
//
//        models.add(OBJModelLoader.safeLoadModel("models/cottage/cottage_obj.obj"))
//                .setMaterial(new Material()
//                        .setSpecularLightingIntensity(0.1f)
//                        .setTexture(Texture.load("models/cottage/cottage_diffuse.png")))
//                .setTranslation(30, 0, 10)
//        ;
//
//        models.add(OBJModelLoader.safeLoadModel("models/buildings/low poly buildings.obj"))
//                .setMaterial(new Material()
//                        .setSpecularLightingIntensity(0.1f))
//                .setTranslation(40, 0, 10)
//                .setScale(0.005f)
//        ;
//
//        models.add(OBJModelLoader.safeLoadModel("models/trees/Tree1.obj"))
//                .removeObject("Plane")
//                .setMaterial(new Material()
//                        .setSpecularLightingIntensity(0.1f))
//                .setTranslation(50, -1, 10)
//        ;

        var graph = new Graph3D(v -> (float) (
                // (float) 1 / (0.1f + v.length())
                Math.sin(1 / (1 + v.length2() / 2) * 25 + 10 * v.getY() / 2)
        ))
                .setColouring(v -> new vec3(0.5f + v.getY() * 0.5f, new vec2(v.getX(), v.getZ()).length(), 1f - v.getY() * 0.5f))
                ;

        var scale = new vec3(20, 1f, 20);
        var res = 50;

        models.add(new Model<>(graph.getTriangulatedVAO(new Graph3D.UniformGridStrategy(res))))
                .setTranslation(0, -10, scale.getX() / 2 + 1)
                .setScale(scale)
        ;

        models.add(new Model<>(new CubeVAO())).setTranslation(0, -10, scale.getX() / 2 + 1);
        models.add(new Model<>(new CubeVAO())).setTranslation(scale.getX() * 0.5f, -10, scale.getX() / 2 + 1);

        models.add(new Model<>(graph.getTriangulatedVAO(new Graph3D.GradientPullStrategy(res))))
                .setTranslation(0, -10, -scale.getX() / 2 - 1)
                .setScale(scale)
        ;

        models.add(new Model<>(graph.getSmoothVAO(new Graph3D.UniformGridStrategy(res))))
                .setTranslation(-scale.getX() - 1, -10, scale.getX() / 2 + 1)
                .setScale(scale)
        ;

        models.add(new Model<>(graph.getSmoothVAO(new Graph3D.GradientPullStrategy(res))))
                .setTranslation(-scale.getX() - 1, -10, -scale.getX() / 2 - 1)
                .setScale(scale)
        ;

        models.add(new Model<>(new ConeVAO(360)))
                .setTranslation(-2, 0, 0);

        models.add(new Model<>(new CylinderVAO(100)))
                .setTranslation(-2, 2, 0);

        models.add(new Model<>(new PyramidVAO(4)))
                .setTranslation(-2, 4, 0);
    }

    public void draw(GLShaderProgram shader) {
        var t = System.currentTimeMillis();

        models.draw(shader, context);

        var sea = new Graph3D(v -> (float) (
                2.00 * SimplexNoise.noise(v.getX() * 0.5 + t * 0.05, v.getY() * 0.5, t * 0.05)
                        + 0.50 * SimplexNoise.noise(v.getX() + t * 0.1, v.getY(), t * 0.1)
                        + 0.50 * SimplexNoise.noise(v.getX() * 3 + t * 0.3, v.getY() * 3, t * 0.3)
        ))
                .setColouring(v -> new vec3(0.3f, 0.6f, 0.9f).add(new vec3(1f, 1f, 1f).mul(v.getY() * 0.1f)))
                ;

        new Model<>(sea.getSmoothVAO(new Graph3D.UniformGridStrategy(50)))
                .setTranslation(50, -10, 0)
                .setScale(40, 1, 40)
                .draw(shader, context)
        ;
    }
}
