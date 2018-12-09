package lwaf;

import org.lwjgl.opengl.GL11;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Draw {

    private static VAO rectangleVAO;
    private static int rectangleVAOuvVBOID;
    private static ShaderLoader.Program texturedShaderProgram;
    private static ShaderLoader.Program textShaderProgram;
    private static ShaderLoader.Program untexturedShaderProgram;
    private static vec3f colour = new vec3f(1, 1, 1);
    private static vec2f viewportSize;

    public static vec2f getViewportSize() {
        return viewportSize;
    }

    static void viewport(vec2f size) {
        viewportSize = size;
        glViewport(0, 0, (int) size.x, (int) size.y);
    }

    static void viewport() {
        int w = Display.getActive().getWidth(), h = Display.getActive().getHeight();
        viewportSize = new vec2f(w, h);
        glViewport(0, 0, w, h);
    }

    public static void setColour(vec3f colour) {
        Draw.colour = colour;
    }

    public static void setColour(float r, float g, float b) {
        Draw.colour = new vec3f(r, g, b);
    }

    public static void rectangle(vec2f position, vec2f size) {
        vec2f displaySize = getViewportSize();

        untexturedShaderProgram.setUniform("position", position.div(displaySize).mul(2));
        untexturedShaderProgram.setUniform("scale", size.div(displaySize).mul(2));
        untexturedShaderProgram.setUniform("colour", colour);
        untexturedShaderProgram.start();
        rectangleVAO.load();
        GL11.glDrawElements(GL11.GL_TRIANGLES, rectangleVAO.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
        rectangleVAO.unload();
        untexturedShaderProgram.stop();
    }

    public static void rectangle(int x, int y, int width, int height) {
        rectangle(new vec2f(x, y), new vec2f(width, height));
    }

    public static void image(Texture texture, vec2f position, vec2f scale) {
        vec2f displaySize = getViewportSize();
        vec2f textureSize = new vec2f(texture.getWidth(), texture.getHeight());

        texture.bind();
        glActiveTexture(GL_TEXTURE0);
        texturedShaderProgram.setUniform("position", position.div(displaySize).mul(2));
        texturedShaderProgram.setUniform("scale", scale.mul(textureSize).div(displaySize).mul(2));
        texturedShaderProgram.setUniform("colour", colour);
        texturedShaderProgram.start();
        rectangleVAO.load();
        GL11.glDrawElements(GL11.GL_TRIANGLES, rectangleVAO.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
        rectangleVAO.unload();
        texturedShaderProgram.stop();
        texture.unbind();
    }

    public static void image(Texture texture, vec2f position) {
        image(texture, position, new vec2f(1, 1));
    }

    public static void image(Texture texture) {
        image(texture, new vec2f(0, 0), new vec2f(1, 1));
    }

    public static void view(View view, vec2f position, vec2f scale) {
        view.render();

        // flip the V coordinate of texture coordinates
        rectangleVAO.bufferData(rectangleVAOuvVBOID, new float[] {
                0, 0,
                0, 1,
                1, 1,
                1, 0
        }, GL_STATIC_DRAW);
        glEnable(GL_DEPTH_TEST);

        image(view.getTexture(), position, scale);

        glDisable(GL_DEPTH_TEST);
        rectangleVAO.bufferData(rectangleVAOuvVBOID, new float[] {
                0, 1,
                0, 0,
                1, 0,
                1, 1
        }, GL_STATIC_DRAW);
    }

    public static void view(View view, vec2f position) {
        view(view, position, new vec2f(1, 1));
    }

    public static void view(View view) {
        view(view, new vec2f(0, 0), new vec2f(1, 1));
    }

    public static void text(Text text, vec2f position) {
        vec2f displaySize = getViewportSize();

        text.getFont().getTexture().bind();
        glActiveTexture(GL_TEXTURE0);
        textShaderProgram.setUniform("position", position.div(displaySize).mul(2));
        textShaderProgram.setUniform("scale", new vec2f(2, 2).div(displaySize));
        textShaderProgram.setUniform("colour", colour);
        textShaderProgram.start();
        text.getVAO().load();
        GL11.glDrawElements(GL11.GL_TRIANGLES, text.getVAO().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
        text.getVAO().unload();
        textShaderProgram.stop();
        text.getFont().getTexture().unbind();

        // if (glGetError() != GL_NO_ERROR) throw new Error("aagh");
    }

    public static void init() throws ShaderLoader.ProgramLoadException, IOException, ShaderLoader.ShaderLoadException {
        untexturedShaderProgram = ShaderLoader.load("lwaf/shader", "untextured.vertex-2D.glsl", "untextured.fragment.glsl", false);
        texturedShaderProgram = ShaderLoader.load("lwaf/shader", "textured.vertex-2D.glsl", "textured.fragment.glsl", false);
        textShaderProgram = ShaderLoader.load("lwaf/shader", "text.vertex-2D.glsl", "text.fragment.glsl", false);

        rectangleVAO = new VAO() {
            {
                int vertexVBOID, normalVBOID, colourVBOID, elementVBOID, uvVBOID;

                setVertexCount(6);

                vertexVBOID = genBuffer();
                normalVBOID = genBuffer();
                colourVBOID = genBuffer();
                elementVBOID = genBuffer();
                uvVBOID = genBuffer();

                bindBuffer(vertexVBOID, 0, 3, GL11.GL_FLOAT);
                bindBuffer(normalVBOID, 1, 3, GL11.GL_FLOAT);
                bindBuffer(colourVBOID, 2, 3, GL11.GL_FLOAT);
                bindBuffer(uvVBOID, 4, 2, GL11.GL_FLOAT);

                enableAttribute(0);
                enableAttribute(1);
                enableAttribute(2);
                enableAttribute(4);

                bufferData(vertexVBOID, new float[] {
                        0, 1, 0,
                        0, 0, 0,
                        1, 0, 0,
                        1, 1, 0
                }, GL_STATIC_DRAW);

                bufferData(normalVBOID, new float[] {
                        0, 0, 1,
                        0, 0, 1,
                        0, 0, 1,
                        0, 0, 1
                }, GL_STATIC_DRAW);

                bufferData(colourVBOID, new float[] {
                        1, 1, 1,
                        1, 1, 1,
                        1, 1, 1,
                        1, 1, 1
                }, GL_STATIC_DRAW);

                bufferElementData(elementVBOID, new int[] {
                        2, 1, 0,
                        3, 2, 0
                }, GL_STATIC_DRAW);

                bufferData(uvVBOID, new float[] {
                        0, 1,
                        0, 0,
                        1, 0,
                        1, 1
                }, GL_STATIC_DRAW);

                rectangleVAOuvVBOID = uvVBOID;
            }
        };
    }

    public static void destroy() {
        rectangleVAO.destroy();
        texturedShaderProgram.destroy();
        untexturedShaderProgram.destroy();
    }
}
