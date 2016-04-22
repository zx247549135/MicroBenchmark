package deca;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by luluorta on 16-4-11.
 */
public abstract class MultiThreadPR extends PR {
    protected int cores = Runtime.getRuntime().availableProcessors();
    protected ExecutorService executor = Executors.newFixedThreadPool(cores);

    protected int[] initKeyCounts = new int[cores];
    protected int[] reduceInKeyCounts = new int[cores];
    protected int[][] mapOutKeyCounts = new int[cores][cores];

    @Override
    protected void cache(Map<Integer, ArrayList<Integer>> links) {
        BitSet ids = new BitSet();
        BitSet[] mapIdSets = new BitSet[cores];
        for (int i = 0; i < cores; i++) {
            mapIdSets[i] = new BitSet();
        }
        for (Map.Entry<Integer, ArrayList<Integer>> entry : links.entrySet()) {
            int key = entry.getKey();
            ArrayList<Integer> value = entry.getValue();
            initKeyCounts[key % cores]++;
            ids.set(key);
            BitSet mapIdSet = mapIdSets[key % cores];
            for (int dst : value) {
                ids.set(dst);
                mapIdSet.set(dst);
            }
        }
        for (int i = ids.nextSetBit(0); i >= 0; i = ids.nextSetBit(i + 1)) {
            reduceInKeyCounts[i % cores]++;
        }
        for (int i = 0; i < cores; i++) {
            BitSet mapIdSet = mapIdSets[i];
            for (int j = mapIdSet.nextSetBit(0); j >= 0; j = mapIdSet.nextSetBit(j + 1)) {
                mapOutKeyCounts[i][j % cores]++;
            }
        }
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
