import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class MinElementFinder {
    private final String[] inputFileNames;
    private final String outputFileName;
    private volatile Double globalMin = null;
    // Чё такое volatile:
    // Ключевое слово volatile в Java гарантирует, что:
    // Чтение переменной всегда возвращает самое актуальное значение (из основной памяти, а не из кеша потока).
    // Запись в переменную сразу видна всем потокам (изменения не остаются в кеше процессора).

    public MinElementFinder(String[] inputFileNames, String outputFileName) {
        this.inputFileNames = inputFileNames;
        this.outputFileName = outputFileName;
    }

    public void findMin() {
        ExecutorService executor = Executors.newFixedThreadPool(inputFileNames.length);
        List<Future<Double>> futures = new ArrayList<>();
        // Future это интерфейс в Java, который представляет результат асинхронной (фоновой) операции,
        // выполняемой в отдельном потоке.
        // Он позволяет:
        // 1.Запускать задачу в фоне (например, через ExecutorService).
        // 2.Проверять, завершилась ли задача isDone().
        // 3.Плучать результат get(), ожидая при необходимости.
        for (String fileName : inputFileNames) {
            Callable<Double> task = () -> {
                double min = Double.POSITIVE_INFINITY;
                try (Scanner scanner = new Scanner(new File(fileName))) {
                    while (scanner.hasNext()) {
                        if (scanner.hasNextDouble()) {
                            double num = scanner.nextDouble();
                            if (num < min) min = num;
                        } else {
                            scanner.next(); // Пропускаем нечисловые данные
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Ошибка чтения файла " + fileName + ": " + e.getMessage());
                    return Double.NaN; // Возвращаем NaN при ошибке
                }
                return min;
            };
            futures.add(executor.submit(task));
        }

        // Собираем результаты
        for (Future<Double> future : futures) {
            try {
                Double fileMin = future.get();
                synchronized (this) {
                    if (globalMin == null || fileMin < globalMin) {
                        globalMin = fileMin;
                        writeResultToFile(globalMin);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Ошибка при обработке файла: " + e.getMessage());
            }
        }

        executor.shutdown();
    }

    private void writeResultToFile(double min) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFileName))) {
            writer.println(min);
        } catch (IOException e) {
            System.err.println("Ошибка записи в файл " + outputFileName + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Неправильное использование");
            return;
        }

        String outputFile = args[0];
        String[] inputFiles = Arrays.copyOfRange(args, 1, args.length);

        MinElementFinder finder = new MinElementFinder(inputFiles, outputFile);
        finder.findMin();

        System.out.println("Глобальный минимум записан в файл: " + outputFile);
    }
}