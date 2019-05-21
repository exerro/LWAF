package lwaf_3D;

import lwaf_core.mat4;
import lwaf_core.vec3;

import java.util.*;

public abstract class Scene {
    private final Camera camera;
    private final Map<Class<? extends Light>, List<Light>> lights = new HashMap<>();

    public Scene(Camera camera) {
        this.camera = camera;
    }

    public Scene() {
        this(new Camera(new vec3(0, 0, 0)));
    }

    protected abstract void drawObjects(mat4 viewMatrix, mat4 projectionMatrix);

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
