package lwaf_3D;

import lwaf.mat4f;
import lwaf.vec3f;

import java.util.*;

public abstract class Scene {
    private final Camera camera;
    private final Map<Class<? extends Light>, List<Light>> lights = new HashMap<>();

    public Scene(Camera camera) {
        this.camera = camera;
    }

    public Scene() {
        this(new Camera(vec3f.zero));
    }

    protected abstract void drawObjects(mat4f viewMatrix, mat4f projectionMatrix);

    public Camera getCamera() {
        return camera;
    }

    public Scene addLight(Light light) {
        lights.computeIfAbsent(light.getClass(), ignored -> new ArrayList<>());
        lights.get(light.getClass()).add(light);

        return this;
    }

    public Scene removeLight(Light light) {
        if (lights.containsKey(light.getClass())) {
            lights.get(light.getClass()).remove(light);
        }

        return this;
    }

    public Set<Class<? extends Light>> getLightTypes() {
        return lights.keySet();
    }

    public List<Light> getLightsOfType(Class<? extends Light> type) {
        return lights.getOrDefault(type, new ArrayList<>());
    }

}
