package lwaf;

import lwaf_core.GLShaderProgram;
import lwaf_core.GLShaderProgramKt;
import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public abstract class ShaderLoader {
    public static GLShaderProgram safeLoad(String basePath, String vertexShader, String fragmentShader, boolean instanced) {
        try {
            var vertexShaderContent = new String(Files.readAllBytes(Paths.get(basePath, vertexShader)));
            var fragmentShaderContent = new String(Files.readAllBytes(Paths.get(basePath, fragmentShader)));
            return GLShaderProgramKt.loadShaderProgram(vertexShaderContent, fragmentShaderContent, instanced);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            while (true);
        }
    }
}
