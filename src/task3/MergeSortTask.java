package task3;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

class MergeSortTask extends RecursiveTask<int[]> {
    private final int[] array;
    private static final int THRESHOLD = 10_000;

    public MergeSortTask(int[] array) {
        this.array = array;
    }

    @Override
    protected int[] compute() {
        if (array.length <= THRESHOLD) {
            // использовать стандартную сортировку для малых массивов
            int[] sorted = array.clone();
            Arrays.sort(sorted);
            return sorted;
        } else {
            // разделить массив
            int mid = array.length / 2;
            int[] left = Arrays.copyOfRange(array, 0, mid);
            int[] right = Arrays.copyOfRange(array, mid, array.length);

            // создать подзадачи
            MergeSortTask leftTask = new MergeSortTask(left);
            MergeSortTask rightTask = new MergeSortTask(right);

            leftTask.fork();
            int[] rightResult = rightTask.compute();
            int[] leftResult = leftTask.join();

            // слить результаты
            return merge(leftResult, rightResult);
        }
    }

    private int[] merge(int[] left, int[] right) {
        int[] result = new int[left.length + right.length];
        int i = 0, j = 0, k = 0;

        while (i < left.length && j < right.length) {
            if (left[i] <= right[j]) {
                result[k++] = left[i++];
            } else {
                result[k++] = right[j++];
            }
        }

        while (i < left.length) {
            result[k++] = left[i++];
        }

        while (j < right.length) {
            result[k++] = right[j++];
        }

        return result;
    }

    // Проверка корректности сортировки
    private static boolean isSorted(int[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println("=== Параллельная сортировка слиянием ===");

        int size = 1_000_000;
        System.out.println("Размер массива: " + size + " элементов");
        System.out.println("Порог разделения: " + THRESHOLD + " элементов");
        System.out.println();

        // Генерация случайного массива
        Random random = new Random(42); // фиксированный seed для воспроизводимости
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(100000);
        }

        System.out.println("Генерация случайного массива...");
        System.out.print("Первые 10 элементов до сортировки: [");
        for (int i = 0; i < 10; i++) {
            System.out.print(array[i]);
            if (i < 9) System.out.print(", ");
        }
        System.out.println("]");
        System.out.println();

        // Стандартная сортировка Arrays.sort()
        int[] arrayCopy1 = array.clone();
        long startTime = System.nanoTime();
        Arrays.sort(arrayCopy1);
        long standardTime = System.nanoTime() - startTime;

        System.out.println("Стандартная сортировка (Arrays.sort):");
        System.out.println("Время выполнения: " + (standardTime / 1_000_000) + " мс");
        System.out.print("Первые 10 элементов: [");
        for (int i = 0; i < 10; i++) {
            System.out.print(arrayCopy1[i]);
            if (i < 9) System.out.print(", ");
        }
        System.out.println("]");
        System.out.println();

        // Параллельная сортировка Fork/Join
        ForkJoinPool pool = ForkJoinPool.commonPool();
        MergeSortTask task = new MergeSortTask(array);

        startTime = System.nanoTime();
        int[] parallelResult = pool.invoke(task);
        long parallelTime = System.nanoTime() - startTime;

        System.out.println("Параллельная сортировка (Fork/Join):");
        System.out.println("Время выполнения: " + (parallelTime / 1_000_000) + " мс");
        System.out.print("Первые 10 элементов: [");
        for (int i = 0; i < 10; i++) {
            System.out.print(parallelResult[i]);
            if (i < 9) System.out.print(", ");
        }
        System.out.println("]");
        System.out.println();

        // Проверка корректности
        boolean isCorrect = Arrays.equals(arrayCopy1, parallelResult) && isSorted(parallelResult);
        System.out.println("Проверка корректности: " + (isCorrect ? "✓ Массив отсортирован правильно" : "✗ Ошибка сортировки"));

        double speedup = (double) standardTime / parallelTime;
        System.out.println("Ускорение: " + String.format("%.2f", speedup) + "x");
    }
}