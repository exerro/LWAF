package lwaf_model;

import lwaf.*;
import lwaf_3D.Renderer;

import java.util.ArrayList;
import java.util.List;

public abstract class ModelRenderer extends Renderer.CameraRenderer3D {
    private final List<Model> models = new ArrayList<>();

    public ModelRenderer() {

    }

    public <T extends VAO> Model<T> add(Model<T> model) {
        models.add(model);
        return model;
    }

    public <T extends VAO> Model<T> remove(Model<T> model) {
        models.remove(model);
        return model;
    }

    public List<Model> getModels() {
        return new ArrayList<>(models);
    }

    @Override
    protected void draw(FBO framebuffer) {
        var shader = getShader();

        for (Model model : models) {
            model.draw(shader);
        }
    }
}
