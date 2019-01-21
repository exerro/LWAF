package lwaf_model;

import lwaf.VAO;
import lwaf.vec2f;
import lwaf.vec3f;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelLoader {

    public static VAO loadVAO(String file) throws IOException {
        var content = new String(Files.readAllBytes(Paths.get(file)));
        var vertexMatcher = vertexPatternMatcher.matcher(content);
        var normalMatcher = normalPatternMatcher.matcher(content);
        var uvMatcher = uvPatternMatcher.matcher(content);
        var faceMatcher = facePatternMatcher.matcher(content);
        var faceMatcher2 = facePatternMatcher2.matcher(content);
        var vertices = readMatcherToVec3f(vertexMatcher);
        var normals = readMatcherToVec3f(normalMatcher);
        var uvs = readMatcherToVec2f(uvMatcher);
        var faces = readMatcherToFaceList(faceMatcher); faces.addAll(readMatcherToFaceList(faceMatcher2));
        var new_vertices = new ArrayList<vec3f>();
        var new_normals = new ArrayList<vec3f>();
        var new_uvs = new ArrayList<vec2f>();
        var allocated = new HashMap<int[], Integer>();
        var elements = new int[faces.size() * 3];
        var ei = 0;

        for (var face : faces) {
            for (int i = 0; i < 3; ++i) {
                elements[ei++] = getElement(face[i], vertices, normals, uvs, allocated, new_vertices, new_normals, new_uvs);
            }
        }

        return new VAO(
                new_vertices.toArray(new vec3f[0]),
                new_normals.toArray(new vec3f[0]),
                null,
                new_uvs.toArray(new vec2f[0]),
                elements
        );
    }

    public static VAO safeLoadVAO(String file) {
        try {
            return loadVAO(file);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            while (true);
        }
    }

    public static Model<VAO> load(String file) throws IOException {
        return new Model<>(loadVAO(file));
    }

    public static Model<VAO> safeLoad(String file) {
        return new Model<>(safeLoadVAO(file));
    }

    private static int getElement(int[] vertex_uv_normal, List<vec3f> vertices, List<vec3f> normals, List<vec2f> uvs, HashMap<int[], Integer> allocated, List<vec3f> new_vertices, List<vec3f> new_normals, List<vec2f> new_uvs) {
        if (!allocated.containsKey(vertex_uv_normal)) {
            new_vertices.add(vertices.get(vertex_uv_normal[0] - 1));
            new_uvs.add(vertex_uv_normal[1] == 0 ? vec2f.zero : uvs.get(vertex_uv_normal[1] - 1));
            new_normals.add(vertex_uv_normal[2] == 0 ? vec3f.zero : normals.get(vertex_uv_normal[2] - 1));

            allocated.put(vertex_uv_normal, new_vertices.size() - 1);
        }

        return allocated.get(vertex_uv_normal);
    }

    private static List<vec3f> readMatcherToVec3f(Matcher matcher) {
        var result = new ArrayList<vec3f>();

        while (matcher.find()) {
            var x = Float.parseFloat(matcher.group(1));
            var y = Float.parseFloat(matcher.group(2));
            var z = Float.parseFloat(matcher.group(3));
            result.add(new vec3f(x, y, z));
        }

        return result;
    }

    private static List<vec2f> readMatcherToVec2f(Matcher matcher) {
        var result = new ArrayList<vec2f>();

        while (matcher.find()) {
            var x = Float.parseFloat(matcher.group(1));
            var y = Float.parseFloat(matcher.group(2));
            result.add(new vec2f(x, 1-y));
        }

        return result;
    }

    private static List<int[][]> readMatcherToFaceList(Matcher matcher) {
        var result = new ArrayList<int[][]>();

        while (matcher.find()) {
            var face = new int[3][3];

            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 3; ++j) {
                    var n = matcher.group(i * 3 + j + 1);
                    face[i][j] = Integer.parseInt(n.equals("") ? "0" : n);
                }
            }

            result.add(face);
        }

        return result;
    }

    private static Pattern vertexPatternMatcher, normalPatternMatcher, uvPatternMatcher, facePatternMatcher, facePatternMatcher2; static {
        vertexPatternMatcher = Pattern.compile(
                "v\\s+" +
                        "([+-]?\\d+\\.\\d+)\\s+" +
                        "([+-]?\\d+\\.\\d+)\\s+" +
                        "([+-]?\\d+\\.\\d+)"
        );
        normalPatternMatcher = Pattern.compile(
                "vn\\s+" +
                        "([+-]?\\d+\\.\\d+)\\s+" +
                        "([+-]?\\d+\\.\\d+)\\s+" +
                        "([+-]?\\d+\\.\\d+)"
        );
        uvPatternMatcher = Pattern.compile(
                "vt\\s+" +
                        "([+-]?\\d+\\.\\d+)\\s+" +
                        "([+-]?\\d+\\.\\d+)"
        );
        facePatternMatcher = Pattern.compile(
                "f\\s+" +
                        "(\\d+)/(\\d*)/(\\d+)\\s+" +
                        "(\\d+)/(\\d*)/(\\d+)\\s+" +
                        "(\\d+)/(\\d*)/(\\d+)"
        );
        facePatternMatcher2 = Pattern.compile(
                "f\\s+" +
                        "(\\d+)/(\\d+)()\\s+" +
                        "(\\d+)/(\\d+)()\\s+" +
                        "(\\d+)/(\\d+)()"
        );
    }
}
