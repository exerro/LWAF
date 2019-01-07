package lwaf_3D;

import lwaf.*;
import lwaf_primitive.IcoSphereVAO;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL11.*;

public abstract class Light {
    private final float intensity;
    private final vec3f colour;

    protected Light(float intensity, vec3f colour) {
        this.intensity = intensity;
        this.colour = colour;
    }

    public abstract mat4f getTransformationMatrix(mat4f viewMatrix, mat4f projectionMatrix);
    public abstract void render(GBuffer buffer);

    protected ShaderLoader.Program getShader() {
        return getShader(this.getClass());
    }

    public float getIntensity() {
        return intensity;
    }

    public vec3f getColour() {
        return colour;
    }

    public void setUniforms(mat4f viewMatrix, mat4f projectionMatrix) {
        getShader().setUniform("transform", getTransformationMatrix(viewMatrix, projectionMatrix));
        getShader().setUniform("viewTransform", viewMatrix);
        getShader().setUniform("projectionTransform", projectionMatrix);
    }

    public static class AmbientLight extends Light {

        public AmbientLight(float intensity, vec3f colour) {
            super(intensity, colour);

            registerShader(AmbientLight.class, () -> ShaderLoader.safeLoad(
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
        public mat4f getTransformationMatrix(mat4f viewMatrix, mat4f projectionMatrix) {
            return mat4f.identity();
        }

        @Override
        public void setUniforms(mat4f viewMatrix, mat4f projectionMatrix) {
            super.setUniforms(viewMatrix, projectionMatrix);

            getShader().setUniform("lightIntensity", getIntensity());
            getShader().setUniform("lightColour", getColour());
        }

        @Override
        public void render(GBuffer buffer) {
            Draw.drawIndexedVAO(VAO.screen_quad);
        }
    }

    public static class DirectionalLight extends Light {
        private final vec3f direction;

        public DirectionalLight(float intensity, vec3f direction, vec3f colour) {
            super(intensity, colour);

            this.direction = direction.normalise();

            registerShader(DirectionalLight.class, () -> ShaderLoader.safeLoad(
                    "lwaf_3D/shader",
                    "pass-through.vertex-3D.glsl",
                    "directional.fragment-3D.glsl",
                    false
            ));
        }

        public DirectionalLight(float intensity, vec3f direction) {
            this(intensity, direction, vec3f.one);
        }

        public vec3f getDirection() {
            return direction;
        }

        @Override
        public mat4f getTransformationMatrix(mat4f viewMatrix, mat4f projectionMatrix) {
            return mat4f.identity();
        }

        @Override
        public void setUniforms(mat4f viewMatrix, mat4f projectionMatrix) {
            super.setUniforms(viewMatrix, projectionMatrix);

            getShader().setUniform("lightIntensity", getIntensity());
            getShader().setUniform("lightDirection", getDirection());
            getShader().setUniform("lightColour", getColour());
        }

        @Override
        public void render(GBuffer buffer) {
            Draw.drawIndexedVAO(VAO.screen_quad);
        }
    }

    public static class PointLight extends Light {
        private final vec3f position;
        private final vec3f attenuation;

        private static IcoSphereVAO vao;
        public static final vec3f ATTENUATION = new vec3f(1, 0.09f, 0.032f);

        public PointLight(float intensity, vec3f position, vec3f attenuation, vec3f colour) {
            super(intensity, colour);

            this.position = position;
            this.attenuation = attenuation;

            registerShader(PointLight.class, () -> ShaderLoader.safeLoad(
                    "lwaf_3D/shader",
                    "pass-through.vertex-3D.glsl",
                    "point.fragment-3D.glsl",
                    false
            ));

            if (vao == null) {
                vao = new IcoSphereVAO(2);
            }
        }

        public PointLight(float intensity, vec3f position, vec3f attenuation) {
            this(intensity, position, attenuation, vec3f.one);
        }

        public PointLight(float intensity, vec3f position) {
            this(intensity, position, ATTENUATION, vec3f.one);
        }

        public vec3f getPosition() {
            return position;
        }

        public vec3f getAttenuation() {
            return attenuation;
        }

        @Override
        public mat4f getTransformationMatrix(mat4f viewMatrix, mat4f projectionMatrix) {
            var Lx = getAttenuation().x;
            var Ly = getAttenuation().y;
            var Lz = getAttenuation().z;

            float radius;

            if (Lz != 0) radius = (-Ly + (float) Math.sqrt(Ly*Ly - 4 * Lz * (Lx - 256 * getIntensity())))
                                / (2 * Lz);
            else         radius = (256 * getIntensity() - Lx) / Ly;

            return projectionMatrix
                    .mul(viewMatrix)
                    .mul(mat4f.translation(getPosition()))
                    .mul(mat4f.scale(radius * 1.1f));
        }

        @Override
        public void setUniforms(mat4f viewMatrix, mat4f projectionMatrix) {
            super.setUniforms(viewMatrix, projectionMatrix);

            getShader().setUniform("lightIntensity", getIntensity());
            getShader().setUniform("lightPosition", getPosition());
            getShader().setUniform("lightAttenuation", getAttenuation());
            getShader().setUniform("lightColour", getColour());
        }

        @Override
        public void render(GBuffer buffer) {
            glEnable(GL_CULL_FACE);
            glFrontFace(GL_CW);
            Draw.drawIndexedVAO(vao);
            glFrontFace(GL_CCW);
            glDisable(GL_CULL_FACE);
        }
    }

    public static class SpotLight extends PointLight {
        private final vec3f direction;
        private final vec2f cutoff;

        public static final vec2f CUTOFF = new vec2f();

        public SpotLight(float intensity, vec3f position, vec3f direction, vec3f attenuation, vec2f cutoff, vec3f colour) {
            super(intensity, position, attenuation, colour);

            this.direction = direction;
            this.cutoff = cutoff;

            registerShader(SpotLight.class, () -> ShaderLoader.safeLoad(
                    "lwaf_3D/shader",
                    "pass-through.vertex-3D.glsl",
                    "spot.fragment-3D.glsl",
                    false
            ));
        }

        public SpotLight(float intensity, vec3f position, vec3f direction, vec3f attenuation, float cutoff, vec3f colour) {
            this(intensity, position, direction, attenuation, new vec2f(cutoff * 0.8f, cutoff), colour);
        }

        public SpotLight(float intensity, vec3f position, vec3f direction, vec3f attenuation) {
            this(intensity, position, direction, attenuation, CUTOFF, vec3f.one);
        }

        public SpotLight(float intensity, vec3f position, vec3f direction) {
            this(intensity, position, direction, ATTENUATION, CUTOFF, vec3f.one);
        }

        public vec3f getDirection() {
            return direction;
        }

        public vec2f getCutoff() {
            return cutoff;
        }

        @Override
        public mat4f getTransformationMatrix(mat4f viewMatrix, mat4f projectionMatrix) {
            return mat4f.identity();
        }

        @Override
        public void setUniforms(mat4f viewMatrix, mat4f projectionMatrix) {
            super.setUniforms(viewMatrix, projectionMatrix);

            getShader().setUniform("lightDirection", getDirection());
            getShader().setUniform("lightCutoff", getCutoff());
        }

        @Override
        public void render(GBuffer buffer) {
            Draw.drawIndexedVAO(VAO.screen_quad);
        }

        public static float lightSpread(float range) {
            return (float) Math.PI * range / 2;
        }
    }

    private static final Map<Class<? extends Light>, ShaderLoader.Program> shaders = new HashMap<>();

    public static void registerShader(Class<? extends Light> type, Supplier<ShaderLoader.Program> generator) {
        shaders.computeIfAbsent(type, ignored -> {
            var shader = generator.get();

            shader.setUniform("colourMap", 0);
            shader.setUniform("positionMap", 1);
            shader.setUniform("normalMap", 2);
            shader.setUniform("lightingMap", 3);

            return shader;
        });
    }

    public static ShaderLoader.Program getShader(Class<? extends Light> lightType) {
        return shaders.get(lightType);
    }

    // half brightness distance is the distance where the brightness is half its brightness at distance 1
    // this is kind of a terrible function
    public static vec3f attenuation(float distance, float halfBrightnessDistance) {
        // brightness factor = 1 / (Lx + d * (Ly + d * Lz))

        // at distance 1 , brightness = 1          : 1 / (Lx + 1  * (Ly + 1  * Lz)) = 1
        // at distance Dh, brightness = 1/2        : 1 / (Lx + Dh * (Ly + Dh * Lz)) = 1/2
        // at distance D , brightness = 1/256 ~= 0 : 1 / (Lx + D  * (Ly + D  * Lz)) = 1/256

        // Lx +      Ly +        Lz = 1
        // Lx + Dh * Ly + Dh^2 * Lz = 2
        // Lx + D  * Ly + D^2  * Lz = 256

        var Lz = (255 - (distance - 1) / (halfBrightnessDistance - 1))
               / ((distance-1) * (distance - halfBrightnessDistance));
        var Ly = (1 - (halfBrightnessDistance * halfBrightnessDistance - 1) * Lz)
               / (halfBrightnessDistance - 1);
        var Lx = 1 - Ly - Lz;

        return new vec3f(Lx, Ly, Lz);
    }

    public static vec3f attenuation(float distance) {
        var Lz = 255 / (distance * distance);
        var Ly = 0;
        var Lx = 1;

        return new vec3f(Lx, Ly, Lz);
    }

}
