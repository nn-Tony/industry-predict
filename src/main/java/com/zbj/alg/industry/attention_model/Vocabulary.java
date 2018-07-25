package com.zbj.alg.industry.attention_model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by octacon on 2017/10/9.
 */
public class Vocabulary {
    private Map<String, Integer> word2count = new HashMap<String, Integer>();
    private List<Map.Entry<String, Integer>> wordcounts;
    private Map<String, Integer> wordidx = new HashMap<String, Integer>();
    private final String unknow = "<UNK>";
    private final String start = "<S>";
    private int unknowid;
    private int startid;

    public Vocabulary(String path) {
        try {
            File file = new File(path);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String read;
            while ((read = reader.readLine()) != null) {
                String[] tokens = read.split(" ");
                String word = tokens[0];
                int freq = Integer.parseInt(tokens[1]);
                this.word2count.put(word, freq);
            }

            this.wordcounts = new ArrayList<Map.Entry<String, Integer>>(this.word2count.entrySet());
            Collections.sort(this.wordcounts, new Comparator<Map.Entry<String, Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                    return (o2.getValue() - o1.getValue());
                }
            });

            for (int i = 0; i < this.wordcounts.size(); i++) {
                String token = this.wordcounts.get(i).getKey();
                this.wordidx.put(token, i);
            }
            if (!this.wordidx.containsKey(this.unknow)) {
                this.wordidx.put(this.unknow, this.wordidx.size() - 1);
            }

            this.startid = this.wordidx.get(this.start);
            this.unknowid = this.wordidx.get(this.unknow);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int word_to_id(String token) {
        if (this.wordidx.containsKey(token))
            return this.wordidx.get(token);
        else
            return this.unknowid;
    }
}
