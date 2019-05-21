package lwaf_model;

import lwaf.*;
import lwaf_core.GLShaderProgram;
import lwaf_core.GLVAO;

import java.util.ArrayList;
import java.util.List;

public class ModelRenderer {
    private final List<Model> models = new ArrayList<>();

    public ModelRenderer() {}

    public <T extends GLVAO> Model<T> add(Model<T> model) {
        models.add(model);
        return model;
    }

    public <T extends GLVAO
            > Model<T> remove(Model<T> model) {
        models.remove(model);
        return model;
    }

    public List<Model> getModels() {
        return new ArrayList<>(models);
    }

    public void draw(GLShaderProgram shader) {
        for (Model model : models) {
            model.draw(shader);
        }
    }
}
