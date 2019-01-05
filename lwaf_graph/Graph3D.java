package lwaf_graph;

import lwaf.VAO;
import lwaf.vec2f;
import lwaf.vec3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Graph3D {
    private final SurfaceMap function;
    private SurfaceMap curvature = v -> 1;
    private ColourMap colouring = v -> vec3f.one;
    private vec2f bounds_min = new vec2f(-1, -1), bounds_max = new vec2f(1, 1);
    private float curvature_threshold = 0.1f;
    private int min_expansions = 0;
    private int max_triangles = 10000;

    public Graph3D(SurfaceMap function) {
        this.function = function;
    }

    public Graph3D setCurvatureFunction(SurfaceMap curvature) {
        this.curvature = curvature;
        return this;
    }

    public Graph3D setBounds(vec2f min, vec2f max) {
        this.bounds_min = min;
        this.bounds_max = max;
        return this;
    }

    public Graph3D setColouring(ColourMap colouring) {
        this.colouring = colouring;
        return this;
    }

    public Graph3D setCurvatureThreshold(float curvatureThreshold) {
        this.curvature_threshold = curvatureThreshold;
        return this;
    }

    public Graph3D setMinExpansions(int minExpansions) {
        this.min_expansions = minExpansions;
        return this;
    }

    public Graph3D setMaxTriangles(int maxTriangles) {
        this.max_triangles = maxTriangles;
        return this;
    }

    public VAO getTriangulatedVAO(EvaluationStrategy strategy) {
        List<Tri> triangles = strategy.generateTriangles(this);

        var vertices = new vec3f[triangles.size() * 3];
        var normals  = new vec3f[triangles.size() * 3];
        var colours = new vec3f[triangles.size() * 3];
        var uvs = new vec2f[triangles.size() * 3];
        var elements = new int[triangles.size() * 3];
        var functionCache = new HashMap<vec2f, vec3f>();
        var vertex = 0;

        for (var tri : triangles) {
            var p0 = eval(tri.points[0], functionCache);
            var p1 = eval(tri.points[1], functionCache);
            var p2 = eval(tri.points[2], functionCache);
            var n  = p1.sub(p0).cross(p2.sub(p0)).normalise();

            vertices[vertex    ] = p0;
            vertices[vertex + 1] = p1;
            vertices[vertex + 2] = p2;

            normals[vertex    ] = n;
            normals[vertex + 1] = n;
            normals[vertex + 2] = n;

            uvs[vertex    ] = new vec2f(0.5f + p0.x, 0.5f + p0.z);
            uvs[vertex + 1] = new vec2f(0.5f + p1.x, 0.5f + p1.z);
            uvs[vertex + 2] = new vec2f(0.5f + p2.x, 0.5f + p2.z);

            vertex += 3;
        }

        for (int i = 0; i < vertices.length; ++i) {
            colours[i] = colouring.apply(vertices[i]);
        }

        for (int i = 0; i < elements.length; ++i) {
            elements[i] = i;
        }

        return new VAO() {{
            setVertexCount(elements.length);
            genVertexBuffer(vec3fToFloatArray(vertices));
            genNormalBuffer(vec3fToFloatArray(normals));
            genColourBuffer(vec3fToFloatArray(colours));
            // genUVBuffer(vec2fToFloatArray(uvs));
            genElementBuffer(elements);
        }};
    }

    public static SurfaceMap curvatureFromComponents(Function<vec2f, Float> d2fdx2, Function<vec2f, Float> d2fdy2) {
        return v -> new vec2f(d2fdx2.apply(v), d2fdy2.apply(v)).length();
    }

    @FunctionalInterface
    public interface SurfaceMap {
        float apply(vec2f location);
    }

    @FunctionalInterface
    public interface ColourMap {
        vec3f apply(vec3f location);
    }

    public vec3f eval(vec2f position, Map<vec2f, vec3f> cache) {
        cache.computeIfAbsent(position, v -> {
            var pos = bounds_min.add(bounds_max.sub(bounds_min).mul(v.add(new vec2f(0.5f, 0.5f))));
            return new vec3f(v.x, function.apply(pos), v.y);
        });
        return cache.get(position);
    }

    private static class Tri {
        final vec2f[] points;

        Tri(vec2f... points) {
            this.points = points;
        }

        vec2f centre() {
            return points[0].add(points[1]).add(points[2]).div(3);
        }

        List<Tri> divide() {
            var c = centre();
            var result = new ArrayList<Tri>();

            result.add(new Tri(c, points[0], points[1]));
            result.add(new Tri(c, points[1], points[2]));
            result.add(new Tri(c, points[2], points[0]));

            return result;
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

        @Override
        protected List<Tri> generateTriangles(Graph3D graph) {
            List<Tri> result = new ArrayList<>(resolution * resolution * 2);

            for (int xi = 0; xi < resolution; ++xi) {
                var x0 = (float) xi / resolution - 0.5f;
                var x1 = (float) (xi + 1) / resolution - 0.5f;

                for (int zi = 0; zi < resolution; ++zi) {
                    var z0 = (float) zi / resolution - 0.5f;
                    var z1 = (float) (zi + 1) / resolution - 0.5f;
                    var a = new vec2f(x0, z0);
                    var b = new vec2f(x0, z1);
                    var c = new vec2f(x1, z1);
                    var d = new vec2f(x1, z0);

                    result.add(new Tri(a, b, c));
                    result.add(new Tri(a, c, d));
                }
            }

            return result;
        }
    }

    public static class GradientPullStrategy extends UniformGridStrategy {
        public GradientPullStrategy(int resolution) {
            super(resolution);
        }

        @Override
        protected List<Tri> generateTriangles(Graph3D graph) {
            var points = new vec2f[resolution + 1][resolution + 1];
            var points_out = new vec2f[resolution + 1][resolution + 1];
            var cache = new HashMap<vec2f, vec3f>();
            float max_pull = 0.4f / resolution;

            for (int xi = 0; xi <= resolution; ++xi) {
                var x = (float) xi / resolution - 0.5f;

                for (int zi = 0; zi <= resolution; ++zi) {
                    var z = (float) zi / resolution - 0.5f;

                    points[xi][zi] = new vec2f(x, z);
                }
            }

            for (int xi = 0; xi <= resolution; ++xi) {
                for (int zi = 0; zi <= resolution; ++zi) {
                    var grad_x = 0f;
                    var grad_z = 0f;
                    var k = 2f / resolution;
                    var y  = graph.eval(points[xi][zi], cache).y;

                    if (xi > 0 && xi < resolution) {
                        var y0x = graph.eval(points[xi - 1][zi], cache).y;
                        var y1x = graph.eval(points[xi + 1][zi], cache).y;

                        grad_x = (y1x - y0x) * k;
                        if (Math.abs(grad_x) > max_pull) grad_x = grad_x > 0 ? max_pull : -max_pull;
                    }

                    if (zi > 0 && zi < resolution) {
                        var y0z = graph.eval(points[xi][zi - 1], cache).y;
                        var y1z = graph.eval(points[xi][zi + 1], cache).y;

                        grad_z = (y1z - y0z) * k;
                        if (Math.abs(grad_z) > max_pull) grad_z = grad_z > 0 ? max_pull : -max_pull;
                    }

                    points_out[xi][zi] = new vec2f(points[xi][zi].x + grad_x, points[xi][zi].y + grad_z);

                    System.out.println(grad_x + ", " + grad_z);
                }
            }

            List<Tri> result = new ArrayList<>(resolution * resolution * 2);

            for (int xi = 0; xi < resolution; ++xi) {
                for (int zi = 0; zi < resolution; ++zi) {
                    var a = points_out[xi][zi];
                    var b = points_out[xi][zi + 1];
                    var c = points_out[xi + 1][zi + 1];
                    var d = points_out[xi + 1][zi];

                    result.add(new Tri(a, b, c));
                    result.add(new Tri(a, c, d));
                }
            }

            return result;
        }
    }
}
