package lwaf;

import static org.lwjgl.opengl.GL11.*;

public abstract class Renderer {
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

        public abstract void setUniforms();

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

    public static abstract class VAORenderer3D extends Renderer3D {
        private VAO vao;

        public VAO getVAO() {
            return vao;
        }

        public void setVAO(VAO vao) {
            this.vao = vao;
        }

        @Override
        protected void draw(FBO framebuffer) {
            vao.load();
            glDrawElements(GL_TRIANGLES, vao.getVertexCount(), GL_UNSIGNED_INT, 0);
            vao.unload();
        }
    }
}
