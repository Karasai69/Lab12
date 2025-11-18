package task2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

class FileSearchTask extends RecursiveAction {
    private final File directory;
    private final String extension;
    private final ConcurrentLinkedQueue<String> results;

    public FileSearchTask(File directory, String extension, ConcurrentLinkedQueue<String> results) {
        this.directory = directory;
        this.extension = extension;
        this.results = results;
    }

    @Override
    protected void compute() {
        File[] files = directory.listFiles();
        if (files == null) return;

        List<FileSearchTask> subTasks = new ArrayList<>();

        for (File file : files) {
            if (file.isDirectory()) {
                // создать подзадачу для директории
                FileSearchTask task = new FileSearchTask(file, extension, results);
                subTasks.add(task);
                task.fork();
            } else if (file.getName().toLowerCase().endsWith(extension.toLowerCase())) {
                // добавить файл в результаты
                results.add(file.getAbsolutePath());
            }
        }

        // дождаться завершения всех подзадач
        for (FileSearchTask task : subTasks) {
            task.join();
        }
    }

    // Метод для создания тестовой структуры директорий
    private static void createTestDirectory(String rootPath) throws IOException {
        File root = new File(rootPath);
        if (!root.exists()) {
            root.mkdirs();
        }

        // Создать файлы в корневой директории
        new File(root, "file1.txt").createNewFile();
        new File(root, "file2.java").createNewFile();
        new File(root, "document.txt").createNewFile();
        new File(root, "image.png").createNewFile();

        // Создать поддиректории с файлами
        File subdir1 = new File(root, "subdir1");
        subdir1.mkdir();
        new File(subdir1, "file3.txt").createNewFile();
        new File(subdir1, "data.txt").createNewFile();
        new File(subdir1, "program.java").createNewFile();

        File subdir2 = new File(subdir1, "subdir2");
        subdir2.mkdir();
        new File(subdir2, "file4.txt").createNewFile();
        new File(subdir2, "config.txt").createNewFile();

        File subdir3 = new File(root, "subdir3");
        subdir3.mkdir();
        new File(subdir3, "file5.txt").createNewFile();
        new File(subdir3, "log.txt").createNewFile();
        new File(subdir3, "script.py").createNewFile();
    }

    public static void main(String[] args) {
        System.out.println("=== Параллельный поиск файлов ===");

        String rootPath = "./test_directory";
        String extension = ".txt";

        try {
            // Создание тестовой структуры
            createTestDirectory(rootPath);
            System.out.println("Корневая директория: " + new File(rootPath).getAbsolutePath());
            System.out.println("Искомое расширение: " + extension);
            System.out.println();

            System.out.println("Поиск файлов...");

            ConcurrentLinkedQueue<String> results = new ConcurrentLinkedQueue<>();
            FileSearchTask task = new FileSearchTask(new File(rootPath), extension, results);
            ForkJoinPool pool = ForkJoinPool.commonPool();

            long startTime = System.nanoTime();
            pool.invoke(task);
            long searchTime = System.nanoTime() - startTime;

            // Вывод результатов
            System.out.println("Найденные файлы:");
            int count = 1;
            for (String filePath : results) {
                System.out.println(count + ". " + filePath);
                count++;
            }

            System.out.println();
            System.out.println("Всего найдено: " + results.size() + " файлов");
            System.out.println("Время выполнения: " + (searchTime / 1_000_000) + " мс");

        } catch (IOException e) {
            System.err.println("Ошибка при создании тестовой структуры: " + e.getMessage());
        }
    }
}