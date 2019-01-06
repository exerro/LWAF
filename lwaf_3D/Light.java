package lwaf_3D;

import lwaf.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class Light {

    private static final Map<Class<? extends Light>, ShaderLoader.Program> shaders = new HashMap<>();

    public static ShaderLoader.Program getShader(Class<? extends Light> lightType) {
        return shaders.get(lightType);
    }

    protected ShaderLoader.Program registerShader(Supplier<ShaderLoader.Program> generator) {
        shaders.computeIfAbsent(this.getClass(), ignored -> {
            var shader = generator.get();

            shader.setUniform("colourMap", 0);
            shader.setUniform("positionMap", 1);
            shader.setUniform("normalMap", 2);
            shader.setUniform("lightingMap", 3);

            return shader;
        });

        return shaders.get(this.getClass());
    }

    protected ShaderLoader.Program getShader() {
        return getShader(this.getClass());
    }

    public abstract mat4f getTransformationMatrix();

    public void render(GBuffer buffer, mat4f viewMatrix, mat4f projectionMatrix) {
        getShader().setUniform("transform", getTransformationMatrix());
        getShader().setUniform("viewTransform", viewMatrix);
        getShader().setUniform("projectionTransform", projectionMatrix);
    }

    public static class AmbientLight extends Light {
        private final float intensity;
        private final vec3f colour;

        public AmbientLight(float intensity, vec3f colour) {
            this.intensity = intensity;
            this.colour = colour;

            registerShader(() -> ShaderLoader.safeLoad(
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
        public mat4f getTransformationMatrix() {
            return mat4f.identity();
        }

        @Override
        public void render(GBuffer buffer, mat4f viewMatrix, mat4f projectionMatrix) {
            super.render(buffer, viewMatrix, projectionMatrix);

            getShader().setUniform("lightIntensity", intensity);
            getShader().setUniform("lightColour", colour);
            getShader().setUniform("transform", getTransformationMatrix());

            Draw.drawIndexedVAO(VAO.screen_quad);
        }
    }

    public static class DirectionalLight extends Light {
        private final float intensity;
        private final vec3f direction;
        private final vec3f colour;

        public DirectionalLight(float intensity, vec3f direction, vec3f colour) {
            this.intensity = intensity;
            this.direction = direction.normalise();
            this.colour = colour;

            registerShader(() -> ShaderLoader.safeLoad(
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
        public mat4f getTransformationMatrix() {
            return mat4f.identity();
        }

        @Override
        public void render(GBuffer buffer, mat4f viewMatrix, mat4f projectionMatrix) {
            super.render(buffer, viewMatrix, projectionMatrix);

            getShader().setUniform("lightIntensity", intensity);
            getShader().setUniform("lightDirection", direction);
            getShader().setUniform("lightColour", colour);
            getShader().setUniform("transform", getTransformationMatrix());

            Draw.drawIndexedVAO(VAO.screen_quad);
        }
    }

    public static class PointLight extends Light {
        private final float intensity;
        private final vec3f position;
        private final vec3f attenuation;
        private final vec3f colour;

        public static final vec3f ATTENUATION = new vec3f(1, 0.09f, 0.032f);

        public PointLight(float intensity, vec3f position, vec3f attenuation, vec3f colour) {
            this.intensity = intensity;
            this.position = position;
            this.attenuation = attenuation;
            this.colour = colour;

            registerShader(() -> ShaderLoader.safeLoad(
                    "lwaf_3D/shader",
                    "pass-through.vertex-3D.glsl",
                    "point.fragment-3D.glsl",
                    false
            ));
        }

        public PointLight(float intensity, vec3f position, vec3f attenuation) {
            this(intensity, position, attenuation, vec3f.one);
        }

        public PointLight(float intensity, vec3f position) {
            this(intensity, position, ATTENUATION, vec3f.one);
        }

        public static vec3f attenuation(float distance, float halfBrightnessDistance) {
            // brightness factor = 1 / (Lx + d * (Ly + d * Lz))

            // at distance 1 , brightness = 1          : 1 / (Lx + 1  * (Ly + 1  * Lz)) = 1
            // at distance Dh, brightness = 1/2        : 1 / (Lx + Dh * (Ly + Dh * Lz)) = 1/2
            // at distance D , brightness = 1/256 ~= 0 : 1 / (Lx + D  * (Ly + D  * Lz)) = 1/256

            // Lx +      Ly +        Lz = 1
            // Lx + Dh * Ly + Dh^2 * Lz = 2
            // Lx + D  * Ly + D^2  * Lz = 256

            var Lz = ( 254/(distance-halfBrightnessDistance) - 1/(halfBrightnessDistance-1) ) / (distance+1);
            var Ly = 1/(halfBrightnessDistance-1)
                   - (
                           (halfBrightnessDistance+1)
                         / ((distance-halfBrightnessDistance) * (distance+1))
                     )
                   * (254 - (distance-halfBrightnessDistance) / (halfBrightnessDistance - 1));
            var Lx = 1 - Ly - Lz;

            return new vec3f(Lx, Ly, Lz);
        }

        @Override
        public mat4f getTransformationMatrix() {
            return mat4f.identity();
        }

        @Override
        public void render(GBuffer buffer, mat4f viewMatrix, mat4f projectionMatrix) {
            super.render(buffer, viewMatrix, projectionMatrix);

            getShader().setUniform("lightIntensity", intensity);
            getShader().setUniform("lightPosition", position);
            getShader().setUniform("lightAttenuation", attenuation);
            getShader().setUniform("lightColour", colour);
            getShader().setUniform("transform", getTransformationMatrix());

            Draw.drawIndexedVAO(VAO.screen_quad);
        }
    }

}
