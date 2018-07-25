package com.zbj.alg.industry.attention_model; /**
 * Created by octacon on 2017/10/9.
 */

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;

import com.zbj.alg.seg.domain.Result;
import com.zbj.alg.seg.domain.Term;
import com.zbj.alg.seg.splitWord.ToAnalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class CategoryModel implements IndLabel{

    private SavedModelBundle modelBundle;
    private Vocabulary vocab;
    private Map<Long, String> idxLableMap;

    public CategoryModel(String modelPath) {
        this.modelBundle = SavedModelBundle.load(modelPath, "serve");
        this.vocab = new Vocabulary(modelPath + "word_counts.txt");
        this.idxLableMap = buildLableMap(modelPath + "catthree_lable_idx.txt");
    }

    public SavedModelBundle getModelBundle() {
        return this.modelBundle;
    }

    public Tensor buildInput(String data) {
        Set<String> unExpectedNature = new HashSet<String>() {{
            add("w");
            add("x");
            add("m");
            add("u");
            add("r");
            add("y");
            add("null");
        }};
        Result parse = ToAnalysis.parse(data);
        List<Term> terms = parse.getTerms();
        List<String> cleanterms = new ArrayList<String>();
        List<Integer> token_index = new ArrayList<Integer>();

        for (Term w : terms) {
            String word = w.getName();
            String natureStr = w.getNatureStr();
            if (!unExpectedNature.contains(natureStr)) {
                cleanterms.add(word);
            }
        }
        cleanterms.add(0, "<S>");

        for (String token : cleanterms) {
            token_index.add(this.vocab.word_to_id(token));
        }

        int[][] matrix = new int[1][100];

        for (int i = 0; i < token_index.size(); i++) {
            if (i >= 100) break;
            matrix[0][i] = token_index.get(i);
        }

        Tensor x = Tensor.create(matrix);
        return x;
    }

    private Map<Long, String> buildLableMap(String path) {
        Map<Long, String> idx_to_lable = new HashMap<Long, String>();
        try {
            File file = new File(path);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String read;
            while ((read = reader.readLine()) != null) {
                String[] tokens = read.split(" ");
                String lable = tokens[0];
                long idx = Long.parseLong(tokens[1]);
                idx_to_lable.put(idx, lable);
            }
            return idx_to_lable;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getLabel(String text) {
        Tensor x = buildInput(text);
        Tensor result = this.modelBundle.session().runner().feed("input_x", x)
                .fetch("accuracy/predict").run().get(0);
        long[] dst = new long[1];
        result.copyTo(dst);
        result.close();
        x.close();
        return this.idxLableMap.get(dst[0]);
    }
}
