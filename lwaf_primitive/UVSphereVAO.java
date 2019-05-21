package lwaf_primitive;

import lwaf_core.vec3;

@SuppressWarnings({"unused", "WeakerAccess"})
public class UVSphereVAO extends GenericSmoothSpheroidVAO {
    public final int verticalPoints, horizontalPoints;

    public UVSphereVAO(int verticalPoints, int horizontalPoints) {
        this.verticalPoints = verticalPoints;
        this.horizontalPoints = horizontalPoints;

        var elements = genElements(horizontalPoints, verticalPoints);
        var vertices = genVertices(horizontalPoints, verticalPoints);

        setVertexCount(elements.length);
        genSpheroidBuffers(vertices);
        genColourBuffer(vertices.length);
        genUVBuffer(genUVs(horizontalPoints, verticalPoints));
        genElementBuffer(elements);
    }

    private static vec3[] genVertices(int w, int h) {
        var vs = new vec3[(w + 1) * h + 2 * w];
        var v = 0;

        for (int yt = 0; yt < h; ++yt) {
            for (int xt = 0; xt <= w; ++xt) {
                double yTheta = (double) (yt + 1) / (h + 1) * Math.PI;
                double xTheta = (double) xt / w * Math.PI * 2;
                double y = Math.cos(yTheta);
                double rl = Math.sin(yTheta);
                double x = rl * Math.sin(xTheta);
                double z = rl * Math.cos(xTheta);

                vs[v++] = new vec3((float) x, (float) y, (float) z).normalise();
            }
        }

        for (int i = 0; i < w; ++i) {
            vs[v++] = new vec3(0, 1, 0);
            vs[v++] = new vec3(0, -1, 0);
        }

        return vs;
    }

    private static float[] genUVs(int w, int h) {
        var uvs = new float[(w + 1) * h * 2 + 4 * w];
        var uv = 0;

        for (int yt = 0; yt < h; ++yt) {
            for (int xt = 0; xt <= w; ++xt) {
                uvs[uv++] = (float) xt / w;
                uvs[uv++] = (float) (yt + 1) / (h + 1);
            }
        }

        for (int i = 0; i < w; ++i) {
            uvs[uv++] = (float) i / w;
            uvs[uv++] = 0;
            uvs[uv++] = (float) i / w;
            uvs[uv++] = 1;
        }

        return uvs;
    }

    private static int[] genElements(int w, int h) {
        var elements = new int[6 * h * (w + 1)];
        var i = 0;
        var top = (w + 1) * h;
        var bottom = top + 1;

        for (int y = 0; y < h - 1; ++y) {
            for (int x = 0; x <= w; ++x) {
                var a = y * (w + 1) + x;
                var b = y * (w + 1) + (x+1);
                var c = (y+1) * (w + 1) + x;
                var d = (y+1) * (w + 1) + (x+1);

                elements[i++] = c;
                elements[i++] = b;
                elements[i++] = a;
                elements[i++] = b;
                elements[i++] = c;
                elements[i++] = d;
            }
        }

        for (int x = 0; x <= w; ++x) {
            var a = x % (w + 1);
            var b = (x+1) % (w + 1);

            elements[i++] = top + x * 2;
            elements[i++] = a;
            elements[i++] = b;
            elements[i++] = bottom + x * 2;
            elements[i++] = (w + 1) * (h-1) + b;
            elements[i++] = (w + 1) * (h-1) + a;
        }

        return elements;
    }
}
