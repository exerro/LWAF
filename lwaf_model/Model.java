package lwaf_model;

import lwaf.*;
import lwaf_3D.Lighting;

public class Model<T extends VAO> implements ITranslated<Model<T>>, IRotated<Model<T>> {
    private vec3f position = vec3f.zero,
                  rotation = vec3f.zero,
                  scale = vec3f.one;
    private vec3f colour = vec3f.one;
    private Texture texture = null;
    private Lighting lighting = new Lighting();
    private final T vao;

    public Model(T vao) {
        this.vao = vao;
    }

    public void draw(ShaderLoader.Program shader) {
        Texture texture = getTexture();

        shader.setUniform("transform", getTransformationMatrix());
        shader.setUniform("colour", getColour());

        lighting.setShaderUniforms(shader);

        if (texture != null) {
            shader.setUniform("useTexture", true);
            texture.bind();
        }
        else {
            shader.setUniform("useTexture", false);
        }

        Draw.drawElements(getVAO());

        if (texture != null) {
            texture.unbind();
        }
    }

    public T getVAO() {
        return vao;
    }

    public Texture getTexture() {
        return texture;
    }

    public Model<T> setTexture(Texture texture) {
        if (!vao.areTexturesSupported())
            throw new IllegalStateException("Model VAO does not support textures (" + vao.getClass().getName() + ")");

        this.texture = texture;
        return this;
    }

    public Lighting getLighting() {
        return lighting;
    }

    public Model<T> setLighting(Lighting lighting) {
        this.lighting = lighting;
        return this;
    }

    public Model<T> setSpecularLighting(float specularLightingIntensity) {
        lighting.setSpecularLightingIntensity(specularLightingIntensity);
        return this;
    }

    public vec3f getColour() {
        return colour;
    }

    public Model<T> setColour(float r, float g, float b) {
        this.colour = new vec3f(r, g, b);
        return this;
    }

    public Model<T> setColour(vec3f colour) {
        this.colour = colour;
        return this;
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

    public Model<T> setScale(float x, float y, float z) {
        return setScale(new vec3f(x, y, z));
    }

    public Model<T> setScale(float scale) {
        return setScale(scale, scale, scale);
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
