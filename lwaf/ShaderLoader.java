package lwaf;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class ShaderLoader {
    private static final Map<Integer, Integer> referenceCount = new HashMap<>();
    private static final Map<String, Integer> shaderIDLookup = new HashMap<>();

    public static Program load(String basePath, String vertexShader, String geometryShader, String fragmentShader, boolean instanced) throws ProgramLoadException, IOException, ShaderLoadException {
        return loadShaders(
                Paths.get(basePath, vertexShader).toString(),
                Paths.get(basePath, geometryShader).toString(),
                Paths.get(basePath, fragmentShader).toString(),
                instanced
        );
    }

    public static Program load(String basePath, String vertexShader, String fragmentShader, boolean instanced) throws ProgramLoadException, IOException, ShaderLoadException {
        return loadShaders(
                Paths.get(basePath, vertexShader).toString(),
                null,
                Paths.get(basePath, fragmentShader).toString(),
                instanced
        );
    }

    public static Program safeLoad(String basePath, String vertexShader, String geometryShader, String fragmentShader, boolean instanced) {
        try {
            return loadShaders(
                    Paths.get(basePath, vertexShader).toString(),
                    Paths.get(basePath, geometryShader).toString(),
                    Paths.get(basePath, fragmentShader).toString(),
                    instanced
            );
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            while (true);
        }
    }

    public static Program safeLoad(String basePath, String vertexShader, String fragmentShader, boolean instanced) {
        try {
            return loadShaders(
                    Paths.get(basePath, vertexShader).toString(),
                    null,
                    Paths.get(basePath, fragmentShader).toString(),
                    instanced
            );
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            while (true);
        }
    }

    private static Program loadShaders(String vertexShader, String geometryShader, String fragmentShader, boolean instanced) throws ProgramLoadException, IOException, ShaderLoadException {
        int programID  = GL20.glCreateProgram(),
            vertexID   = loadShader(vertexShader, GL20.GL_VERTEX_SHADER),
            fragmentID = loadShader(fragmentShader, GL20.GL_FRAGMENT_SHADER),
            geometryID = geometryShader != null ? loadShader(geometryShader, GL32.GL_GEOMETRY_SHADER) : -1;

        GL20.glAttachShader(programID, vertexID);

        if (geometryID != -1) {
            GL20.glAttachShader(programID, geometryID);
        }

        GL20.glAttachShader(programID, fragmentID);
        GL20.glLinkProgram(programID);

        if (GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new ProgramLoadException(programID);
        }

        GL20.glValidateProgram(programID);
        GL20.glDetachShader(programID, vertexID);
        GL20.glDetachShader(programID, fragmentID);

        if (geometryID != -1) {
            GL20.glDetachShader(programID, geometryID);
        }

        return new Program(programID, fragmentID, -1, vertexID, instanced);
    }

    private static int loadShader(String shaderPath, int shaderType) throws IOException, ShaderLoadException {
        if (shaderIDLookup.containsKey(shaderPath)) {
            return shaderIDLookup.get(shaderPath);
        }

        var shaderID = GL20.glCreateShader(shaderType);
        var fileContent = new String(Files.readAllBytes(Paths.get(shaderPath)));

        GL20.glShaderSource(shaderID, fileContent);
        GL20.glCompileShader(shaderID);

        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new ShaderLoadException(shaderID);
        }

        shaderIDLookup.put(shaderPath, shaderID);

        return shaderID;
    }

    private static void unloadShader(int shaderID) {
        GL20.glDeleteShader(shaderID);
    }

    private static void reference(int fragmentID, int geometryID, int vertexID) {
        referenceCount.put(fragmentID, referenceCount.getOrDefault(fragmentID, 0));
        referenceCount.put(vertexID, referenceCount.getOrDefault(vertexID, 0));

        if (geometryID != -1) {
            referenceCount.put(geometryID, referenceCount.getOrDefault(geometryID, 0));
        }
    }

    private static void dereference(int fragmentID, int geometryID, int vertexID) {
        referenceCount.put(fragmentID, referenceCount.get(fragmentID) - 1);
        referenceCount.put(vertexID, referenceCount.get(vertexID) - 1);

        if (geometryID != -1) {
            referenceCount.put(geometryID, referenceCount.get(geometryID) - 1);
        }

        if (referenceCount.get(fragmentID) == 0) {
            unloadShader(fragmentID);
        }

        if (geometryID != -1 && referenceCount.get(geometryID) == 0) {
            unloadShader(geometryID);
        }

        if (referenceCount.get(vertexID) == 0) {
            unloadShader(vertexID);
        }
    }

    public static class Program {
        private final int programID;
        private final int fragmentID;
        private final int geometryID;
        private final int vertexID;
        private final boolean instanced;
        private boolean active = false;

        private Program(int programID, int fragmentID, int geometryID, int vertexID, boolean instanced) {
            this.programID = programID;
            this.fragmentID = fragmentID;
            this.geometryID = geometryID;
            this.vertexID = vertexID;
            this.instanced = instanced;

            reference(fragmentID, geometryID, vertexID);
        }

        public boolean isInstanced() {
            return instanced;
        }

        public void setUniform(String uniform, boolean value) {
            if (!active) GL20.glUseProgram(programID);
            GL20.glUniform1i(GL20.glGetUniformLocation(programID, uniform), value ? 1 : 0);
            if (!active) GL20.glUseProgram(0);
        }

        public void setUniform(String uniform, float value) {
            if (!active) GL20.glUseProgram(programID);
            GL20.glUniform1f(GL20.glGetUniformLocation(programID, uniform), value);
            if (!active) GL20.glUseProgram(0);
        }

        public void setUniform(String uniform, float[] value) {
            if (!active) GL20.glUseProgram(programID);
            if (value.length == 4) {
                GL20.glUniform4f(GL20.glGetUniformLocation(programID, uniform), value[0], value[1], value[2], value[3]);
            }
            else if (value.length == 2) {
                GL20.glUniform2f(GL20.glGetUniformLocation(programID, uniform), value[0], value[1]);
            }
            else {
                throw new UnsupportedOperationException("Invalid value length (" + value.length + ")");
            }
            if (!active) GL20.glUseProgram(0);
        }

        public void setUniform(String uniform, vec2f value) {
            if (!active) GL20.glUseProgram(programID);
            GL20.glUniform2f(GL20.glGetUniformLocation(programID, uniform), value.x, value.y);
            if (!active) GL20.glUseProgram(0);
        }

        public void setUniform(String uniform, vec3f value) {
            if (!active) GL20.glUseProgram(programID);
            GL20.glUniform3f(GL20.glGetUniformLocation(programID, uniform), value.x, value.y, value.z);
            if (!active) GL20.glUseProgram(0);
        }

        public void setUniform(String uniform, mat4f value) {
            if (!active) GL20.glUseProgram(programID);
            GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(programID, uniform), true, value.el);
            if (!active) GL20.glUseProgram(0);
        }

        public void start() {
            GL20.glUseProgram(programID);
            active = true;
        }

        public void stop() {
            GL20.glUseProgram(0);
            active = false;
        }

        public void destroy() {
            dereference(fragmentID, geometryID, vertexID);
            GL20.glDeleteProgram(programID);
        }
    }

    public static class ShaderLoadException extends Exception {
        ShaderLoadException(int shaderID) {
            super(GL20.glGetShaderInfoLog(shaderID));
        }
    }

    public static class ProgramLoadException extends Exception {
        ProgramLoadException(int programID) {
            super(GL20.glGetProgramInfoLog(programID));
        }
    }
}
