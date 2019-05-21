package lwaf_3D;

import lwaf_core.*;
import lwaf_primitive.IcoSphereVAO;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL11.*;

public abstract class Light {
    private final float intensity;
    private final vec3 colour;

    protected Light(float intensity, vec3 colour) {
        this.intensity = intensity;
        this.colour = colour;
    }

    public abstract mat4 getTransformationMatrix(mat4 viewMatrix, mat4 projectionMatrix);
    public abstract void render(GBuffer buffer);

    protected GLShaderProgram getShader() {
        return getShader(this.getClass());
    }

    public float getIntensity() {
        return intensity;
    }

    public vec3 getColour() {
        return colour;
    }

    public void setUniforms(mat4 viewMatrix, mat4 projectionMatrix) {
        getShader().setUniform("transform", getTransformationMatrix(viewMatrix, projectionMatrix));
        getShader().setUniform("viewTransform", viewMatrix);
        getShader().setUniform("projectionTransform", projectionMatrix);
    }

    public static class AmbientLight extends Light {

        public AmbientLight(float intensity, vec3 colour) {
            super(intensity, colour);

            registerShader(AmbientLight.class, () -> GLShaderProgramKt.loadShaderProgramFiles(
                    "lwaf_3D/shader/pass-through.vertex-3D.glsl",
                    "lwaf_3D/shader/ambient.fragment-3D.glsl",
                    false
            ));
        }

        public AmbientLight(float intensity) {
            this(intensity, new vec3(1, 1, 1));
        }

        @Override
        public mat4 getTransformationMatrix(mat4 viewMatrix, mat4 projectionMatrix) {
            return MatrixKt.getMat4_identity();
        }

        @Override
        public void setUniforms(mat4 viewMatrix, mat4 projectionMatrix) {
            super.setUniforms(viewMatrix, projectionMatrix);

            getShader().setUniform("lightIntensity", getIntensity());
            getShader().setUniform("lightColour", getColour());
        }

        @Override
        public void render(GBuffer buffer) {
            buffer.context.drawIndexedVAO(GLVAOKt.getScreen_quad());
        }
    }

    public static class DirectionalLight extends Light {
        private final vec3 direction;

        public DirectionalLight(float intensity, vec3 direction, vec3 colour) {
            super(intensity, colour);

            this.direction = direction.normalise();

            registerShader(DirectionalLight.class, () -> GLShaderProgramKt.loadShaderProgramFiles(
                    "lwaf_3D/shader/pass-through.vertex-3D.glsl",
                    "lwaf_3D/shader/directional.fragment-3D.glsl",
                    false
            ));
        }

        public DirectionalLight(float intensity, vec3 direction) {
            this(intensity, direction, new vec3(1, 1, 1));
        }

        public vec3 getDirection() {
            return direction;
        }

        @Override
        public mat4 getTransformationMatrix(mat4 viewMatrix, mat4 projectionMatrix) {
            return MatrixKt.getMat4_identity();
        }

        @Override
        public void setUniforms(mat4 viewMatrix, mat4 projectionMatrix) {
            super.setUniforms(viewMatrix, projectionMatrix);

            getShader().setUniform("lightIntensity", getIntensity());
            getShader().setUniform("lightDirection", getDirection());
            getShader().setUniform("lightColour", getColour());
        }

        @Override
        public void render(GBuffer buffer) {
            buffer.context.drawIndexedVAO(GLVAOKt.getScreen_quad());
        }
    }

    public static class PointLight extends Light {
        private final vec3 position;
        private final vec3 attenuation;

        private static IcoSphereVAO vao;
        public static final vec3 ATTENUATION = new vec3(1, 0.09f, 0.032f);

        public PointLight(float intensity, vec3 position, vec3 attenuation, vec3 colour) {
            super(intensity, colour);

            this.position = position;
            this.attenuation = attenuation;

            registerShader(PointLight.class, () -> GLShaderProgramKt.loadShaderProgramFiles(
                    "lwaf_3D/shader/pass-through.vertex-3D.glsl",
                    "lwaf_3D/shader/point.fragment-3D.glsl",
                    false
            ));

            if (vao == null) {
                vao = new IcoSphereVAO(2);
            }
        }

        public PointLight(float intensity, vec3 position, vec3 attenuation) {
            this(intensity, position, attenuation, new vec3(1, 1, 1));
        }

        public PointLight(float intensity, vec3 position) {
            this(intensity, position, ATTENUATION, new vec3(1, 1, 1));
        }

        public vec3 getPosition() {
            return position;
        }

        public vec3 getAttenuation() {
            return attenuation;
        }

        @Override
        public mat4 getTransformationMatrix(mat4 viewMatrix, mat4 projectionMatrix) {
            var Lx = getAttenuation().getX();
            var Ly = getAttenuation().getY();
            var Lz = getAttenuation().getZ();

            float radius;

            if (Lz != 0) radius = (-Ly + (float) Math.sqrt(Ly*Ly - 4 * Lz * (Lx - 256 * getIntensity())))
                                / (2 * Lz);
            else         radius = (256 * getIntensity() - Lx) / Ly;

            return projectionMatrix
                    .mul(viewMatrix)
                    .mul(MatrixKt.mat4_translate(getPosition().unm()))
                    .mul(MatrixKt.mat4_scale(radius * 1.1f));
        }

        @Override
        public void setUniforms(mat4 viewMatrix, mat4 projectionMatrix) {
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
            buffer.context.drawIndexedVAO(vao);
            glFrontFace(GL_CCW);
            glDisable(GL_CULL_FACE);
        }
    }

    public static class SpotLight extends PointLight {
        private final vec3 direction;
        private final vec2 cutoff;

        public static final vec2 CUTOFF = new vec2(0, 0);

        public SpotLight(float intensity, vec3 position, vec3 direction, vec3 attenuation, vec2 cutoff, vec3 colour) {
            super(intensity, position, attenuation, colour);

            this.direction = direction;
            this.cutoff = cutoff;

            registerShader(SpotLight.class, () -> GLShaderProgramKt.loadShaderProgramFiles(
                    "lwaf_3D/shader/pass-through.vertex-3D.glsl",
                    "lwaf_3D/shader/spot.fragment-3D.glsl",
                    false
            ));
        }

        public SpotLight(float intensity, vec3 position, vec3 direction, vec3 attenuation, float cutoff, vec3 colour) {
            this(intensity, position, direction, attenuation, new vec2(cutoff * 0.8f, cutoff), colour);
        }

        public SpotLight(float intensity, vec3 position, vec3 direction, vec3 attenuation) {
            this(intensity, position, direction, attenuation, CUTOFF, new vec3(1, 1, 1));
        }

        public SpotLight(float intensity, vec3 position, vec3 direction) {
            this(intensity, position, direction, ATTENUATION, CUTOFF, new vec3(1, 1, 1));
        }

        public vec3 getDirection() {
            return direction;
        }

        public vec2 getCutoff() {
            return cutoff;
        }

        @Override
        public mat4 getTransformationMatrix(mat4 viewMatrix, mat4 projectionMatrix) {
            return MatrixKt.getMat4_identity();
        }

        @Override
        public void setUniforms(mat4 viewMatrix, mat4 projectionMatrix) {
            super.setUniforms(viewMatrix, projectionMatrix);

            getShader().setUniform("lightDirection", getDirection());
            getShader().setUniform("lightCutoff", getCutoff());
        }

        @Override
        public void render(GBuffer buffer) {
            buffer.context.drawIndexedVAO(GLVAOKt.getScreen_quad());
        }

        public static float lightSpread(float range) {
            return (float) Math.PI * range / 2;
        }
    }

    private static final Map<Class<? extends Light>, GLShaderProgram> shaders = new HashMap<>();

    public static void registerShader(Class<? extends Light> type, Supplier<GLShaderProgram> generator) {
        shaders.computeIfAbsent(type, ignored -> {
            var shader = generator.get();

            shader.setUniform("colourMap", 0);
            shader.setUniform("positionMap", 1);
            shader.setUniform("normalMap", 2);
            shader.setUniform("lightingMap", 3);

            return shader;
        });
    }

    public static GLShaderProgram getShader(Class<? extends Light> lightType) {
        return shaders.get(lightType);
    }

    // half brightness distance is the distance where the brightness is half its brightness at distance 1
    // this is kind of a terrible function
    public static vec3 attenuation(float distance, float halfBrightnessDistance) {
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

        return new vec3(Lx, Ly, Lz);
    }

    public static vec3 attenuation(float distance) {
        var Lz = 255 / (distance * distance);
        var Ly = 0;
        var Lx = 1;

        return new vec3(Lx, Ly, Lz);
    }

}
