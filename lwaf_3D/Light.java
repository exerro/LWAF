package lwaf_3D;

import lwaf.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class Light {

    public static final Map<Class<? extends Light>, ShaderLoader.Program> shaders = new HashMap<>();

    public static ShaderLoader.Program loadShader(Class<? extends Light> name, Supplier<ShaderLoader.Program> generator) {
        shaders.computeIfAbsent(name, ignored -> {
            var shader = generator.get();

            shader.setUniform("colourMap", 0);
            shader.setUniform("positionMap", 1);
            shader.setUniform("normalMap", 2);
            shader.setUniform("lightingMap", 3);

            return shader;
        });

        return shaders.get(name);
    }

    public abstract ShaderLoader.Program getShader();
    public abstract mat4f getTransformMatrix();

    public void render(GBuffer buffer, mat4f viewMatrix, mat4f projectionMatrix) {
        getShader().setUniform("transform", getTransformMatrix());
        getShader().setUniform("viewTransform", viewMatrix);
        getShader().setUniform("projectionTransform", projectionMatrix);
    }

    public static class AmbientLight extends Light {
        private final float intensity;
        private final vec3f colour;
        private final ShaderLoader.Program shader;

        public AmbientLight(float intensity, vec3f colour) {
            this.intensity = intensity;
            this.colour = colour;
            this.shader = Light.loadShader(AmbientLight.class, () -> ShaderLoader.safeLoad(
                    "lwaf_3D/shader",
                    "pass-through.vertex-3D.glsl",
                    "ambient.fragment-3D.glsl",
                    false
            ));
        }

        public AmbientLight(float intensity) {
            this(intensity, vec3f.one);
        }

        @Override
        public ShaderLoader.Program getShader() {
            return shader;
        }

        @Override
        public mat4f getTransformMatrix() {
            return mat4f.identity();
        }

        @Override
        public void render(GBuffer buffer, mat4f viewMatrix, mat4f projectionMatrix) {
            super.render(buffer, viewMatrix, projectionMatrix);

            getShader().setUniform("lightIntensity", intensity);
            getShader().setUniform("lightColour", colour);
            getShader().start();

            Draw.drawIndexedVAO(VAO.screen_quad);

            getShader().stop();
        }
    }

    public static class DirectionalLight extends Light {
        private final float intensity;
        private final vec3f direction;
        private final vec3f colour;
        private final ShaderLoader.Program shader;

        public DirectionalLight(float intensity, vec3f direction, vec3f colour) {
            this.intensity = intensity;
            this.direction = direction.normalise();
            this.colour = colour;
            this.shader = Light.loadShader(DirectionalLight.class, () -> ShaderLoader.safeLoad(
                    "lwaf_3D/shader",
                    "pass-through.vertex-3D.glsl",
                    "directional.fragment-3D.glsl",
                    false
            ));
        }

        public DirectionalLight(float intensity, vec3f direction) {
            this(intensity, direction, vec3f.one);
        }

        @Override
        public ShaderLoader.Program getShader() {
            return shader;
        }

        @Override
        public mat4f getTransformMatrix() {
            return mat4f.identity();
        }

        @Override
        public void render(GBuffer buffer, mat4f viewMatrix, mat4f projectionMatrix) {
            super.render(buffer, viewMatrix, projectionMatrix);

            getShader().setUniform("lightIntensity", intensity);
            getShader().setUniform("lightDirection", direction);
            getShader().setUniform("lightColour", colour);
            getShader().start();

            Draw.drawIndexedVAO(VAO.screen_quad);

            getShader().stop();
        }
    }

}
