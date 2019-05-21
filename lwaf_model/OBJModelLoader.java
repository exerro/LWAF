package lwaf_model;

import lwaf_3D.Material;
import lwaf_core.GLVAO;
import lwaf_core.GLVAOKt;
import lwaf_core.vec2;
import lwaf_core.vec3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OBJModelLoader {

    public static Model<GLVAO> loadModel(String file, String basePath) throws FileNotFoundException {
        System.out.println("Loading " + file);

        var parsed = readObjectLines(file);
        var model = new Model<>();

        var vertices = parsed.dataLines
                .getOrDefault("v", Collections.emptyList())
                .stream()
                .map(OBJModelLoader.parseVecNf(3))
                .collect(Collectors.toList())
                ;
        var normals = parsed.dataLines
                .getOrDefault("vn", Collections.emptyList())
                .stream()
                .map(OBJModelLoader.parseVecNf(3))
                .collect(Collectors.toList())
                ;
        var uvs = parsed.dataLines
                .getOrDefault("vt", Collections.emptyList())
                .stream()
                .map(OBJModelLoader.parseVecNf(2))
                .map(v -> new vec2(v.getX(), 1 - v.getY())) // why does 1-v.y work???
                .collect(Collectors.toList())
                ;

        System.out.println(vertices.size() + " vertices; " + normals.size() + " normals; " + uvs.size() + " uvs; " + parsed.objects.size() + " objects");

        var vertexCache = new HashMap<int[], Integer>();

        for (String objectName : parsed.objects.keySet()) {
            var vao = loadObjectVAO(parsed.objects.get(objectName), vertices, normals, uvs, vertexCache);
            var material = new Material();

            System.out.println("Object '" + objectName + "': " + parsed.objects.get(objectName).getOrDefault("f", Collections.emptyList()).size() + " faces");

            model.addObject(objectName, vao, material);
        }

        return model;
    }

    public static Model<GLVAO> loadModel(String file) throws FileNotFoundException {
        var basePath = Paths.get(file).getParent();
        return loadModel(file, basePath == null ? "" : basePath.toString());
    }

    public static Model<GLVAO> safeLoadModel(String file, String basePath) {
        try {
            return loadModel(file, basePath);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
            while (true);
        }
    }

    public static Model<GLVAO> safeLoadModel(String file) {
        try {
            return loadModel(file);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
            while (true);
        }
    }

    private static GLVAO loadObjectVAO(Map<String, List<String>> linesData, List<vec3> vertices, List<vec3> normals, List<vec2> uvs, HashMap<int[], Integer> vertexCache) {
        var faces = linesData
                .getOrDefault("f", Collections.emptyList())
                .stream()
                .map(OBJModelLoader::parseFace)
                .flatMap(Stream::of)
                .collect(Collectors.toList())
                ;

        var new_vertices = new ArrayList<vec3>();
        var new_normals = new ArrayList<vec3>();
        var new_uvs = new ArrayList<vec2>();
        var elements = new int[faces.size() * 3];
        var ei = 0;

        for (int[][] face : faces) {
            // note, here, that face[vertex] is { position, uv, normal }
            var hasUndefinedNormal = face[0][2] == -1 || face[1][2] == -1 || face[2][2] == -1;
            var normal = hasUndefinedNormal ? calculateNormal(face[0][0], face[1][0], face[2][0], vertices) : new vec3(0f, 0f, 0f);

            for (int v = 0; v < 3; ++v) {
                var set = false;

                if (face[v][1] == -1 || face[v][2] == -1) {
                    set = true;
                    elements[ei++] = new_vertices.size();
                }
                else {
                    if (!vertexCache.containsKey(face[v])) {
                        set = true;
                        vertexCache.put(face[v], new_vertices.size());
                    }

                    elements[ei++] = vertexCache.get(face[v]);
                }

                if (set) {
                    new_vertices.add(vertices.get(face[v][0] - 1));
                    new_uvs.add(face[v][1] == -1 ? new vec2(0, 0) : uvs.get(face[v][1] - 1));
                    new_normals.add((face[v][2] == -1 ? normal : normals.get(face[v][2] - 1)).normalise());
                }
            }
        }

        return GLVAOKt.generateStandardVAO(
                new_vertices.toArray(new vec3[0]),
                new_normals.toArray(new vec3[0]),
                null,
                new_uvs.toArray(new vec2[0]),
                elements
        );
    }

    private static Function<String, vec3> parseVecNf(int n) {
        return s -> {
            var numberMatcher = numberPattern.matcher(s);
            var ns = new float[]{0, 0, 0};

            for (int i = 0; i < n && numberMatcher.find(); ++i) {
                ns[i] = Float.parseFloat(numberMatcher.group(0));
            }

            return new vec3(ns[0], ns[1], ns[2]);
        };
    }

    private static int[][][] parseFace(String s) {
        var sectionMatcher = faceSectionPattern.matcher(s);
        var sections = new ArrayList<int[]>();

        while (sectionMatcher.find()) {
            var sVertex = sectionMatcher.group(1);
            var sUV = sectionMatcher.group(2);
            var sNormal = sectionMatcher.group(3);

            sections.add(new int[] {
                    Integer.parseInt(sVertex),
                    sUV == null || sUV.equals("") ? -1 : Integer.parseInt(sUV),
                    sNormal == null || sNormal.equals("") ? -1 : Integer.parseInt(sNormal)
            });
        }

        var faces = new int[sections.size() - 2][][];

        for (int i = 0; i < sections.size() - 2; ++i) {
            var s0 = sections.get(0);
            var s1 = sections.get(i + 1);
            var s2 = sections.get(i + 2);

            faces[i] = new int[][] {s0, s1, s2};
        }

        return faces;
    }

    private static ObjectLines readObjectLines(String file) throws FileNotFoundException {
        var reader = new BufferedReader(new FileReader(file));
        var objects = new HashMap<String, Map<String, List<String>>>();
        var lines = new HashMap<String, List<String>>();
        var objectName = Model.DEFAULT_OBJECT_NAME;

        for (var line : reader.lines().collect(Collectors.toList())) {
            line = sanitiseLine(line);

            if (line.length() == 0) continue;
            if (line.charAt(0) < 'a' || line.charAt(0) > 'z') continue;

            var type = getLineType(line);
            var data = getLineData(line);

            if (type.equals("o")) {
                objectName = data;
            }
            else if (type.equals("v") || type.equals("vt") || type.equals("vn")) {
                lines.computeIfAbsent(type, (eh) -> new ArrayList<>());
                lines.get(type).add(data);
            }
            else {
                objects.computeIfAbsent(objectName, (eh) -> new HashMap<>());
                objects.get(objectName).computeIfAbsent(type, (eh) -> new ArrayList<>());
                objects.get(objectName).get(type).add(data);
            }
        }

        return new ObjectLines(lines, objects);
    }

    private static String getLineType(String line) {
        return line.contains(" ") ? line.substring(0, line.indexOf(" ")) : line;
    }

    private static String getLineData(String line) {
        return line.contains(" ") ? line.substring(line.indexOf(" ") + 1) : "";
    }

    private static String sanitiseLine(String line) {
        if (line.contains("#")) {
            line = line.substring(0, line.indexOf("#"));
        }

        return line.toLowerCase();
    }

    private static vec3 calculateNormal(int v0n, int v1n, int v2n, List<vec3> vertices) {
        var v0 = vertices.get(v0n - 1);
        var v1 = vertices.get(v1n - 1);
        var v2 = vertices.get(v2n - 1);
        var n = v1.sub(v0).cross(v2.sub(v0));
        return n;
    }

    private static class ObjectLines {
        final Map<String, List<String>> dataLines;
        final Map<String, Map<String, List<String>>> objects;

        private ObjectLines(Map<String, List<String>> dataLines, Map<String, Map<String, List<String>>> objects) {
            this.dataLines = dataLines;
            this.objects = objects;
        }
    }

    private static final Pattern
        numberPattern = Pattern.compile("\\-?\\d*\\.?\\d+"),
        faceSectionPattern = Pattern.compile("(\\d+)(?:/(\\d*)(?:/(\\d+))?)?")
    ;

}
