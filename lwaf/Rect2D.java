package lwaf;

import org.lwjgl.opengl.GL11;

import java.io.IOException;

import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Rect2D extends UI {

    private static VAO vao;
    private static ShaderLoader.Program shader;

    private vec2f size;

    public Rect2D(vec2f position, vec2f size, vec3f colour) {
        super(position, colour);
        this.size = size;
    }

    public vec2f getSize() {
        return size;
    }

    public void setSize(vec2f size) {
        this.size = size;
    }

    @Override
    void draw() {
        vec2f displaySize = new vec2f(Display.getActive().getWidth(), Display.getActive().getHeight());

        shader.setUniform("position", getPosition().div(displaySize).mul(2));
        shader.setUniform("size", getSize().div(displaySize).mul(2));
        shader.setUniform("colour", getColour());
        shader.start();
        vao.load();
        GL11.glDrawElements(GL11.GL_TRIANGLES, vao.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
        vao.unload();
        shader.stop();
    }

    public static void init() throws ShaderLoader.ProgramLoadException, IOException, ShaderLoader.ShaderLoadException {
        shader = ShaderLoader.load("lwaf/shader", "vertex2D.glsl","fragment.glsl", false);

        vao = new VAO() {
            {
                int vertexVBOID, normalVBOID, colourVBOID, elementVBOID;

                setVertexCount(6);

                vertexVBOID = genBuffer();
                normalVBOID = genBuffer();
                colourVBOID = genBuffer();
                elementVBOID = genBuffer();

                bindBuffer(vertexVBOID, 0, 3, GL11.GL_FLOAT);
                bindBuffer(normalVBOID, 1, 3, GL11.GL_FLOAT);
                bindBuffer(colourVBOID, 2, 3, GL11.GL_FLOAT);

                enableAttribute(0);
                enableAttribute(1);
                enableAttribute(2);

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
            }
        };
    }

}
