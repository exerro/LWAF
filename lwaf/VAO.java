package lwaf;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL40;

import java.util.*;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

/*
    genBuffer();
    bindBufferToAttribute();
    enableAttribute();
    setBufferData();
 */

@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
public class VAO {
    private int vertexCount = 0, instanceCount = 0;
    private final int vaoID;
    private final List<Integer> vboIDs = new ArrayList<>();
    private final Set<Integer> enabledAttributes = new HashSet<>();
    private boolean areTexturesSupported;

    public static final int VERTEX_POSITION_ATTRIBUTE = 0;
    public static final int VERTEX_TEXTURE_ATTRIBUTE = 1;
    public static final int VERTEX_NORMAL_ATTRIBUTE = 2;
    public static final int VERTEX_COLOUR_ATTRIBUTE = 3;
    public static final VAO screen_quad;

    public VAO() {
        vaoID = GL30.glGenVertexArrays();
    }

    public VAO(vec3f[] vertices, vec3f[] normals, vec3f[] colours, vec2f[] uvs, int[] elements) {
        this();

        setVertexCount(elements.length);
        genVertexBuffer(vec3fToFloatArray(vertices));
        genNormalBuffer(vec3fToFloatArray(normals));
        genElementBuffer(elements);

        if (uvs != null) genUVBuffer(vec2fToFloatArray(uvs));
        if (colours != null) genColourBuffer(vec3fToFloatArray(colours));
        else                 genColourBuffer(vertices.length);
    }

    // registers that this VAO supports texturing
    public void enableTextures() {
        areTexturesSupported = true;
    }

    // sets the vertex count (number of integers in the element buffer)
    public void setVertexCount(int vertexCount) {
        this.vertexCount = vertexCount;
    }

    public void setInstanceCount(int instanceCount) {
        this.instanceCount = instanceCount;
    }

    public boolean areTexturesSupported() {
        return areTexturesSupported;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getInstanceCount() {
        return instanceCount;
    }

    public void bind() {
        GL30.glBindVertexArray(vaoID);
    }

    public void unbind() {
        GL30.glBindVertexArray(0);
    }

    // loads the VAO for drawing
    public void load() {
        bind();

        for (int attribute : enabledAttributes) {
            GL20.glEnableVertexAttribArray(attribute);
        }
    }

    // unloads the VAO from drawing
    public void unload() {
        for (int attribute : enabledAttributes) {
            GL20.glDisableVertexAttribArray(attribute);
        }

        unbind();
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

    // generates a new buffer
    protected int genBuffer() {
        int vboID = GL20.glGenBuffers();
        vboIDs.add(vboID);
        return vboID;
    }

    // generates a default vertex buffer using given data and binds it
    protected int genVertexBuffer(float[] data) {
        return genAttributeFloatBuffer(data, VERTEX_POSITION_ATTRIBUTE, 3, GL_STATIC_DRAW);
    }

    // generates a default normal buffer using given data and binds it
    protected int genNormalBuffer(float[] data) {
        return genAttributeFloatBuffer(data, VERTEX_NORMAL_ATTRIBUTE, 3, GL_STATIC_DRAW);
    }

    // generates a default colour buffer using given data and binds it
    protected int genColourBuffer(float[] data) {
        return genAttributeFloatBuffer(data, VERTEX_COLOUR_ATTRIBUTE, 3, GL_STATIC_DRAW);
    }

    // generates a default UV buffer using given data and binds it
    protected int genUVBuffer(float[] data) {
        enableTextures();
        return genAttributeFloatBuffer(data, VERTEX_TEXTURE_ATTRIBUTE, 2, GL_STATIC_DRAW);
    }

    // generates a default colour using default data and binds it
    protected int genColourBuffer(int vertices) {
        var data = new float[vertices * 3];
        Arrays.fill(data, 1);
        return genColourBuffer(data);
    }

    // generates a default element buffer
    protected int genElementBuffer(int[] data) {
        var elementVBOID = genBuffer();
        setBufferElementData(elementVBOID, data, GL_STATIC_DRAW);
        return elementVBOID;
    }

    // enables an attribute for draw calls
    protected void enableAttribute(int attribute) {
        enabledAttributes.add(attribute);
    }

    // binds a buffer to an attribute
    // `attribute` is for linking with the shader
    // `dataSize` is the number of components for each item in the buffer (e.g. 3 for (float, float, float))
    // `dataType` is the type of the items in the buffer (e.g. GL_FLOAT)
    protected void bindBufferToAttribute(int vboID, int attribute, int dataSize, int dataType) {
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
    protected void setAttributeDivisor(int attribute, int divisor) {
        GL30.glBindVertexArray(vaoID);
        GL40.glVertexAttribDivisor(attribute, divisor);
        GL30.glBindVertexArray(0);
    }

    // sets buffer data
    // usage should be GL_STATIC_DRAW or GL_DYNAMIC_DRAW
    protected void setBufferData(int vboID, float[] data, int usage) {
        GL30.glBindVertexArray(vaoID);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, usage);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GL30.glBindVertexArray(0);
    }

    // sets the buffer data to be empty
    // this can be useful for instance buffers
    protected void setBufferData(int vboID, int usage) {
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
    protected void setBufferElementData(int vboID, int[] data, int usage) {
        GL30.glBindVertexArray(vaoID);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, data, usage);

        GL30.glBindVertexArray(0);
    }

    protected static float[] vec3fToFloatArray(vec3f[] vs) {
        var result = new float[vs.length * 3];

        for (int i = 0; i < vs.length; ++i) {
            result[i * 3    ] = vs[i].x;
            result[i * 3 + 1] = vs[i].y;
            result[i * 3 + 2] = vs[i].z;
        }

        return result;
    }

    protected static float[] vec2fToFloatArray(vec2f[] vs) {
        var result = new float[vs.length * 2];

        for (int i = 0; i < vs.length; ++i) {
            result[i * 2    ] = vs[i].x;
            result[i * 2 + 1] = vs[i].y;
        }

        return result;
    }

    private int genAttributeFloatBuffer(float[] data, int attribute, int dataSize, int usage) {
        var VBOID = genBuffer();

        bindBufferToAttribute(VBOID, attribute, dataSize, GL_FLOAT);
        enableAttribute(attribute);
        setBufferData(VBOID, data, usage);

        return VBOID;
    }

    static {
        screen_quad = new VAO() {{
            setVertexCount(6);

            genVertexBuffer(new float[] {
                    -1,  1, 0,
                    -1, -1, 0,
                     1, -1, 0,
                     1,  1, 0
            });

            genUVBuffer(new float[] {
                    0, 1,
                    0, 0,
                    1, 0,
                    1, 1
            });

            genElementBuffer(new int[] {
                    2, 1, 0,
                    3, 2, 0
            });
        }};
    }

}
