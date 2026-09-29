package com.yuganji.generator.finnegan;

import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Pre-built pool of random words, sentences, and URLs.
 * Generated once at startup using Finnegan, then served at O(1) cost with zero allocation.
 * Thread-safe: uses only immutable arrays and ThreadLocalRandom.
 */
@Log4j2
public class TextPool {

    private static final int WORD_POOL_SIZE = 10_000;
    private static final int SENTENCE_POOL_SIZE = 2_000;
    private static final int URL_POOL_SIZE = 2_000;
    private static final String[] EXTENSIONS = {".jsp", ".xml", ".js"};

    // INSTANCE must be declared AFTER all constants it depends on
    private static final TextPool INSTANCE = new TextPool();

    private final String[] words;
    private final String[] sentences;
    private final String[] urls;

    private TextPool() {
        log.info("Initializing TextPool: generating {} words, {} sentences, {} URLs...",
                WORD_POOL_SIZE, SENTENCE_POOL_SIZE, URL_POOL_SIZE);
        long start = System.currentTimeMillis();

        // Use a separate Finnegan copy to avoid shared state
        Finnegan fin = Finnegan.ENGLISH.copy();

        this.words = generateWords(fin, WORD_POOL_SIZE);
        this.sentences = generateSentences(fin, SENTENCE_POOL_SIZE);
        this.urls = generateUrls(fin, URL_POOL_SIZE);

        log.info("TextPool initialized in {}ms: {} words, {} sentences, {} URLs",
                System.currentTimeMillis() - start, words.length, sentences.length, urls.length);
    }

    public static TextPool getInstance() {
        return INSTANCE;
    }

    public String randomWord() {
        return words[ThreadLocalRandom.current().nextInt(words.length)];
    }

    public String randomSentence() {
        return sentences[ThreadLocalRandom.current().nextInt(sentences.length)];
    }

    public String randomUrl() {
        return urls[ThreadLocalRandom.current().nextInt(urls.length)];
    }

    private static String[] generateWords(Finnegan fin, int count) {
        String[] pool = new String[count];
        long seed = 12345L;
        for (int i = 0; i < count; i++) {
            try {
                String word = fin.word(seed + i, i % 2 == 0, 1);
                pool[i] = word != null ? word : "word" + i;
            } catch (Exception e) {
                pool[i] = "word" + i;
            }
        }
        return pool;
    }

    private static String[] generateSentences(Finnegan fin, int count) {
        String[] pool = new String[count];
        long seed = 67890L;
        String[] midPunctuation = {",", ",", ",", ";"};
        String[] endPunctuation = {".", ".", ".", "!", "?", "..."};
        for (int i = 0; i < count; i++) {
            try {
                int minWords = 1 + (i % 5);
                int maxWords = minWords + 3;
                String sentence = fin.sentence(seed + i, minWords, maxWords,
                        midPunctuation, endPunctuation, 0.10);
                pool[i] = sentence != null ? sentence : "Generated log sentence number " + i + ".";
            } catch (Exception e) {
                pool[i] = "Generated log sentence number " + i + ".";
            }
        }
        return pool;
    }

    private static String[] generateUrls(Finnegan fin, int count) {
        String[] pool = new String[count];
        long seed = 11111L;
        for (int i = 0; i < count; i++) {
            try {
                // Generate URL path
                int pathDepth = 1 + (i % 4);
                String sentence = fin.sentence(seed + i, pathDepth, pathDepth + 1,
                        new String[]{"/"}, EXTENSIONS, 1);
                if (sentence == null) {
                    pool[i] = "/page" + i + EXTENSIONS[i % EXTENSIONS.length];
                    continue;
                }
                String path = "/" + sentence.replace(" ", "");

                // Generate 0-4 query params
                int paramCount = i % 5;
                if (paramCount > 0) {
                    StringBuilder params = new StringBuilder();
                    params.append('?');
                    for (int j = 0; j < paramCount; j++) {
                        if (j > 0) params.append('&');
                        String key = fin.word(seed + i * 100 + j, false, 1);
                        String val = fin.word(seed + i * 100 + j + 50, false, 1);
                        params.append(key != null ? key : "k" + j);
                        params.append('=');
                        params.append(val != null ? val : "v" + j);
                    }
                    path += params.toString();
                }
                pool[i] = path;
            } catch (Exception e) {
                pool[i] = "/page" + i + EXTENSIONS[i % EXTENSIONS.length];
            }
        }
        return pool;
    }
}
