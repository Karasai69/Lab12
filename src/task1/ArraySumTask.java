package task1;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

class ArraySumTask extends RecursiveTask<Long> {
    private final int[] array;
    private final int start;
    private final int end;
    private static final int THRESHOLD = 100_000;

    public ArraySumTask(int[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }

    public ArraySumTask(int[] array) {
        this(array, 0, array.length);
    }

    @Override
    protected Long compute() {
        int length = end - start;

        if (length <= THRESHOLD) {
            // прямое вычисление
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += array[i];
            }
            return sum;
        } else {
            // разделение на подзадачи
            int mid = start + length / 2;
            ArraySumTask leftTask = new ArraySumTask(array, start, mid);
            ArraySumTask rightTask = new ArraySumTask(array, mid, end);

            leftTask.fork();
            long rightResult = rightTask.compute();
            long leftResult = leftTask.join();

            return leftResult + rightResult;
        }
    }

    // Последовательное суммирование для сравнения
    private static long sequentialSum(int[] array) {
        long sum = 0;
        for (int value : array) {
            sum += value;
        }
        return sum;
    }

    public static void main(String[] args) {
        System.out.println("=== Параллельное суммирование массива ===");

        // Создание массива из 10,000,000 элементов
        int size = 10_000_000;
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = i + 1;
        }

        System.out.println("Размер массива: " + size + " элементов");
        System.out.println("Порог разделения: " + THRESHOLD + " элементов");
        System.out.println();

        // Последовательное суммирование
        long startTime = System.nanoTime();
        long sequentialResult = sequentialSum(array);
        long sequentialTime = System.nanoTime() - startTime;

        System.out.println("Последовательное суммирование:");
        System.out.println("Результат: " + sequentialResult);
        System.out.println("Время выполнения: " + (sequentialTime / 1_000_000) + " мс");
        System.out.println();

        // Параллельное суммирование
        ForkJoinPool pool = ForkJoinPool.commonPool();
        ArraySumTask task = new ArraySumTask(array);

        startTime = System.nanoTime();
        long parallelResult = pool.invoke(task);
        long parallelTime = System.nanoTime() - startTime;

        System.out.println("Параллельное суммирование (ForkJoin):");
        System.out.println("Результат: " + parallelResult);
        System.out.println("Время выполнения: " + (parallelTime / 1_000_000) + " мс");
        System.out.println();

        // Сравнение производительности
        double speedup = (double) sequentialTime / parallelTime;
        System.out.println("Ускорение: " + String.format("%.2f", speedup) + "x");
        System.out.println("Результаты совпадают: " + (sequentialResult == parallelResult));
    }
}