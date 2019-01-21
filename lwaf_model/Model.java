package lwaf_model;

import lwaf.*;
import lwaf_3D.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Model<T extends VAO> implements IPositioned<Model<T>>, IRotated<Model<T>>, IScaled<Model<T>> {
    private vec3f position = vec3f.zero,
                  rotation = vec3f.zero,
                  scale = vec3f.one;
    private final Map<String, T> vaos = new HashMap<>();
    private final Map<String, Material> materials = new HashMap<>();

    public static final String DEFAULT_OBJECT_NAME = "default";

    public Model() {

    }

    public Model(T vao) {
        attach(DEFAULT_OBJECT_NAME, vao, new Material());
    }

    public Model<T> attach(String objectName, T vao, Material material) {
        vaos.put(objectName, vao);
        materials.put(objectName, material);
        return this;
    }

    public Set<String> getObjectNames() {
        return vaos.keySet();
    }

    public VAO getVAO(String objectName) {
        return vaos.get(objectName);
    }

    public VAO getVAO() {
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

    public void draw(ShaderLoader.Program shader) {
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

            Draw.drawIndexedVAO(vaos.get(objectName));

            if (material.hasTexture()) {
                texture.unbind();
            }
        }
    }

    @Override
    public vec3f getRotation() {
        return rotation;
    }

    @Override
    public Model<T> setRotation(vec3f rotation) {
        this.rotation = rotation;
        return this;
    }

    @Override
    public vec3f getTranslation() {
        return position;
    }

    @Override
    public Model<T> setTranslation(vec3f translation) {
        this.position = translation;
        return this;
    }

    public vec3f getScale() {
        return scale;
    }

    public Model<T> setScale(vec3f scale) {
        this.scale = scale;
        return this;
    }

    public mat4f getTransformationMatrix() {
        return mat4f.identity()
                .mul(mat4f.translation(position))
                .mul(mat4f.rotation(vec3f.x_axis, rotation.x))
                .mul(mat4f.rotation(vec3f.y_axis, rotation.y))
                .mul(mat4f.rotation(vec3f.z_axis, rotation.z))
                .mul(mat4f.scale(scale));
    }
}
