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

### Comparison with KMP

KMP uses a prefix table to remember how much of a pattern still matches after a mismatch. Calling `KMP.searchAll()` separately for 1,000 patterns scans the same text 1,000 times. Aho–Corasick stores all patterns in one trie and uses failure links to find them in a single scan.

With 1,000 patterns, Aho–Corasick was **2.8× faster** on 1,000 characters and **21.2× faster** on 100,000 characters. In the larger test, both algorithms found the same 1,931 matches. Aho–Corasick took 12.038 ms and repeated KMP took 254.792 ms, including preprocessing.

KMP is simpler for a single pattern and needs only one prefix table at a time. Aho–Corasick uses more memory because each trie node stores 26 transition slots. Its advantage is that one scan covers the whole dictionary, and the same trie can be reused to search additional texts.