package lwaf.util;

import lwaf.VAO;
import lwaf.vec3f;

import java.util.Arrays;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public class UVSphereVAO extends VAO {
    public final int verticalPoints, horizontalPoints;

    public UVSphereVAO(int verticalPoints, int horizontalPoints) {
        this.verticalPoints = verticalPoints;
        this.horizontalPoints = horizontalPoints;

        var elements = genElements(horizontalPoints, verticalPoints);
        var vertices = genVertices(horizontalPoints, verticalPoints);
        var colours = new float[vertices.length * 3];

        var normal_floats = vec3fToFloatArray(vertices);
        var vertex_floats = new float[vertices.length * 3];

        int vertexVBOID, normalVBOID, colourVBOID, elementVBOID;

        for (int i = 0; i < normal_floats.length; ++i) {
            vertex_floats[i] = normal_floats[i] / 2;
        }

        setVertexCount(elements.length);

        Arrays.fill(colours, 1);

        vertexVBOID = genBuffer();
        normalVBOID = genBuffer();
        colourVBOID = genBuffer();
        elementVBOID = genBuffer();

        bindBuffer(vertexVBOID, 0, 3, GL_FLOAT);
        bindBuffer(normalVBOID, 1, 3, GL_FLOAT);
        bindBuffer(colourVBOID, 2, 3, GL_FLOAT);

        enableAttribute(0);
        enableAttribute(1);
        enableAttribute(2);

        bufferData(vertexVBOID, vertex_floats, GL_STATIC_DRAW);
        bufferData(normalVBOID, normal_floats, GL_STATIC_DRAW);
        bufferData(colourVBOID, colours, GL_STATIC_DRAW);
        bufferElementData(elementVBOID, elements, GL_STATIC_DRAW);
    }

    private static vec3f[] genVertices(int w, int h) {
        var vs = new vec3f[w * h + 2];
        var v = 0;

        for (int yt = 0; yt < h; ++yt) {
            for (int xt = 0; xt < w; ++xt) {
                double yTheta = (double) (yt + 1) / (h + 1) * Math.PI;
                double xTheta = (double) xt / w * Math.PI * 2;
                double y = Math.cos(yTheta);
                double rl = Math.sin(yTheta);
                double x = rl * Math.sin(xTheta);
                double z = rl * Math.cos(xTheta);

                vs[v++] = new vec3f((float) x, (float) y, (float) z).normalise();
            }
        }

        vs[v++] = vec3f.y_axis;
        vs[v++] = vec3f.y_axis.unm();

        return vs;
    }

    private static int[] genElements(int w, int h) {
        int[] elements = new int[6 * h * (w + 1)];
        int i = 0;
        int top = w * h;
        int bottom = top + 1;

        for (int y = 0; y < h - 1; ++y) {
            for (int x = 0; x <= w; ++x) {
                int a = y * w + x % w,
                    b = y * w + (x+1) % w,
                    c = (y+1) * w + x % w,
                    d = (y+1) * w + (x+1) % w;

                elements[i++] = c;
                elements[i++] = b;
                elements[i++] = a;
                elements[i++] = b;
                elements[i++] = c;
                elements[i++] = d;
            }
        }

        for (int x = 0; x <= w; ++x) {
            int a = x % w,
                b = (x+1) % w;

            elements[i++] = top;
            elements[i++] = a;
            elements[i++] = b;
            elements[i++] = bottom;
            elements[i++] = w * (h-1) + b;
            elements[i++] = w * (h-1) + a;
        }

        return elements;
    }
}
