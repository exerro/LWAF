package lwaf_3D;

import lwaf.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL14.glBlendEquation;

public abstract class Scene {
    private final Camera camera;

    public Scene(Camera camera) {
        this.camera = camera;
    }

    public Scene() {
        this(new Camera(vec3f.zero));
    }

    protected abstract void drawObjects(mat4f viewMatrix, mat4f projectionMatrix);
    protected abstract List<Light> getLights();

    public Camera getCamera() {
        return camera;
    }

    public void draw(FBO framebuffer, GBuffer buffer) {
        var viewMatrix = camera.getViewMatrix();
        var projectionMatrix = camera.getProjectionMatrix();

        buffer.bind();

        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glDepthMask(true);
        glClearColor(0, 0, 0, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        drawObjects(viewMatrix, projectionMatrix);

        glDepthMask(false);
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);

        buffer.unbind();
        framebuffer.bind();

        glClearColor(0, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT);
        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_ONE, GL_ONE);

        buffer.bindReading();

        for (var light : getLights()) {
            light.render(buffer, viewMatrix, projectionMatrix);
        }

        framebuffer.unbind();
        buffer.unbindReading();

        glDisable(GL_BLEND);
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
