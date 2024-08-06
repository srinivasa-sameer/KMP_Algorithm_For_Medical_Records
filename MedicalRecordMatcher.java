
import java.util.ArrayList;
import java.util.List;

public class MedicalRecordMatcher {

    // Function to compute the prefix function
    private static int[] computePrefixFunction(String pattern) {
        int m = pattern.length();
        int[] pi = new int[m];
        int k = 0;
        for (int q = 1; q < m; q++) {
            while (k > 0 && pattern.charAt(k) != pattern.charAt(q)) {
                k = pi[k - 1];
            }
            if (pattern.charAt(k) == pattern.charAt(q)) {
                k++;
            }
            pi[q] = k;
        }
        return pi;
    }

    // Find occurrences of the keyword in a single text
    private static int findOccurrences(String text, String pattern) {
        int n = text.length();
        int m = pattern.length();
        int[] pi = computePrefixFunction(pattern);
        int q = 0;
        int occurrences = 0;
        for (int i = 0; i < n; i++) {
            while (q > 0 && pattern.charAt(q) != text.charAt(i)) {
                q = pi[q - 1];
            }
            if (pattern.charAt(q) == text.charAt(i)) {
                q++;
            }
            if (q == m) {
                occurrences++;
                q = pi[q - 1];
            }
        }
        return occurrences;
    }

    // Main method to process records
    public static List<String> matchRecords(String[] records, String keyword) {
        List<String> matches = new ArrayList<>();
        int totalOccurrences = 0;
        String keywordLower = keyword.toLowerCase();

        for (String record : records) {
            String recordLower = record.toLowerCase();
            int occurrences = findOccurrences(recordLower, keywordLower);
            if (occurrences > 0) {
                matches.add(record + " (Occurrences: " + occurrences + ")");
                totalOccurrences += occurrences;
            }
        }

        double percentageMatch = records.length > 0 ? (totalOccurrences / (double) records.length) * 100 : 0.0;
        System.out.println(String.format("Percentage of records with the keyword: %.2f%%", percentageMatch));

        return matches;
    }

    public static void main(String[] args) {
        String[] medicalRecords = {
                "Patient A: Diagnosed with diabetes. Prescribed metformin.",
                "Patient B: History of hypertension and high cholesterol.",
                "Patient C: Prescribed metformin for diabetes management.",
                "Patient D: No significant medical history."
        };

        String keyword = "diabetes";
        List<String> matches = matchRecords(medicalRecords, keyword);

        System.out.println("Patients with the keyword and occurrences:");
        for (String match : matches) {
            System.out.println("Record: " + match);
        }
    }
}
