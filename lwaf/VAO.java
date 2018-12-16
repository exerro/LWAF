package lwaf;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL40;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
    genBuffer();
    bindBuffer();
    enableAttribute();
    bufferData();
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class VAO {
    private int vertexCount = 0, instanceCount = 0;
    private final int vaoID;
    private final List<Integer> vboIDs = new ArrayList<>();
    private final Set<Integer> enabledAttributes = new HashSet<>();

    public VAO() {
        vaoID = GL30.glGenVertexArrays();
    }

    public void setVertexCount(int vertexCount) {
        this.vertexCount = vertexCount;
    }

    public void setInstanceCount(int instanceCount) {
        this.instanceCount = instanceCount;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getInstanceCount() {
        return instanceCount;
    }

    public void load() {
        bindVAO();

        for (int attribute : enabledAttributes) {
            GL20.glEnableVertexAttribArray(attribute);
        }
    }

    public void unload() {
        for (int attribute : enabledAttributes) {
            GL20.glDisableVertexAttribArray(attribute);
        }

        unbindVAO();
    }

    protected void bindVAO() {
        GL30.glBindVertexArray(vaoID);
    }

    protected void unbindVAO() {
        GL30.glBindVertexArray(0);
    }

    // generates a new buffer
    protected int genBuffer() {
        int vboID = GL20.glGenBuffers();

        vboIDs.add(vboID);

        return vboID;
    }

    // binds a buffer to an attribute
    // `attribute` is for linking with the shader
    // `dataSize` is the number of components for each item in the buffer (e.g. 3 for (float, float, float))
    // `dataType` is the type of the items in the buffer (e.g. GL_FLOAT)
    protected void bindBuffer(int vboID, int attribute, int dataSize, int dataType) {
        GL30.glBindVertexArray(vaoID);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        GL20.glVertexAttribPointer(attribute, dataSize, dataType, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GL30.glBindVertexArray(0);
    }

    // sets the attribute divisor for an attribute
    // controls which item in the buffer is passed to the shader
    // default is 0
    // use 1 with instancing if the buffer contains instance-specific data
    protected void attributeDivisor(int attribute, int divisor) {
        GL30.glBindVertexArray(vaoID);
        GL40.glVertexAttribDivisor(attribute, divisor);
        GL30.glBindVertexArray(0);
    }

    // sets buffer data
    // usage should be GL_STATIC_DRAW or GL_DYNAMIC_DRAW
    protected void bufferData(int vboID, float[] data, int usage) {
        GL30.glBindVertexArray(vaoID);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, usage);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GL30.glBindVertexArray(0);
    }

    // sets the buffer data to be empty
    protected void bufferData(int vboID, int usage) {
        GL30.glBindVertexArray(vaoID);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 0, usage);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GL30.glBindVertexArray(0);
    }

    // updates buffer data
    protected void updateBufferData(int vboID, float[] data) {
        GL30.glBindVertexArray(vaoID);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, data);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GL30.glBindVertexArray(0);
    }

    // sets buffer data for element array buffers
    protected void bufferElementData(int vboID, int[] data, int usage) {
        GL30.glBindVertexArray(vaoID);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, data, usage);

        GL30.glBindVertexArray(0);
    }

    // enables an attribute for draw calls
    protected void enableAttribute(int attribute) {
        enabledAttributes.add(attribute);
    }

    // destroys the VAO and VBOs associated with it
    public void destroy() {
        GL30.glBindVertexArray(vaoID);

        for (int attribute : enabledAttributes) {
            GL20.glDisableVertexAttribArray(attribute);
        }

        GL30.glBindVertexArray(0);
        GL30.glDeleteVertexArrays(vaoID);

        for (int vboID : vboIDs) {
            GL15.glDeleteBuffers(vboID);
        }
    }

    protected static float[] vec3fToFloatArray(vec3f[] vs) {
        float[] result = new float[vs.length * 3];

        for (int i = 0; i < vs.length; ++i) {
            result[i * 3    ] = vs[i].x;
            result[i * 3 + 1] = vs[i].y;
            result[i * 3 + 2] = vs[i].z;
        }

        return result;
    }

}
