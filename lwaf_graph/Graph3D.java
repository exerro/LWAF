package lwaf_graph;

import lwaf_core.GLVAO;
import lwaf_core.GLVAOKt;
import lwaf_core.vec2;
import lwaf_core.vec3;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Graph3D {
    private final SurfaceMap function;
    private ColourMap colouring = v -> new vec3(1, 1, 1);
    private vec2 bounds_min = new vec2(-1, -1), bounds_max = new vec2(1, 1);

    public Graph3D(SurfaceMap function) {
        this.function = function;
    }

    public Graph3D setBounds(vec2 min, vec2 max) {
        this.bounds_min = min;
        this.bounds_max = max;
        return this;
    }

    public Graph3D setColouring(ColourMap colouring) {
        this.colouring = colouring;
        return this;
    }

    public GLVAO getTriangulatedVAO(EvaluationStrategy strategy) {
        var triangles = strategy.generateTriangles(this);
        var vertices = new vec3[triangles.size() * 3];
        var normals  = new vec3[triangles.size() * 3];
        var colours = new vec3[triangles.size() * 3];
        var uvs = new vec2[triangles.size() * 3];
        var elements = new int[triangles.size() * 3];
        var lookup = buildVertexLookup(triangles);
        var vertex = 0;

        for (var tri : triangles) {
            var p0 = lookup.get(tri.points[0]);
            var p1 = lookup.get(tri.points[1]);
            var p2 = lookup.get(tri.points[2]);
            var n  = p1.sub(p0).cross(p2.sub(p0)).normalise();

            vertices[vertex    ] = p0;
            vertices[vertex + 1] = p1;
            vertices[vertex + 2] = p2;

            normals[vertex    ] = n;
            normals[vertex + 1] = n;
            normals[vertex + 2] = n;

            uvs[vertex    ] = new vec2(0.5f + p0.getX(), 0.5f + p0.getZ());
            uvs[vertex + 1] = new vec2(0.5f + p1.getX(), 0.5f + p1.getZ());
            uvs[vertex + 2] = new vec2(0.5f + p2.getX(), 0.5f + p2.getZ());

            vertex += 3;
        }

        for (int i = 0; i < colours.length; ++i) {
            colours[i] = colouring.apply(vertices[i]);
        }

        for (int i = 0; i < elements.length; ++i) {
            elements[i] = i;
        }

        return GLVAOKt.generateStandardVAO(vertices, normals, colours, uvs, elements);
    }

    public GLVAO getSmoothVAO(EvaluationStrategy strategy) {
        var triangles = strategy.generateTriangles(this);
        var lookup = buildVertexLookup(triangles);
        var elements = new int[triangles.size() * 3];
        var el = 0;

        List<vec2> vertex_list;
        List<vec3> accumulated_normals;
        Map<vec2, Integer> vertex_indexes;
        vec3[] vertices, normals, colours;
        vec2[] uvs;

        vertex_list = triangles
                .stream()
                .flatMap(tri -> Stream.of(tri.points))
                .distinct()
                .collect(Collectors.toList());

        vertex_indexes = IntStream.range(0, vertex_list.size())
                .parallel()
                .boxed()
                .collect(Collectors.toMap(
                        vertex_list::get,
                        Function.identity()
                ));

        accumulated_normals = new ArrayList<>(Collections.nCopies(vertex_list.size(), new vec3(0, 0, 0)));

        for (var triangle : triangles) {
            var p0 = triangle.points[0];
            var p1 = triangle.points[1];
            var p2 = triangle.points[2];
            var p0v = lookup.get(p0);
            var p1v = lookup.get(p1);
            var p2v = lookup.get(p2);
            var p0i = vertex_indexes.get(p0);
            var p1i = vertex_indexes.get(p1);
            var p2i = vertex_indexes.get(p2);
            var p0n = accumulated_normals.get(p0i);
            var p1n = accumulated_normals.get(p1i);
            var p2n = accumulated_normals.get(p2i);
            var n = p1v.sub(p0v).cross(p2v.sub(p0v));

            p0n = p0n.add(n);
            p1n = p1n.add(n);
            p2n = p2n.add(n);
            accumulated_normals.set(p0i, p0n);
            accumulated_normals.set(p1i, p1n);
            accumulated_normals.set(p2i, p2n);
        }

        accumulated_normals = accumulated_normals
                .parallelStream()
                .map(vec3::normalise)
                .collect(Collectors.toList());

        vertices = vertex_list
                .parallelStream()
                .map(lookup::get)
                .toArray(vec3[]::new);

        normals = accumulated_normals.toArray(new vec3[0]);

        colours = new vec3[vertices.length];
        for (int i = 0; i < colours.length; ++i) {
            colours[i] = colouring.apply(vertices[i]);
        }

        uvs = new vec2[vertices.length];
        for (int i = 0; i < uvs.length; ++i) {
            uvs[i] = new vec2(vertices[i].getX() + 0.5f, vertices[i].getZ() + 0.5f);
        }

        for (var triangle : triangles) {
            elements[el    ] = vertex_indexes.get(triangle.points[0]);
            elements[el + 1] = vertex_indexes.get(triangle.points[1]);
            elements[el + 2] = vertex_indexes.get(triangle.points[2]);
            el += 3;
        }

        return GLVAOKt.generateStandardVAO(vertices, normals, colours, uvs, elements);
    }

    public Map<vec2, vec3> buildVertexLookup(List<Tri> triangles) {
        var bounds_diff = bounds_max.sub(bounds_min);
        var half = new vec2(1f, 1f).mul(0.5f);

        return triangles
                .parallelStream()
                .flatMap(tri -> Stream.of(tri.points))
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        v -> {
                            vec2 pos = bounds_min.add(bounds_diff.mul(v.add(half)));
                            var val = function.apply(pos);
                            return new vec3(v.getX(), val, v.getY());
                        }
                ));
    }

    public vec3 eval(vec2 v) {
        var pos = bounds_min.add(bounds_max.sub(bounds_min).mul(v.add(new vec2(0.5f, 0.5f))));
        var val = function.apply(pos);
        return new vec3(pos.getX(), val, pos.getY());
    }

    public vec3 evalCached(vec2 v, Map<vec2, vec3> cache) {
        cache.computeIfAbsent(v, this::eval);
        return cache.get(v);
    }

    public Map<vec2, vec3> generateCache() {
        return new HashMap<>();
    }

    @FunctionalInterface
    public interface SurfaceMap {
        float apply(vec2 location);
    }

    @FunctionalInterface
    public interface ColourMap {
        vec3 apply(vec3 location);
    }

    private static class Tri {
        final vec2[] points;

        Tri(vec2... points) {
            this.points = points;
        }
    }

    private static abstract class EvaluationStrategy {
        protected abstract List<Tri> generateTriangles(Graph3D graph);
    }

    public static class UniformGridStrategy extends EvaluationStrategy {
        final int resolution;

        public UniformGridStrategy(int resolution) {
            this.resolution = resolution;
        }

        protected vec2[][] generateVertexArray() {
            var result = new vec2[resolution + 1][resolution + 1];

            for (int xi = 0; xi <= resolution; ++xi) {
                var x = (float) xi / resolution - 0.5f;

                for (int zi = 0; zi <= resolution; ++zi) {
                    var z = (float) zi / resolution - 0.5f;

                    result[xi][zi] = new vec2(x, z);
                }
            }

            return result;
        }

        protected List<Tri> generateTrianglesFromVertexArray(vec2[][] vertices) {
            var result = new ArrayList<Tri>(resolution * resolution * 2);

            for (int xi = 0; xi < resolution; ++xi) {
                for (int zi = 0; zi < resolution; ++zi) {
                    var a = vertices[xi][zi];
                    var b = vertices[xi][zi + 1];
                    var c = vertices[xi + 1][zi + 1];
                    var d = vertices[xi + 1][zi];

                    result.add(new Tri(a, b, c));
                    result.add(new Tri(a, c, d));
                }
            }

            return result;
        }

        @Override
        protected List<Tri> generateTriangles(Graph3D graph) {
            return generateTrianglesFromVertexArray(generateVertexArray());
        }
    }

    public static class GradientPullStrategy extends UniformGridStrategy {
        public GradientPullStrategy(int resolution) {
            super(resolution);
        }

        @Override
        protected List<Tri> generateTriangles(Graph3D graph) {
            var points = generateVertexArray();
            var points_out = new vec2[resolution + 1][resolution + 1];
            var cache = graph.generateCache();
            float max_pull = 0.4f / resolution;

            for (int xi = 0; xi <= resolution; ++xi) {
                for (int zi = 0; zi <= resolution; ++zi) {
                    var grad_x = 0f;
                    var grad_z = 0f;
                    var k = 2f / resolution;

                    if (xi > 0 && xi < resolution) {
                        var y0x = graph.evalCached(points[xi - 1][zi], cache).getY();
                        var y1x = graph.evalCached(points[xi + 1][zi], cache).getY();

                        grad_x = (y1x - y0x) * k;
                        if (Math.abs(grad_x) > max_pull) grad_x = grad_x > 0 ? max_pull : -max_pull;
                    }

                    if (zi > 0 && zi < resolution) {
                        var y0z = graph.evalCached(points[xi][zi - 1], cache).getY();
                        var y1z = graph.evalCached(points[xi][zi + 1], cache).getY();

                        grad_z = (y1z - y0z) * k;
                        if (Math.abs(grad_z) > max_pull) grad_z = grad_z > 0 ? max_pull : -max_pull;
                    }

                    points_out[xi][zi] = new vec2(points[xi][zi].getX() + grad_x, points[xi][zi].getY() + grad_z);
                }
            }

            return generateTrianglesFromVertexArray(points_out);
        }
    }
}
