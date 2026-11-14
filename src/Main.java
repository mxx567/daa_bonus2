public class Main {
    public static void main(String[] args) {
        AhoCorasick matcher = new AhoCorasick("he", "she", "his", "hers");
        matcher.search("ushers");
        AhoCorasickBenchmark.run();
    }
}