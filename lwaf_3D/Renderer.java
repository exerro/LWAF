package lwaf_3D;

import lwaf.FBO;
import lwaf.ShaderLoader;
import lwaf.vec3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class Renderer {

    public void preDraw() {

    }

    public void postDraw() {

    }

    public abstract void draw();


    public static abstract class Renderer3D extends Renderer {
        private ShaderLoader.Program shader;

        public ShaderLoader.Program getShader() {
            return shader;
        }

        public void setShader(ShaderLoader.Program shader) {
            this.shader = shader;
        }

        protected abstract void setUniforms();

        @Override
        public void preDraw() {
            glEnable(GL_CULL_FACE); //
            glEnable(GL_DEPTH_TEST); //
            setUniforms();
            if (getShader() != null) getShader().start();
        }

        @Override
        public void postDraw() {
            if (getShader() != null) getShader().stop();
            glDisable(GL_CULL_FACE); //
            glDisable(GL_DEPTH_TEST); //
        }

        public void setLightingPosition(vec3f position) {
            getShader().setUniform("lightPosition", position);
        }

        public void setLighting(Lighting lighting) {
            lighting.setShaderUniforms(getShader());
        }

        public void setAmbientLighting(float ambientLightingIntensity) {
            getShader().setUniform("ambientLightingIntensity", ambientLightingIntensity);
        }
    }

    public static abstract class CameraRenderer3D extends Renderer3D {
        public abstract Camera getCamera();

        @Override
        protected void setUniforms() {
            var camera = getCamera();
            var shader = getShader();

            shader.setUniform("projectionTransform", camera.getProjectionMatrix());
            shader.setUniform("viewTransform", camera.getViewMatrix());
        }
    }
}
