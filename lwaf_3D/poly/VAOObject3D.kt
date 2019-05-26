package lwaf_3D.poly

import lwaf_3D.DrawContext3D
import lwaf_3D.Material
import lwaf_3D.MutableObject3D
import lwaf_core.*

data class VAOObject3D(val vao: GLVAO, val material: Material = Material()): MutableObject3D {
    override var translation: vec3 = vec3(0f)
    override var rotation: vec3 = vec3(0f)
    override var scale: vec3 = vec3(1f)

    override fun draw(context: DrawContext3D, shader: GLShaderProgram) {
        shader.setUniform("transform", mat4_translate(translation) * (rotation.toRotationMatrix() * mat3_scale(scale)).mat4())
        shader.setUniform("colour", material.colour)
        shader.setUniform("useTexture", material.texture != null)
        shader.setUniform("diffuseLightingIntensity", 0.7f)
        shader.setUniform("specularLightingIntensity", 0.4f)
        shader.setUniform("specularLightingPower", 5)

        material.texture?.bind()
        context.drawIndexedVAO(vao)
        material.texture?.unbind()
    }
}
