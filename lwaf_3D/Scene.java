package lwaf_3D;

import lwaf.ShaderLoader;
import lwaf.mat4f;
import lwaf.vec3f;

import java.io.IOException;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.*;

public abstract class Scene {
    private final Camera camera;

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

    public void draw(GBuffer buffer) {
        buffer.bind();

        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glClearColor(0, 0, 0, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // setUniforms();
        // if (getShader() != null) getShader().start();

        drawObjects(camera.getViewMatrix(), camera.getProjectionMatrix());

        // if (getShader() != null) getShader().stop();
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);

        buffer.unbind();
    }

    public static String FRAGMENT_SHADER_PATH = "lwaf_3D/shader/fragment-3D.glsl";

    public static ShaderLoader.Program loadGeometryShader(String basePath, String vertexShader, String geometryShader, boolean instanced) throws ShaderLoader.ProgramLoadException, IOException, ShaderLoader.ShaderLoadException {
        return ShaderLoader.load(
                "",
                Paths.get(basePath, vertexShader).toString(),
                Paths.get(basePath, geometryShader).toString(),
                FRAGMENT_SHADER_PATH,
                instanced
        );
    }

    public static ShaderLoader.Program loadGeometryShader(String basePath, String vertexShader, boolean instanced) throws ShaderLoader.ProgramLoadException, IOException, ShaderLoader.ShaderLoadException {
        return ShaderLoader.load(
                "",
                Paths.get(basePath, vertexShader).toString(),
                FRAGMENT_SHADER_PATH,
                instanced
        );
    }

    public static ShaderLoader.Program safeLoadGeometryShader(String basePath, String vertexShader, String geometryShader, boolean instanced) {
        return ShaderLoader.safeLoad(
                "",
                Paths.get(basePath, vertexShader).toString(),
                Paths.get(basePath, geometryShader).toString(),
                FRAGMENT_SHADER_PATH,
                instanced
        );
    }

    public static ShaderLoader.Program safeLoadGeometryShader(String basePath, String vertexShader, boolean instanced) {
        return ShaderLoader.safeLoad(
                "",
                Paths.get(basePath, vertexShader).toString(),
                FRAGMENT_SHADER_PATH,
                instanced
        );
    }

}
