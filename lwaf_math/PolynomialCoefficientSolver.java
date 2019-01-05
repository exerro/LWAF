package lwaf_math;

public class PolynomialCoefficientSolver {
    private final int order;
    private final float[][] matrix;
    private final float[] composition;

    public PolynomialCoefficientSolver(int order) {
        this.order = order;
        this.matrix = new float[order][order];
        this.composition = new float[order];
    }

    public void setRow(int row, float ...value) {
        assert value.length == order;
        matrix[row] = value;
    }

    public void setValues(float ...values) {
        assert values.length == order;
        System.arraycopy(values, 0, composition, 0, values.length);
    }

    public float[] solve() {
        float[] result = new float[order];

        for (int i = 0; i < order; ++i) {
            for (int j = i + 1; j < order; ++j) {
                subtractRow(j, i, i);
            }
        }

        for (int i = order - 2; i >= 0; --i) {
            for (int j = i + 1; j < order; ++j) {
                subtractRow(i, j, j);
            }
        }

        for (int i = 0; i < order; ++i) {
            result[i] = composition[i] / matrix[i][i];
        }

        return result;
    }

    private void subtractRow(int row, int sourceRow, int elementToZero) {
        float multiple = matrix[row][elementToZero] / matrix[sourceRow][elementToZero];
        subMultiple(row, sourceRow, multiple);
    }

    private void subMultiple(int row, int sourceRow, float multiple) {
        for (int i = 0; i < order; ++i) {
            matrix[row][i] -= matrix[sourceRow][i] * multiple;
        }

        composition[row] -= composition[sourceRow] * multiple;
    }
}
