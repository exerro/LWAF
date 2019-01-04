package lwaf;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class Renderer {
    public static void drawElements(VAO vao) {
        vao.load();
        glDrawElements(GL_TRIANGLES, vao.getVertexCount(), GL_UNSIGNED_INT, 0);
        vao.unload();
    }

    public static void drawElementsInstanced(VAO vao) {
        vao.load();
        glDrawElementsInstanced(GL_TRIANGLES, vao.getVertexCount(), GL_UNSIGNED_INT, 0, vao.getInstanceCount());
        vao.unload();
    }

    protected void load() {

    }

    protected void unload() {

    }

    protected void preDraw(FBO framebuffer) {

    }

    protected void postDraw(FBO framebuffer) {

    }

    protected abstract void draw(FBO framebuffer);


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
        protected void preDraw(FBO framebuffer) {
            glEnable(GL_CULL_FACE);
            glEnable(GL_DEPTH_TEST);
            setUniforms();
            if (shader != null) shader.start();
        }

        @Override
        protected void postDraw(FBO framebuffer) {
            if (shader != null) shader.stop();
            glDisable(GL_CULL_FACE);
            glDisable(GL_DEPTH_TEST);
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
