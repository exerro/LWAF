package lwaf_model

import lwaf_3D.DrawContext3D
import lwaf_3D.Material
import lwaf_3D.MutableObject3D
import lwaf_core.*
import java.util.*

class Model<T : GLVAO> : MutableObject3D {
    override var translation = vec3(0f)
    override var rotation = vec3(0f)
    override var scale = vec3(1f)
    private val vaos = HashMap<String, T>()
    private val materials = HashMap<String, Material>()

    val objectNames: Set<String>
        get() = vaos.keys

    val vao: GLVAO
        get() = getVAO(DEFAULT_OBJECT_NAME)

    val material: Material
        get() = getMaterial(DEFAULT_OBJECT_NAME)

    val transformationMatrix: mat4
        get() = mat4_identity *
                mat4_translate(translation) *
                mat3_rotate(rotation.x, vec3(1f, 0f, 0f)).mat4() *
                mat3_rotate(rotation.y, vec3(0f, 1f, 0f)).mat4() *
                mat3_rotate(rotation.z, vec3(0f, 0f, 1f)).mat4() *
                mat3_scale(scale).mat4()

    constructor()

    constructor(vao: T) {
        addObject(DEFAULT_OBJECT_NAME, vao, Material())
    }

    fun addObject(objectName: String, vao: T, material: Material): Model<T> {
        vaos[objectName] = vao
        materials[objectName] = material
        return this
    }

    fun removeObject(objectName: String): Model<T> {
        vaos.remove(objectName)
        materials.remove(objectName)
        return this
    }

    fun getVAO(objectName: String): GLVAO {
        return vaos[objectName]!!
    }

    fun setVAO(objectName: String, vao: T): Model<T> {
        if (!vaos.containsKey(objectName)) throw IllegalStateException("No object '$objectName' attached")
        vaos[objectName] = vao
        return this
    }

    fun getMaterial(objectName: String): Material {
        return materials[objectName]!!
    }

    fun setMaterial(objectName: String, material: Material): Model<T> {
        if (!materials.containsKey(objectName)) throw IllegalStateException("No object '$objectName' attached")
        materials[objectName] = material
        return this
    }

    fun setMaterial(material: Material): Model<T> {
        for (objectName in vaos.keys) {
            materials[objectName] = material
        }
        return this
    }

    override fun draw(context: DrawContext3D, shader: GLShaderProgram) {
        shader.setUniform("transform", transformationMatrix)

        for (objectName in objectNames) {
            val material = materials[objectName]!!
            val texture = material.texture

            material.setShaderUniforms(shader)

            if (material.hasTexture()) {
                shader.setUniform("useTexture", true)
                texture?.bind()
            } else {
                shader.setUniform("useTexture", false)
            }

            context.drawIndexedVAO(vaos[objectName]!!)

            if (material.hasTexture()) {
                texture?.unbind()
            }
        }
    }

    companion object {
        const val DEFAULT_OBJECT_NAME = "default"
    }
}
