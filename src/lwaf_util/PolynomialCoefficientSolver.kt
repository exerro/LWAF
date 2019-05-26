package lwaf_util

class PolynomialCoefficientSolver(private val order: Int) {
    private val matrix: Array<FloatArray> = Array(order) { FloatArray(order) }
    private val composition: FloatArray = FloatArray(order)

    fun setRow(row: Int, vararg value: Float) {
        assert(value.size == order)
        matrix[row] = value
    }

    fun setValues(vararg values: Float) {
        assert(values.size == order)
        System.arraycopy(values, 0, composition, 0, values.size)
    }

    fun solve(): FloatArray {
        val result = FloatArray(order)

        for (i in 0 until order) {
            for (j in i + 1 until order) {
                subtractRow(j, i, i)
            }
        }

        for (i in order - 2 downTo 0) {
            for (j in i + 1 until order) {
                subtractRow(i, j, j)
            }
        }

        for (i in 0 until order) {
            result[i] = composition[i] / matrix[i][i]
        }

        return result
    }

    private fun subtractRow(row: Int, sourceRow: Int, elementToZero: Int) {
        val multiple = matrix[row][elementToZero] / matrix[sourceRow][elementToZero]
        subMultiple(row, sourceRow, multiple)
    }

    private fun subMultiple(row: Int, sourceRow: Int, multiple: Float) {
        for (i in 0 until order) {
            matrix[row][i] -= matrix[sourceRow][i] * multiple
        }

        composition[row] -= composition[sourceRow] * multiple
    }
}
