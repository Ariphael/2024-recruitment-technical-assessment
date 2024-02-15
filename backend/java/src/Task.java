package src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

record File(
    int id, 
    String name, 
    List<String> categories, 
    int parent, 
    int size
) {}  

public class Task {
    /**
     * Task 1
     */
    public static List<String> leafFiles(List<File> files) {
        if (files.size() == 0)
            return new ArrayList<>();

        Map<Integer, List<Integer>> parentAndChildFilesMap = 
            createParentAndCorrespondingChildrenFilesListMap(files);

        return files.stream()
            .filter(file -> parentAndChildFilesMap.get(file.id()) == null)
            .map(leafFile -> leafFile.name())
            .collect(Collectors.toList());
    }

    /**
     * Task 2
     */
    public static List<String> kLargestCategories(List<File> files, int k) {
        if (files.size() == 0)
            return new ArrayList<>();

        Map<String, Integer> categoryFileCount = new HashMap<>();

        files.forEach(file -> {
            for (String category : file.categories()) {
                if (categoryFileCount.get(category) == null)
                    categoryFileCount.put(category, 1);
                else
                    categoryFileCount.put(category, categoryFileCount.get(category) + 1);
            }
        });

        Comparator<Map.Entry<String, Integer>> comparator = Comparator.comparing(
            (Map.Entry<String, Integer> entry) -> entry.getValue(),
            Comparator.reverseOrder()
        ).thenComparing(
            (Map.Entry<String, Integer> entry) -> entry.getKey(), 
            String.CASE_INSENSITIVE_ORDER
        );

        return categoryFileCount.entrySet()
            .stream()
            .sorted(comparator)
            .limit(k)
            .map(Map.Entry::getKey)
            .toList();
    }

    /**
     * Task 3
     */
    public static int largestFileSize(List<File> files) {
        if (files.size() == 0)
            return 0;

        Map<Integer, File> fileIdMap = new HashMap<>();
        files.forEach(file -> fileIdMap.put(file.id(), file));

        Map<Integer, Integer> fileIdToTotalSizeMap = createFileSizeMap(fileIdMap);
        return fileIdToTotalSizeMap.entrySet()
            .stream()
            .mapToInt(Map.Entry::getValue)
            .max()
            .getAsInt();
    }

    private static Map<Integer, List<Integer>> createParentAndCorrespondingChildrenFilesListMap(List<File> files) {
        Map<Integer, List<Integer>> parentAndChildFilesMap = new HashMap<>();

        files.forEach(file -> {
            if (file.parent() == -1)
                return;
            else if (parentAndChildFilesMap.get(file.parent()) == null)
                parentAndChildFilesMap.put(file.parent(), new ArrayList<>(Arrays.asList(file.id())));
            else
                parentAndChildFilesMap.get(file.parent()).add(file.id());
        });

        return parentAndChildFilesMap;
    }

    private static Map<Integer, Integer> createFileSizeMap(Map<Integer, File> fileIdMap) {
        Map<Integer, Integer> fileSizeMap = new HashMap<>();
        
        fileIdMap.values().stream().forEach(file -> {
            Integer fileSize = fileSizeMap.get(file.id());
            fileSizeMap.put(
                file.id(), 
                fileSize == null ? file.size() : fileSize + file.size()
            );
            updateParentFilesSizeMap(fileSizeMap, fileIdMap, file, file.size());
        });

        return fileSizeMap;
    }

    private static Map<Integer, Integer> updateParentFilesSizeMap(
        Map<Integer, Integer> fileSizeMap, 
        Map<Integer, File> fileIdMap, 
        File file,
        int fileSize
    ) {
        if (file.parent() == -1)
            return fileSizeMap;

        Integer parentFileSize = fileSizeMap.get(file.parent());

        fileSizeMap.put(
            file.parent(), 
            parentFileSize == null ? fileSize : parentFileSize + fileSize
        );
        return updateParentFilesSizeMap(fileSizeMap, fileIdMap, fileIdMap.get(file.parent()), file.size());

    }

    public static void main(String[] args) {
        // leaf files tests

        List<File> empty = new ArrayList<>();
        List<File> oneLayer = List.of(
            new File(1, "1", List.of("Documents"), -1, 1024),
            new File(2, "2", List.of("Documents"), 1, 1024),
            new File(3, "3", List.of("Documents"), 1, 1024),
            new File(4, "4", List.of("Documents"), 1, 1024),
            new File(5, "5", List.of("Documents"), 1, 1024)
        );
        List<File> multiLayer = List.of(
            new File(1, "1", List.of("Documents"), -1, 1024),
            new File(2, "2", List.of("Documents"), -1, 1024),
            new File(3, "3", List.of("Documents"), -1, 1024),
            new File(4, "4", List.of("Documents"), 1, 1024),
            new File(5, "5", List.of("Documents"), 1, 1024),
            new File(6, "6", List.of("Documents"), 2, 1024),
            new File(7, "7", List.of("Documents"), 6, 1024),
            new File(8, "8", List.of("Documents"), 2, 1024),
            new File(9, "9", List.of("Documents"), 7, 1024),
            new File(10, "10", List.of("Documents"), 4, 1024)
        );
        
        // empty
        List<String> leafFilesNoTestFiles = leafFiles(empty);
        assert leafFilesNoTestFiles != null && leafFilesNoTestFiles.size() == 0;
        // one layer
        List<String> leafFilesOneLayer = leafFiles(oneLayer);
        assert leafFilesOneLayer.containsAll(List.of("2", "3", "4", "5"));
        assert !leafFilesOneLayer.contains("1");
        // multi layer
        List<String> leafFilesComplex = leafFiles(multiLayer);
        assert leafFilesComplex.containsAll(List.of("10", "5", "9", "8", "3"));
        assert !leafFilesComplex.contains("1") && !leafFilesComplex.contains("4") 
            && !leafFilesComplex.contains("2") && !leafFilesComplex.contains("6")
            && !leafFilesComplex.contains("7");

        // k largest categories tests

        List<File> kLargestFiles = List.of(
            new File(1, "1", List.of("b", "a"), -1, 1024),
            new File(2, "2", List.of("b", "a"), -1, 1024),
            new File(3, "3", List.of("b", "a", "c"), -1, 1024),
            new File(4, "4", List.of("b", "a", "c", "d"), -1, 1024),
            new File(5, "5", List.of("b", "a", "c", "d", "f"), -1, 1024),
            new File(6, "6", List.of("b", "a", "c", "d", "e"), -1, 1024)
        );
        List<File> alphabeticalOrderFiles = List.of(
            new File(1, "1", List.of("A", "d", "e", "C", "b"), -1, 1024),
            new File(2, "2", List.of("A", "d", "e", "C", "b"), -1, 1024),
            new File(3, "3", List.of("A", "d", "e", "C", "b"), -1, 1024),
            new File(4, "4", List.of("A", "d", "e", "C", "b"), -1, 1024)
        );

        // k = number of categories
        List<String> result = kLargestCategories(kLargestFiles, 6);
        assert result.equals(List.of("a", "b", "c", "d", "e", "f"));
        // k > number of categories
        assert kLargestCategories(kLargestFiles, 10).equals(List.of("a", "b", "c", "d", "e", "f"));
        // k < number of categories
        assert kLargestCategories(kLargestFiles, 5).equals(List.of("a", "b", "c", "d", "e"));
        assert kLargestCategories(kLargestFiles, 4).equals(List.of("a", "b", "c", "d"));
        assert kLargestCategories(kLargestFiles, 3).equals(List.of("a", "b", "c"));
        assert kLargestCategories(kLargestFiles, 2).equals(List.of("a", "b"));
        assert kLargestCategories(kLargestFiles, 1).equals(List.of("a"));
        // case insensitive alphabetical order
        assert kLargestCategories(alphabeticalOrderFiles, 5).equals(List.of("A", "b", "C", "d", "e"));

        // largest file size
        List<File> A = List.of(            
            new File(1, "1", null, -1, 1024),
            new File(2, "2", null, 1, 2000),
            new File(3, "3", null, 1, 3000),
            new File(4, "4", null, 1, 2000),
            new File(5, "5", null, 4, 2000)
        );
        List<File> B = List.of(
            new File(6, "6", null, 9, 6044),
            new File(7, "7", null, 6, 10000),
            new File(8, "8", null, 7, 10000),
            new File(9, "9", null, -1, 10000)
        );
        List<File> manyLayers = List.of(
            new File(1, "1", null, -1, 1024),
            new File(2, "2", null, 1, 2000),
            new File(3, "3", null, 1, 3000),
            new File(4, "4", null, 1, 2000),
            new File(5, "5", null, 4, 2000),
            new File(6, "6", null, 9, 6044),
            new File(7, "7", null, 6, 10000),
            new File(8, "8", null, 7, 10000),
            new File(9, "9", null, -1, 10000)
        );
        List<File> sameSize = List.of(
            new File(1, "1", null, -1, 1024),
            new File(2, "2", null, 1, 2000),
            new File(3, "3", null, 1, 3000),
            new File(4, "4", null, 1, 2000),
            new File(5, "5", null, 4, 26020),
            new File(6, "6", null, 9, 6044),
            new File(7, "7", null, 6, 10000),
            new File(8, "8", null, 7, 10000),
            new File(9, "9", null, -1, 10000)
        );

        // no files, function should return 0
        assert largestFileSize(empty) == 0;

        // Total size tests
        assert largestFileSize(A) == 10024;
        assert largestFileSize(B) ==36044;
        assert largestFileSize(manyLayers) == 36044;
        assert largestFileSize(sameSize) == 36044;


        // Provided dry tests
        List<File> testFiles = List.of(
            new File(1, "Document.txt", List.of("Documents"), 3, 1024),
            new File(2, "Image.jpg", List.of("Media", "Photos"), 34, 2048),
            new File(3, "Folder", List.of("Folder"), -1, 0),
            new File(5, "Spreadsheet.xlsx", List.of("Documents", "Excel"), 3, 4096),
            new File(8, "Backup.zip", List.of("Backup"), 233, 8192),
            new File(13, "Presentation.pptx", List.of("Documents", "Presentation"), 3, 3072),
            new File(21, "Video.mp4", List.of("Media", "Videos"), 34, 6144),
            new File(34, "Folder2", List.of("Folder"), 3, 0),
            new File(55, "Code.py", List.of("Programming"), -1, 1536),
            new File(89, "Audio.mp3", List.of("Media", "Audio"), 34, 2560),
            new File(144, "Spreadsheet2.xlsx", List.of("Documents", "Excel"), 3, 2048),
            new File(233, "Folder3", List.of("Folder"), -1, 4096)
        );
        
        List<String> leafFiles = leafFiles(testFiles);
        leafFiles.sort(null);
        assert leafFiles.equals(List.of(
            "Audio.mp3",
            "Backup.zip",
            "Code.py",
            "Document.txt",
            "Image.jpg",
            "Presentation.pptx",
            "Spreadsheet.xlsx",
            "Spreadsheet2.xlsx",
            "Video.mp4"
        ));

        assert kLargestCategories(testFiles, 3).equals(List.of(
            "Documents", "Folder", "Media"
        ));

        assert largestFileSize(testFiles) == 20992;
    }
}