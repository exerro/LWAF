package lwaf_3D

import lwaf_3D.property.Positioned
import lwaf_3D.property.Rotated
import lwaf_3D.property.Scaled
import lwaf_core.GLShaderProgram

abstract class Object3D: Positioned, Rotated, Scaled {
    abstract fun draw(context: DrawContext3D, shader: GLShaderProgram)
}
