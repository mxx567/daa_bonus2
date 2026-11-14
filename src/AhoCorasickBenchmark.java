import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AhoCorasickBenchmark {
    private static final int RUNS = 5;
    private static final int WARMUP_RUNS = 5;
    private static final int[] PATTERN_COUNTS = {10, 100, 1000};
    private static final int[] TEXT_LENGTHS = {1000, 10000, 100000};

    public static void run() {
        checkExamples();

        Random random = new Random(54);
        String[] allPatterns = generatePatterns(random, 1000);
        String fullText = generateText(random, allPatterns, 100000);

        Path output = Path.of("results.csv").toAbsolutePath();
        PrintStream console = System.out;

        try (PrintStream csv = new PrintStream(
                Files.newOutputStream(output), false, StandardCharsets.UTF_8)) {

            System.setOut(csv);
            System.out.println("patterns,text_length,run,matches,build_ms,search_ms,total_ms");

            for (int count : PATTERN_COUNTS) {
                String[] patterns = Arrays.copyOf(allPatterns, count);
                for (int length : TEXT_LENGTHS) {
                    benchmark(patterns, fullText.substring(0, length));
                }
            }

            if (csv.checkError()) {
                throw new IOException("Could not finish writing " + output);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save " + output, e);
        } finally {
            System.setOut(console);
        }

        System.out.println("CSV saved to " + output);
    }

    private static void benchmark(String[] patterns, String text) {
        List<String> expected = referenceMatches(patterns, text);
        long[] builds = new long[RUNS];
        long[] searches = new long[RUNS];
        PrintStream console = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try (PrintStream capture = new PrintStream(buffer, false, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            for (int run = 0; run < WARMUP_RUNS + RUNS; run++) {
                buffer.reset();
                long start = System.nanoTime();
                AhoCorasick matcher = new AhoCorasick(patterns);
                long built = System.nanoTime();
                matcher.search(text);
                long finished = System.nanoTime();

                verifyOutput(expected, buffer);
                if (run >= WARMUP_RUNS) {
                    int index = run - WARMUP_RUNS;
                    builds[index] = built - start;
                    searches[index] = finished - built;
                }
            }
        } finally {
            System.setOut(console);
        }

        long buildSum = 0;
        long searchSum = 0;
        for (int run = 0; run < RUNS; run++) {
            buildSum += builds[run];
            searchSum += searches[run];
            printRow(patterns.length, text.length(), Integer.toString(run + 1),
                    expected.size(), builds[run], searches[run]);
        }
        printRow(patterns.length, text.length(), "average", expected.size(),
                (double) buildSum / RUNS, (double) searchSum / RUNS);
    }

    private static void printRow(int count, int length, String run, long matches,
                                 double buildNs, double searchNs) {
        System.out.printf(Locale.ROOT, "%d,%d,%s,%d,%.6f,%.6f,%.6f%n",
                count, length, run, matches, buildNs / 1e6, searchNs / 1e6,
                (buildNs + searchNs) / 1e6);
    }

    private static String[] generatePatterns(Random random, int count) {
        Set<String> words = new LinkedHashSet<>();
        while (words.size() < count) {
            int length = 3 + random.nextInt(6);
            StringBuilder word = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                word.append((char) ('a' + random.nextInt(26)));
            }
            words.add(word.toString());
        }
        return words.toArray(new String[0]);
    }

    private static String generateText(Random random, String[] patterns, int length) {
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = (char) ('a' + random.nextInt(26));
        }
        for (int i = 0; i + 8 <= length; i += 100) {
            int index = (i == 0) ? 0 : random.nextInt(patterns.length);
            String word = patterns[index];
            word.getChars(0, word.length(), text, i);
        }
        return new String(text);
    }

    private static List<String> referenceMatches(String[] patterns, String text) {
        List<String> matches = new ArrayList<>();
        for (String pattern : patterns) {
            int start = text.indexOf(pattern);
            while (start >= 0) {
                int end = start + pattern.length() - 1;
                matches.add(pattern + " at [" + start + ", " + end + "]");
                start = text.indexOf(pattern, start + 1);
            }
        }
        Collections.sort(matches);
        return matches;
    }

    private static void verifyOutput(List<String> expected, ByteArrayOutputStream buffer) {
        List<String> actual = new ArrayList<>();
        buffer.toString(StandardCharsets.UTF_8).lines().forEach(actual::add);
        Collections.sort(actual);
        if (!actual.equals(expected)) {
            throw new AssertionError("Search output differs from reference. Expected "
                    + expected + ", got " + actual);
        }
    }

    private static void checkExamples() {
        checkExample("ushers", "he", "she", "his", "hers");
        checkExample("aaaa", "a", "aa", "aaa");
        checkExample("bird", "cat", "dog");
        checkExample("", "he");
        checkExample("h!e he", "he");
    }

    private static void checkExample(String text, String... patterns) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream console = System.out;
        try (PrintStream capture = new PrintStream(buffer, false, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            new AhoCorasick(patterns).search(text);
        } finally {
            System.setOut(console);
        }
        verifyOutput(referenceMatches(patterns, text), buffer);
    }
}
