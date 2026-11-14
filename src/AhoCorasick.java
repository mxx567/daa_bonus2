import java.util.ArrayDeque;
import java.util.Queue;

public class AhoCorasick {
    private static class Node {
        Node[] next = new Node[26];
        Node fail;
        Node output;
        String word;
    }

    private final Node root = new Node();

    public AhoCorasick(String... patterns) {
        for (String pattern : patterns) {
            insert(pattern);
        }
        buildLinks();
    }

    private void insert(String pattern) {
        if (pattern.isEmpty()) {
            throw new IllegalArgumentException("Patterns must be nonempty");
        }

        Node node = root;
        for (char ch : pattern.toCharArray()) {
            if (ch < 'a' || ch > 'z') {
                throw new IllegalArgumentException("Patterns must use a-z");
            }

            int letter = ch - 'a';
            if (node.next[letter] == null) {
                node.next[letter] = new Node();
            }
            node = node.next[letter];
        }
        node.word = pattern;
    }

    private void buildLinks() {
        Queue<Node> queue = new ArrayDeque<>();
        root.fail = root;

        for (int letter = 0; letter < 26; letter++) {
            Node child = root.next[letter];
            if (child == null) {
                root.next[letter] = root;
            } else {
                child.fail = root;
                queue.add(child);
            }
        }

        while (!queue.isEmpty()) {
            Node parent = queue.remove();

            for (int letter = 0; letter < 26; letter++) {
                Node child = parent.next[letter];
                Node fallback = parent.fail.next[letter];

                if (child == null) {
                    parent.next[letter] = fallback;
                } else {
                    child.fail = fallback;
                    child.output = (fallback.word != null) ? fallback : fallback.output;
                    queue.add(child);
                }
            }
        }
    }

    public void search(String text) {
        Node state = root;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch < 'a' || ch > 'z') {
                state = root;
                continue;
            }

            state = state.next[ch - 'a'];

            // Report the current word, then any shorter words ending here.
            for (Node match = state; match != null; match = match.output) {
                if (match.word != null) {
                    int start = i - match.word.length() + 1;
                    System.out.printf("%s at [%d, %d]%n", match.word, start, i);
                }
            }
        }
    }
}