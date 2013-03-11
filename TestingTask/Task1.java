import java.util.Arrays;

/**
 * Сразу оговорюсь, что я не рассматривал длинную арифметику,
 * полагал, что ответ с промежуточными вычислениями укладывает в тип java.lang.Long
 */
public class Task1 {
    public static void main(String[] args) {
        System.out.println(AnaliticalFormula.getAnswer(3000L));
        System.out.println(AnaliticalFormula.getAnswer(11000L));
        System.out.println(AnaliticalFormula.getAnswer(InputData.MAX_VALUE));
    }
}

class InputData {
    public static final long MAX_VALUE = 4 * 1000 * 1000;
}

/**
 * Ответом на поставленную задачу является
 * sum(k=0..n) {fib(3*k}
 * где n = sup {k | fib(3*k) <= MAX_VALUE}
 * Иначе, n - наибольшее из чисел k, которые удовлетворяют неравенству fib(3*k) <= MAX_VALUE.
 *
 * Данную сумму можно предствить в виде:
 * sum(k=0..n) {fib(3*k} = (1/4) * [ fib(3n + 3) + fib(3n) - 2 ]
 *
 * Сложностью этого подхода является лишь вычисление n по заданному MAX_VALUE
 */
class AnaliticalFormula {
    static final Matrix FIB_START = new Matrix(new long[][] {new long[] {1L, 0L}}, 2);
    static final Matrix FIB_MATRIX = new Matrix(new long[][] {new long[] {1L, 1L}, new long[] {1L, 0L}}, 2);

    static Matrix[] fibPowerOfTwo;

    private static final int MAX_N_POW = 7;

    /**
     * Предвычиляем матрицы вида
     * (1 1) ^n    ( fib(n+1)  fib(n)   )
     * (   )     = (                    )
     * (1 0)       ( fib(n)    fib(n-1) )
     *
     * для n = 2^k | k <= MAX_N_POW, т.е. n - целая степень двойки.
     * Это необходимо только в тех случаях, когда алгоритм запускается более 1 раза.
     */
    static {
        fibPowerOfTwo = new Matrix[MAX_N_POW];
        fibPowerOfTwo[0] = FIB_MATRIX;
        for(int i = 1; i < MAX_N_POW; ++i) {
            fibPowerOfTwo[i] = fibPowerOfTwo[i - 1].multiply(fibPowerOfTwo[i - 1]);
        }
    }

    /**
     * Поиск матрицы с наибольшим четным числом Фибоначчи <= maxValue.
     * Идея схожа с бинарным поиском.
     *
     * @param start стартовая точка для бин. поиска
     * @param step шаг бин. поиска - FIB_MATRIX^2^step
     * @param maxValue
     * @return матрицу с наибольшим четным числом Фибоначчи <= maxValue
     */
    public static Matrix getLowerBoundFibonacciMatrix(Matrix start, int step, final long maxValue) {
        Matrix mul = fibPowerOfTwo[step];
        Matrix eval = start;
        Matrix last = eval;
        while (eval.getElement(0, 0) <= maxValue) {
            last = eval;
            eval = eval.multiply(mul);
        }
        if(eval.getElement(1, 0) <= maxValue) {
            return eval;
        } else {
            return getLowerBoundFibonacciMatrix(last, step - 1, maxValue);
        }
    }

    /**
     *
     * @param maxValue
     * @return сумму чётных чисел Фибоначчи меньших @code{maxValue}
     */
    public static long getAnswer(final long maxValue) {
        Matrix m = getLowerBoundFibonacciMatrix(FIB_MATRIX, MAX_N_POW - 1, maxValue).multiply(FIB_MATRIX);
        return (m.getElement(0, 0) + m.getElement(0, 1) + m.getElement(1, 1) - 2)/4;
    }
}

/**
 * Простейшая реализация, без параметризации, сложных конструктов и прямого доступа/манипуляции строк и столбцов.
 */
class Matrix {
    long[][] elements;
    int rowCount;
    int columnCount;

    /**
     *
     * @param a элементы новой матрицы
     * @param columnCount количество столбцов. В принципе, можно на вход подать и ступененчатый(зубчатый) массив.
     */
    Matrix(long[][] a, int columnCount) {
        this.rowCount = a.length;
        this.columnCount = columnCount;
        elements = new long[rowCount][];
        for(int i = 0; i < elements.length; ++i) {
            elements[i] = Arrays.copyOf(a[i], columnCount);
        }
    }

    long getElement(int row, int column) {
        return elements[row][column];
    }

    /**
     * Тривиальное умножение за O(n^3) арифмитечиских операций.
     * @param that
     * /@return матрицу B равную произведению данной матрицы на матрицу that
     */
    Matrix multiply(Matrix that) {
        if(this.columnCount != that.rowCount) {
            //нужно бросить исключению. Но не буду усложнять логику, ибо
            //придётся добавлять catch-блоки, а также реализовывать класс исключения
            return null;
        }
        long[][] newMatrix = new long[this.rowCount][that.columnCount];
        for(int i = 0; i < this.rowCount; ++i) {
            for(int j = 0; j < that.columnCount; ++j) {
                long ceilValue = 0;
                for(int k = 0; k < this.columnCount; ++k) {
                    ceilValue += this.elements[i][k] * that.elements[k][j];
                }
                newMatrix[i][j] = ceilValue;
            }
        }
        return new Matrix(newMatrix, that.columnCount);
    }

    /**
     * Бинарное возведение в степень за O(log(n))
     * @param n натуральная степень
     * @return новую матрицу B = this^n
     */
    Matrix pow(int n) {
        Matrix result = new Matrix(this.elements, this.columnCount);
        Matrix copy = new Matrix(this.elements, this.columnCount);
        while (n > 0) {
            if (n % 2 == 1) {
                result = result.multiply(this);
                --n;
            } else {
                copy = copy.multiply(copy);
                n >>= 1;
            }
        }
        return result;
    }
}
