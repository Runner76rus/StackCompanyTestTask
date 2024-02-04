import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestStack {


    /**
     * Выполняет полный цикл чтения списка имен файлов,сортировки и записи полученных данных,
     * а также поиск и запись неоплаченных счетов.
     *
     * @param source - путь к текстовому файлу с именами файлов
     * @param target - путь к папке в которой будет создан файл чеки_по_папкам.txt
     * @throws IOException - if an I/O error occurs opening the file. if an I/O error occurs writing to
     *                     or creating the file, or the text cannot be
     *                     encoded using the specified charset
     */
    public static void run(Path source, Path target) throws IOException {
        List<String> fileNames = getFileNames(source);
        Map<String, List<String>> sortedData = getSortedData(fileNames);

        Path targetFile = Paths.get(target.toString(), "/", "чеки_по_папкам.txt");
        Files.deleteIfExists(targetFile);
        Files.createFile(targetFile);

        putIntoFolders(targetFile, sortedData);
        writeUnpaidBills(targetFile, sortedData);
    }

    /**
     * Метод читает все строки из файла и возвращает их в ввиде списка.
     *
     * @param source - путь к текстовому файлу
     * @return - список прочитанных строк
     * @throws IOException - if an I/O error occurs opening the file
     */
    public static List<String> getFileNames(Path source) throws IOException {
        // нужна чтобы убрать метасимвол ZWNBSP из начала файла *.txt
        String UTF8_BOM = "\uFEFF";
        try (Stream<String> stream = Files.lines(source)) {
            ArrayList<String> result = stream.collect(Collectors.toCollection(ArrayList::new));
            if (result.get(0).startsWith(UTF8_BOM)) {
                result.set(0, result.get(0).replace(UTF8_BOM, ""));
            }
            return result;
        }
    }

    /**
     * Записывает в файл в порядке месяц/файл отсортированные данные.
     *
     * @param targetFile - путь к файлу в который будет записана последовательность строк
     * @param files      - отсортированный список, где key = месяц, value = список имен файлов
     * @throws IOException -  if an I/O error occurs writing to or creating the file, or the text cannot be
     *                     encoded using the specified charset
     */
    public static void putIntoFolders(Path targetFile, Map<String, List<String>> files) throws IOException {
        for (Map.Entry<String, List<String>> entry : files.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                for (String file : entry.getValue()) {
                    Files.writeString(
                            targetFile,
                            String.format("/%s/%s\n", entry.getKey(), file),
                            StandardCharsets.UTF_8,
                            StandardOpenOption.APPEND);
                }
            }
        }
    }

    /**
     * Сортирует список имён файлов .
     *
     * @param files - список имён файлов
     * @return - список пар key, value,где key = месяц, value = список имен файлов
     */
    private static Map<String, List<String>> getSortedData(List<String> files) {
        Map<String, List<String>> data = new HashMap<>();
        for (Month value : Month.values()) {
            data.put(value.getName(), new ArrayList<>());
        }

        for (String file : files) {
            Set<String> months = Arrays.stream(Month.values())
                    .map(Month::getName)
                    .collect(Collectors.toSet());
            Set<String> services = Arrays.stream(UtilityService.values())
                    .map(UtilityService::getName)
                    .collect(Collectors.toSet());

            String[] nameParts = file.split("[_.]");

            if (nameParts.length == 3) {
                String month = nameParts[1].toLowerCase();
                String service = nameParts[0].toLowerCase();
                String suffix = nameParts[2].toLowerCase();
                if (months.contains(month)
                        && services.contains(service)
                        && suffix.equals("pdf")) {
                    data.get(month).add(file);
                }
            }
        }

        return data;
    }

    /**
     * Записывает в файл все неоплаченные счета.
     *
     * @param targetFile - путь к файлу в который будет записаны счета
     * @param data       - отсортированный список, где key = месяц, value = список имен файлов
     * @throws IOException - if an I/O error occurs writing to or creating the file, or the text cannot be
     *                     encoded using the specified charset
     */
    public static void writeUnpaidBills(Path targetFile, Map<String, List<String>> data) throws IOException {
        for (Map.Entry<String, List<String>> entry : data.entrySet()) {
            List<String> services = new ArrayList<>(Arrays.stream(UtilityService.values())
                    .map(UtilityService::getName)
                    .toList());
            for (String file : entry.getValue()) {
                String service = file.split("_")[0];
                services.remove(service);
            }
            data.put(entry.getKey(), services);
        }

        Files.writeString(
                targetFile,
                "Не оплачены:\n",
                StandardCharsets.UTF_8,
                StandardOpenOption.APPEND);

        for (Map.Entry<String, List<String>> entry : data.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                Files.writeString(
                        targetFile,
                        String.format("%s:\n", entry.getKey()),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.APPEND);
                for (String service : entry.getValue()) {
                    Files.writeString(
                            targetFile,
                            String.format("%s\n", service),
                            StandardCharsets.UTF_8,
                            StandardOpenOption.APPEND);
                }
            }
        }
    }

}
