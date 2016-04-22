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
    protected int numCores = Runtime.getRuntime().availableProcessors();
    protected int numPartitions = numCores * 4;
    protected ExecutorService executor = Executors.newFixedThreadPool(numCores);

    protected int[] initKeyCounts = new int[numPartitions];
    protected int[] reduceInKeyCounts = new int[numPartitions];
    protected int[][] mapOutKeyCounts = new int[numPartitions][numPartitions];

    @Override
    protected void cache(Map<Integer, ArrayList<Integer>> links) {
        BitSet ids = new BitSet();
        BitSet[] mapIdSets = new BitSet[numPartitions];
        for (int i = 0; i < numPartitions; i++) {
            mapIdSets[i] = new BitSet();
        }
        for (Map.Entry<Integer, ArrayList<Integer>> entry : links.entrySet()) {
            int key = entry.getKey();
            ArrayList<Integer> value = entry.getValue();
            initKeyCounts[key % numPartitions]++;
            ids.set(key);
            BitSet mapIdSet = mapIdSets[key % numPartitions];
            for (int dst : value) {
                ids.set(dst);
                mapIdSet.set(dst);
            }
        }
        for (int i = ids.nextSetBit(0); i >= 0; i = ids.nextSetBit(i + 1)) {
            reduceInKeyCounts[i % numPartitions]++;
        }
        for (int i = 0; i < numPartitions; i++) {
            BitSet mapIdSet = mapIdSets[i];
            for (int j = mapIdSet.nextSetBit(0); j >= 0; j = mapIdSet.nextSetBit(j + 1)) {
                mapOutKeyCounts[i][j % numPartitions]++;
            }
        }
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
