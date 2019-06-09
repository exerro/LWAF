package lwaf_3D

import lwaf_3D.property.*
import lwaf_core.GLShaderProgram

interface Object3D: MutablePositioned, MutableRotated, MutableScaled {
    fun draw(context: DrawContext3D, shader: GLShaderProgram)
}
