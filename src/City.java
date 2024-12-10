//ОТЧЁТ ПО РАБОТЕ ПРИКРЕПЛЕН В РЕПОЗИТОРИИ, ПОСМОТРИТЕ ЕГО ТОЖЕ, ПОЖАЛУЙСТА
import java.io.*;
import java.util.*;
import java.nio.file.*;
import javax.xml.stream.*;
import java.io.FileInputStream;

public class City {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите путь к файлу или exit для выхода:");
        while (true) {
            String input = scanner.nextLine().trim().toLowerCase().replace("\"", "");
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Завершение работы.");
                break;
            }
            Path filePath = Paths.get(input);
            if (!Files.exists(filePath)) {
                System.out.println("Файл не найден, попробуйте снова.");
                continue;
            }
            long startTime = System.currentTimeMillis();
            try {
                if (input.endsWith(".xml")) {
                    XmlFile processor = new XmlFile();
                    CityStatistics statistics = processor.processFile(filePath);
                    CityStatisticsPrinter.printStatistics(statistics);
                } else if (input.endsWith(".csv")) {
                    CsvFile processor = new CsvFile();
                    CityStatistics statistics = processor.processFile(filePath);
                    CityStatisticsPrinter.printStatistics(statistics);
                }
                else {
                    System.out.println("Неверный формат файла.");

                }
            } catch (Exception e) {
                System.out.println("Ошибка при обработке файла: " + e.getMessage());
            }
            long endTime = System.currentTimeMillis();
            System.out.println("Время обработки файла: " + (endTime - startTime)/1000. + " с");
            System.out.println("Введите путь к файлу или exit для выхода:");
        }
        scanner.close();
    }
}

class CsvFile  {
    public CityStatistics processFile(Path filePath) throws Exception {
        CityStatistics statistics = new CityStatistics();
        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line=br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                String city = values[0].replace("\"", "").trim();
                String street = values[1].replace("\"", "").trim();
                String house = values[2].replace("\"", "").trim();
                int floor = Integer.parseInt(values[3].replace("\"", "").trim());
                statistics.addBuildingData(city, street, house, floor);
            }
        }
        return statistics;
    }
}

class XmlFile {
    public CityStatistics processFile(Path filePath) throws Exception {
        CityStatistics statistics = new CityStatistics();
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
            XMLStreamReader reader = factory.createXMLStreamReader(fis);
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT && "item".equals(reader.getLocalName())) {
                    String city = reader.getAttributeValue(null, "city");
                    String street = reader.getAttributeValue(null, "street");
                    String house = reader.getAttributeValue(null, "house");
                    int floor = Integer.parseInt(reader.getAttributeValue(null, "floor"));
                    statistics.addBuildingData(city, street, house, floor);
                }
            }
            reader.close();
        }
        return statistics;
    }
}

class CityStatistics {
    private final Map<String, Integer> duplicates = new HashMap<>();
    private final Map<String, Map<Integer, Integer>> floorCountByCity = new HashMap<>();
    public void addBuildingData(String city, String street, String house, int floor) {
        String buildingKey = city + ", " + street + ", " + house;
        duplicates.put(buildingKey, duplicates.getOrDefault(buildingKey, 0) + 1);
        floorCountByCity.computeIfAbsent(city, k -> new HashMap<>())
                .merge(floor, 1, Integer::sum);
    }
    public Map<String, Integer> getDuplicates() {
        return duplicates;
    }
    public Map<String, Map<Integer, Integer>> getFloorCountByCity() {
        return floorCountByCity;
    }
}

class CityStatisticsPrinter {
    public static void printStatistics(CityStatistics statistics) {
        System.out.println("Дублирующиеся записи:");
        statistics.getDuplicates().entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue() + " раз(а)"));
        System.out.println("\nКоличество зданий по этажам в каждом городе:");
        statistics.getFloorCountByCity().forEach((city, floorCounts) -> {
            System.out.println("Город: " + city);
            for (int i = 1; i <= 5; i++) {
                int count = floorCounts.getOrDefault(i, 0);
                System.out.println("  " + i + "-этажных зданий: " + count);
            }
        });
    }
}
