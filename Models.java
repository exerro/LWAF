import lwaf.*;
import lwaf_3D.Material;
import lwaf_graph.Graph3D;
import lwaf_math.SimplexNoise;
import lwaf_model.Model;
import lwaf_model.ModelLoader;
import lwaf_model.ModelRenderer;
import lwaf_primitive.*;

class Models {
    public final ModelRenderer models = new ModelRenderer();

    public Models() {
        var dark_texture = Texture.load("lwaf/img/no-texture-light.png");

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
                        .setTexture(Texture.load("lwaf/img/no-texture-light.png")))
                .setTranslation(2, 0, 0);

        models.add(new Model<>(new UVSphereVAO(40, 80)))
                .setMaterial(new Material()
                        .setTexture(Texture.load("lwaf/img/no-texture-dark.png")))
                .setTranslation(4, 0, 0);

        models.add(new Model<>(new IcoSphereVAO(7)))
                .setMaterial(new Material()
                        .setColour(1, 1, 0))
                .setTranslation(6, 0, 0);

        models.add(ModelLoader.safeLoad("stall.obj"))
                .setMaterial(new Material()
                        .setTexture(Texture.load("stall_texture.png"))
                        .setSpecularLightingIntensity(0))
                .setTranslation(0, 0, 10)
        ;

        models.add(ModelLoader.safeLoad("deer.obj"))
                .setTranslation(10, 0, 10)
                .setMaterial(new Material()
                        .setSpecularLightingIntensity(0.1f))
                .scaleBy(0.002f)
        ;

        models.add(ModelLoader.safeLoad("bugatti.obj"))
                .setMaterial(new Material()
                        .setSpecularLightingIntensity(0.1f))
                .setTranslation(20, 0, 10)
        ;

        models.add(ModelLoader.safeLoad("cottage_obj.obj"))
                .setMaterial(new Material()
                        .setSpecularLightingIntensity(0.1f))
                .setTranslation(20, 0, 10)
        ;

        var graph = new Graph3D(v -> (float) (
                // (float) 1 / (0.1f + v.length())
                Math.sin(1 / (1 + v.length2() / 2) * 25 + 10 * v.y / 2)
        ))
                .setColouring(v -> new vec3f(0.5f + v.y * 0.5f, new vec2f(v.x, v.z).length(), 1f - v.y * 0.5f))
                ;

        var scale = new vec3f(20, 1f, 20);
        var res = 50;

        models.add(new Model<>(graph.getTriangulatedVAO(new Graph3D.UniformGridStrategy(res))))
                .setTranslation(0, -10, scale.x / 2 + 1)
                .setScale(scale)
        ;

        models.add(new Model<>(new CubeVAO())).setTranslation(0, -10, scale.x / 2 + 1);
        models.add(new Model<>(new CubeVAO())).setTranslation(scale.x * 0.5f, -10, scale.x / 2 + 1);

        models.add(new Model<>(graph.getTriangulatedVAO(new Graph3D.GradientPullStrategy(res))))
                .setTranslation(0, -10, -scale.x / 2 - 1)
                .setScale(scale)
        ;

        models.add(new Model<>(graph.getSmoothVAO(new Graph3D.UniformGridStrategy(res))))
                .setTranslation(-scale.x - 1, -10, scale.x / 2 + 1)
                .setScale(scale)
        ;

        models.add(new Model<>(graph.getSmoothVAO(new Graph3D.GradientPullStrategy(res))))
                .setTranslation(-scale.x - 1, -10, -scale.x / 2 - 1)
                .setScale(scale)
        ;

        models.add(new Model<>(new ConeVAO(360)))
                .setTranslation(-2, 0, 0);

        models.add(new Model<>(new CylinderVAO(100)))
                .setTranslation(-2, 2, 0);

        models.add(new Model<>(new PyramidVAO(4)))
                .setTranslation(-2, 4, 0);
    }

    public void draw(ShaderLoader.Program shader) {
        var t = Application.getActive().getTime();

        models.draw(shader);

        var sea = new Graph3D(v -> {
            return (float) (
                    2.00 * SimplexNoise.noise(v.x * 0.5 + t * 0.05, v.y * 0.5, t * 0.05)
                            + 0.50 * SimplexNoise.noise(v.x + t * 0.1, v.y, t * 0.1)
                            + 0.50 * SimplexNoise.noise(v.x * 3 + t * 0.3, v.y * 3, t * 0.3)
            );
        })
                .setColouring(v -> new vec3f(0.3f, 0.6f, 0.9f).add(vec3f.one.mul(v.y * 0.1f)))
                ;

        new Model<>(sea.getSmoothVAO(new Graph3D.UniformGridStrategy(50)))
                .setTranslation(50, -10, 0)
                .setScale(40, 1, 40)
                .draw(shader)
        ;
    }
}
