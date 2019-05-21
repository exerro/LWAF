package lwaf_model;

import lwaf_3D.*;
import lwaf_core.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Model<T extends GLVAO> implements IPositioned<Model<T>>, IRotated<Model<T>>, IScaled<Model<T>> {
    private vec3 position = new vec3(0, 0, 0),
                  rotation = new vec3(0, 0, 0),
                  scale = new vec3(1, 1, 1);
    private final Map<String, T> vaos = new HashMap<>();
    private final Map<String, Material> materials = new HashMap<>();

    public static final String DEFAULT_OBJECT_NAME = "default";

    public Model() {

    }

    public Model(T vao) {
        addObject(DEFAULT_OBJECT_NAME, vao, new Material());
    }

    public Model<T> addObject(String objectName, T vao, Material material) {
        vaos.put(objectName, vao);
        materials.put(objectName, material);
        return this;
    }

    public Model<T> removeObject(String objectName) {
        vaos.remove(objectName);
        materials.remove(objectName);
        return this;
    }

    public Set<String> getObjectNames() {
        return vaos.keySet();
    }

    public GLVAO getVAO(String objectName) {
        return vaos.get(objectName);
    }

    public GLVAO getVAO() {
        return getVAO(DEFAULT_OBJECT_NAME);
    }

    public Model<T> setVAO(String objectName, T vao) {
        if (!vaos.containsKey(objectName)) throw new IllegalStateException("No object '" + objectName + "' attached");
        vaos.put(objectName, vao);
        return this;
    }

    public Material getMaterial(String objectName) {
        return materials.get(objectName);
    }

    public Material getMaterial() {
        return getMaterial(DEFAULT_OBJECT_NAME);
    }

    public Model<T> setMaterial(String objectName, Material material) {
        if (!materials.containsKey(objectName)) throw new IllegalStateException("No object '" + objectName + "' attached");
        materials.put(objectName, material);
        return this;
    }

    public Model<T> setMaterial(Material material) {
        for (var objectName : vaos.keySet()) {
            materials.put(objectName, material);
        }
        return this;
    }

    public void draw(GLShaderProgram shader, DrawContext3D context) {
        shader.setUniform("transform", getTransformationMatrix());

        for (String objectName : getObjectNames()) {
            var material = materials.get(objectName);
            var texture = material.getTexture();

            material.setShaderUniforms(shader);

            if (material.hasTexture()) {
                shader.setUniform("useTexture", true);
                texture.bind();
            } else {
                shader.setUniform("useTexture", false);
            }

            context.drawIndexedVAO(vaos.get(objectName));

            if (material.hasTexture()) {
                texture.unbind();
            }
        }
    }

    @Override
    public vec3 getRotation() {
        return rotation;
    }

    @Override
    public Model<T> setRotation(vec3 rotation) {
        this.rotation = rotation;
        return this;
    }

    @Override
    public vec3 getTranslation() {
        return position;
    }

    @Override
    public Model<T> setTranslation(vec3 translation) {
        this.position = translation;
        return this;
    }

    public vec3 getScale() {
        return scale;
    }

    public Model<T> setScale(vec3 scale) {
        this.scale = scale;
        return this;
    }

    public mat4 getTransformationMatrix() {
        return MatrixKt.getMat4_identity()
                .mul(MatrixKt.mat4_translate(position))
                .mul(MatrixKt.mat4_rotation(rotation.getX(), new vec3(1, 0, 0)))
                .mul(MatrixKt.mat4_rotation(rotation.getY(), new vec3(0, 1, 0)))
                .mul(MatrixKt.mat4_rotation(rotation.getZ(), new vec3(0, 0, 1)))
                .mul(MatrixKt.mat4_scale(scale));
    }
}
