# Aho–Corasick in Java

`AhoCorasick.java` finds all occurrences of multiple lowercase English patterns in one pass through a text. A trie stores shared prefixes, failure links let the search continue from the longest usable suffix, and output links report overlapping matches. For example, reading `she` finds both `she` and `he`.

## Tests

Call `AhoCorasickBenchmark.run()` from `Main`. It checks overlapping matches, shared suffixes, empty text, punctuation, and cases with no matches. For each input, it verifies every match position against Java’s `String.indexOf()`. The benchmark writes its timings to `results.csv`.

## Complexity

Let `M` be the total pattern length, `N` the text length, `K` the number of patterns, and `Z` the number of matches.

- **Aho–Corasick:** With a fixed 26-letter alphabet, building the trie and its links takes **O(M) time and space**. Searching takes **O(N + Z) time** and **O(1) extra space** when matches are streamed.
- **Repeated KMP:** Building a prefix table and scanning the text separately for each pattern takes **O(M + K·N + Z) total time**. Only one pattern’s prefix table is needed at a time.

## Experimental results

Both algorithms used identical inputs generated with seed `54`. Each value is the average of five `System.nanoTime()` measurements after five warm-up runs. Times include preprocessing, searching, and formatting matches in memory. KMP called `searchAll(text, pattern)` once per pattern.

| Patterns | Text length | Aho–Corasick (ms) | Repeated KMP (ms) | Speedup |
|---:|---:|---:|---:|---:|
| 10 | 1,000 | 0.280 | 0.519 | 1.9× |
| 10 | 10,000 | 0.447 | 0.525 | 1.2× |
| 10 | 100,000 | 0.688 | 2.743 | 4.0× |
| 100 | 1,000 | 0.157 | 0.250 | 1.6× |
| 100 | 10,000 | 0.324 | 2.684 | 8.3× |
| 100 | 100,000 | 0.862 | 24.692 | 28.6× |
| 1,000 | 1,000 | 0.853 | 2.412 | 2.8× |
| 1,000 | 10,000 | 0.786 | 24.639 | 31.3× |
| 1,000 | 100,000 | 12.038 | 254.792 | 21.2× |

Both algorithms found the same matches in all nine cases. In the largest case, they found **1,931 matches**, and Aho–Corasick was **21.2× faster**. Its trie uses more memory, but one text scan serves the entire dictionary, and the trie can be reused for additional texts. KMP is simpler for a single pattern, but each additional pattern requires another scan.
