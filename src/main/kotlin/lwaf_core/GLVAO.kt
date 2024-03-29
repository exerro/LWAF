package lwaf_core


import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL40

import java.util.*

import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL15.GL_STATIC_DRAW

/*
    Usage:
    - genBuffer();
    - bindBufferToAttribute();
    - enableAttribute();
    - setBufferData();
 */

open class GLVAO : GLResource {
    var vertexCount = 0 // the number of integers in the element buffer
    var instanceCount = 0 // the number of instances to draw
    private val vaoID: Int = GL30.glGenVertexArrays() // the ID of the VAO
    private val vboIDs = ArrayList<Int>() // list of VBO IDs
    private val enabledAttributes = HashSet<Int>() // set of currently enabled attributes
    private var areTexturesSupported: Boolean = false // whether textures are supported or not

    /**
     * registers that this VAO supports texturing
     */
    fun enableTextures() {
        areTexturesSupported = true
    }

    /**
     * Returns whether textures are supported
     */
    fun areTexturesSupported(): Boolean {
        return areTexturesSupported
    }

    /**
     * binds the VAO to be updated used
     */
    fun bind() {
        GL30.glBindVertexArray(vaoID)
    }

    /**
     * unbinds the VAO
     */
    fun unbind() {
        GL30.glBindVertexArray(0)
    }

    /**
     * loads the VAO for drawing
     */
    fun load() {
        bind()

        for (attribute in enabledAttributes) {
            GL20.glEnableVertexAttribArray(attribute)
        }
    }

    /**
     * unloads the VAO from drawing
     */
    fun unload() {
        for (attribute in enabledAttributes) {
            GL20.glDisableVertexAttribArray(attribute)
        }

        unbind()
    }

    /**
     * destroys the VAO and VBOs associated with it
     */
    override fun destroy() {
        GL30.glBindVertexArray(vaoID)

        for (attribute in enabledAttributes) {
            GL20.glDisableVertexAttribArray(attribute)
        }

        GL30.glBindVertexArray(0)
        GL30.glDeleteVertexArrays(vaoID)

        for (vboID in vboIDs) {
            GL15.glDeleteBuffers(vboID)
        }
    }

    /**
     * generates a new buffer
     */
    fun genBuffer(): Int {
        val vboID = GL20.glGenBuffers()
        vboIDs.add(vboID)
        return vboID
    }

    /**
     * generates a default vertex buffer using given data and binds it
     */
    fun genVertexBuffer(data: FloatArray): Int {
        return genAttributeFloatBuffer(data, VERTEX_POSITION_ATTRIBUTE, 3, GL_STATIC_DRAW)
    }

    /**
     * generates a default normal buffer using given data and binds it
     */
    fun genNormalBuffer(data: FloatArray): Int {
        return genAttributeFloatBuffer(data, VERTEX_NORMAL_ATTRIBUTE, 3, GL_STATIC_DRAW)
    }

    /**
     * generates a default colour buffer using given data and binds it
     */
    fun genColourBuffer(data: FloatArray): Int {
        return genAttributeFloatBuffer(data, VERTEX_COLOUR_ATTRIBUTE, 3, GL_STATIC_DRAW)
    }

    /**
     * generates a default colour using default data and binds it
     */
    fun genColourBuffer(vertices: Int): Int {
        val data = FloatArray(vertices * 3)
        Arrays.fill(data, 1f)
        return genColourBuffer(data)
    }

    /**
     * generates a default UV buffer using given data and binds it
     */
    fun genUVBuffer(data: FloatArray): Int {
        enableTextures()
        return genAttributeFloatBuffer(data, VERTEX_TEXTURE_ATTRIBUTE, 2, GL_STATIC_DRAW)
    }

    /**
     * generates a default element buffer
     */
    fun genElementBuffer(data: IntArray): Int {
        val elementVBOID = genBuffer()
        setBufferElementData(elementVBOID, data, GL_STATIC_DRAW)
        return elementVBOID
    }

    /**
     * enables an attribute for draw calls
     */
    fun enableAttribute(attribute: Int) {
        enabledAttributes.add(attribute)
    }

    /**
     * binds a buffer to an attribute
     * @param vboID - the ID of the VBO to use
     * @param attribute - for linking with the shader
     * @param dataSize - the number of components for each item in the buffer (e.g. 3 for (float, float, float))
     * @param dataType - the type of the items in the buffer (e.g. GL_FLOAT)
     */
    fun bindBufferToAttribute(vboID: Int, attribute: Int, dataSize: Int, dataType: Int) {
        GL30.glBindVertexArray(vaoID)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID)
        GL20.glVertexAttribPointer(attribute, dataSize, dataType, false, 0, 0)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        GL30.glBindVertexArray(0)
    }

    /**
     * sets the attribute divisor for an attribute
     * @param attribute - the attribute to modify
     * @param divisor(0) - controls which item in the buffer is passed to the shader
     *                     use 1 with instancing if the buffer contains instance-specific data
     */
    fun setAttributeDivisor(attribute: Int, divisor: Int) {
        GL30.glBindVertexArray(vaoID)
        GL40.glVertexAttribDivisor(attribute, divisor)
        GL30.glBindVertexArray(0)
    }

    /**
     * sets buffer data
     * @param usage - should be GL_STATIC_DRAW or GL_DYNAMIC_DRAW
     */
    fun setBufferData(vboID: Int, data: FloatArray, usage: Int) {
        GL30.glBindVertexArray(vaoID)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, usage)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)

        GL30.glBindVertexArray(0)
    }

    /**
     * sets the buffer data to be empty
     * this can be useful for instance buffers
     */
    fun setBufferData(vboID: Int, usage: Int) {
        GL30.glBindVertexArray(vaoID)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 0, usage)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)

        GL30.glBindVertexArray(0)
    }

    /**
     * updates buffer data
     */
    fun updateBufferData(vboID: Int, data: FloatArray) {
        GL30.glBindVertexArray(vaoID)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID)
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, data)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)

        GL30.glBindVertexArray(0)
    }

    /**
     * sets buffer data for element array buffers
     */
    private fun setBufferElementData(vboID: Int, data: IntArray, usage: Int) {
        GL30.glBindVertexArray(vaoID)

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID)
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, data, usage)

        GL30.glBindVertexArray(0)
    }

    private fun genAttributeFloatBuffer(data: FloatArray, attribute: Int, dataSize: Int, usage: Int): Int {
        val vboID = genBuffer()

        bindBufferToAttribute(vboID, attribute, dataSize, GL_FLOAT)
        enableAttribute(attribute)
        setBufferData(vboID, data, usage)

        return vboID
    }

    companion object {
        const val VERTEX_POSITION_ATTRIBUTE = 0
        const val VERTEX_TEXTURE_ATTRIBUTE = 1
        const val VERTEX_NORMAL_ATTRIBUTE = 2
        const val VERTEX_COLOUR_ATTRIBUTE = 3

        fun vec3fToFloatArray(vs: Collection<vec3>): FloatArray {
            val result = FloatArray(vs.size * 3)
            var i = 0

            for (v in vs) {
                result[i] = v.x
                result[i + 1] = v.y
                result[i + 2] = v.z
                i += 3
            }

            return result
        }

        fun vec2fToFloatArray(vs: Collection<vec2>): FloatArray {
            val result = FloatArray(vs.size * 2)
            var i = 0

            for (v in vs) {
                result[i] = v.x
                result[i + 1] = v.y
                i += 2
            }

            return result
        }
    }
}
val screen_quad: GLVAO = object : GLVAO() {
    init {
        vertexCount = 6
        genVertexBuffer(floatArrayOf(-1f, 1f, 0f, -1f, -1f, 0f, 1f, -1f, 0f, 1f, 1f, 0f))
        genUVBuffer(floatArrayOf(0f, 1f, 0f, 0f, 1f, 0f, 1f, 1f))
        genElementBuffer(intArrayOf(2, 1, 0, 3, 2, 0))
    }
}

fun generateStandardVAO(vertices: Collection<vec3>, normals: Collection<vec3>, elements: IntArray, colours: Int, uvs: Collection<vec2>? = null): GLVAO {
    val vao = GLVAO()
    vao.vertexCount = elements.size
    vao.genVertexBuffer(GLVAO.vec3fToFloatArray(vertices))
    vao.genNormalBuffer(GLVAO.vec3fToFloatArray(normals))
    vao.genElementBuffer(elements)

    if (uvs != null)
        vao.genUVBuffer(GLVAO.vec2fToFloatArray(uvs))

    vao.genColourBuffer(colours)

    return vao
}

fun generateStandardVAO(vertices: Array<vec3>, normals: Array<vec3>, elements: IntArray, colours: Int, uvs: Array<vec2>? = null): GLVAO
        = generateStandardVAO(vertices.toList(), normals.toList(), elements, colours, uvs?.toList())

fun generateStandardVAO(vertices: Collection<vec3>, normals: Collection<vec3>, elements: IntArray, colours: Collection<vec3>? = null, uvs: Collection<vec2>? = null): GLVAO {
    val vao = GLVAO()
    vao.vertexCount = elements.size
    vao.genVertexBuffer(GLVAO.vec3fToFloatArray(vertices))
    vao.genNormalBuffer(GLVAO.vec3fToFloatArray(normals))
    vao.genElementBuffer(elements)

    if (uvs != null)
        vao.genUVBuffer(GLVAO.vec2fToFloatArray(uvs))

    if (colours != null)
        vao.genColourBuffer(GLVAO.vec3fToFloatArray(colours))
    else
        vao.genColourBuffer(vertices.size)

    return vao
}

fun generateStandardVAO(vertices: Array<vec3>, normals: Array<vec3>, elements: IntArray, colours: Array<vec3>? = null, uvs: Array<vec2>? = null): GLVAO
        = generateStandardVAO(vertices.toList(), normals.toList(), elements, colours?.toList(), uvs?.toList())
