package lwaf;

import org.lwjgl.opengl.GL11;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Draw {

    private static VAO rectangleVAO;
    private static int rectangleVAOuvVBOID;
    private static ShaderLoader.Program shaderProgram2D;
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
        mat4f transform = mat4f.identity()
                .scaleBy(1, -1, 1)
                .translate(-1, -1, 0)
                .scaleBy(2 / displaySize.x, 2 / displaySize.y, 1)
                .translate(position.x, position.y, 0)
                .scaleBy(size.x, size.y, 1);

        draw2D(null, rectangleVAO, transform);
    }

    public static void rectangle(int x, int y, int width, int height) {
        rectangle(new vec2f(x, y), new vec2f(width, height));
    }

    public static void image(Texture texture, vec2f position, vec2f scale) {
        vec2f displaySize = getViewportSize();
        mat4f transform = mat4f.identity()
                .scaleBy(1, -1, 1)
                .translate(-1, -1, 0)
                .scaleBy(2 / displaySize.x, 2 / displaySize.y, 1)
                .translate(position.x, position.y, 0)
                .scaleBy(texture.getWidth(), texture.getHeight(), 1)
                .scaleBy(new vec3f(scale, 1));

        draw2D(texture, rectangleVAO, transform);
    }

    public static void image(Texture texture, vec2f position) {
        image(texture, position, new vec2f(1, 1));
    }

    public static void image(Texture texture) {
        image(texture, new vec2f(0, 0), new vec2f(1, 1));
    }

    public static void view(View view, vec2f position, vec2f scale) {
        view.render();

        vec2f displaySize = getViewportSize();
        mat4f transform = mat4f.identity()
                .scaleBy(1, -1, 1)
                .translate(-1, -1, 0)
                .scaleBy(2 / displaySize.x, 2 / displaySize.y, 1)
                .translate(position.x, position.y, 0)
                .scaleBy(view.getTexture().getWidth(), view.getTexture().getHeight(), 1)
                .scaleBy(new vec3f(scale, 1))
                .translate(0, 1, 0)
                .scaleBy(1, -1, 0);

        draw2D(view.getTexture(), rectangleVAO, transform);
    }

    public static void view(View view, vec2f position) {
        view(view, position, new vec2f(1, 1));
    }

    public static void view(View view) {
        view(view, new vec2f(0, 0), new vec2f(1, 1));
    }

    public static void text(Text text, vec2f position) {
        vec2f displaySize = getViewportSize();
        mat4f transform = mat4f.identity()
                .translate(-1, 1, 0)
                .scaleBy(2 / displaySize.x, 2 / displaySize.y, 1)
                .scaleBy(1, -1, 0)
                .translate(position.x, position.y, 0);

        draw2D(text.getFont().getTexture(), text.getVAO(), transform);
    }

    private static void draw2D(Texture texture, VAO vao, mat4f transform) {
        vec2f displaySize = getViewportSize();

        if (texture != null) {
            texture.bind();
            glActiveTexture(GL_TEXTURE0);
        }

        shaderProgram2D.setUniform("transform", transform);
        shaderProgram2D.setUniform("colour", colour);
        shaderProgram2D.setUniform("useTexture", texture != null);
        shaderProgram2D.start();
        vao.load();
        glDrawElements(GL_TRIANGLES, vao.getVertexCount(), GL_UNSIGNED_INT, 0);
        vao.unload();
        shaderProgram2D.stop();

        if (texture != null) {
            texture.unbind();
        }
    }

    public static void init() throws ShaderLoader.ProgramLoadException, IOException, ShaderLoader.ShaderLoadException {
        shaderProgram2D = ShaderLoader.load("lwaf/shader", "vertex-2D.glsl", "fragment-2D.glsl", false);

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
                bindBuffer(uvVBOID, 3, 2, GL11.GL_FLOAT);

                enableAttribute(0);
                enableAttribute(1);
                enableAttribute(2);
                enableAttribute(3);

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
        shaderProgram2D.destroy();
    }
}
