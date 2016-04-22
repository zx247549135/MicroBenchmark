package deca;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.*;

/**
 * Created by zx on 16-4-11.
 */
public class MultiThreadDecaJavaLR {

    private int D = 10;   // Number of dimensions
    private int N = 1000;  // Number of data points
    private final double R = 0.00007;  // Scaling factor

    private int cores = 8;

    private double[] w;

    class Chunk{
        int dimensions;
        int size;
        Unsafe unsafe;
        long address;
        long count = 0;

        Chunk(int dimensions, int size){
            this.size = size;
            this.dimensions = dimensions;
            try {
                Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);
                unsafe = (Unsafe) unsafeField.get(null);
                address = unsafe.allocateMemory(size);
            }catch(Exception e){
                System.out.println("init error: " + e);
            }
        }

        public void putValue(double value, long offset){
            unsafe.putDouble(offset + address, value);
            count += 8;
        }

        public double[] getValue(){
            double[] gradient = new double[dimensions];
            long offset = address;
            double y;
            while(offset < count + address){
                y = unsafe.getDouble(offset);
                offset += 8;
                long current = offset;
                double dot = 0.0;
                for(int j = 0; j < dimensions; j ++){
                    dot += w[j] * unsafe.getDouble(current);
                    current += 8;
                }
                double tmp = (1 / (1 + Math.exp(-y * dot)) - 1) * y;
                for(int j = 0; j < dimensions; j ++){
                    gradient[j] += tmp * unsafe.getDouble(offset);
                    offset += 8;
                }
            }
            return gradient;
        }
    }

    class RunThread implements Callable {
        int partitionId;

        RunThread(int partitionId){
            this.partitionId = partitionId;
        }

        @Override
        public Object call() {
            return cacheBytes[partitionId].getValue();
        }

    }

    private Chunk[] cacheBytes;
    ExecutorService executor = Executors.newFixedThreadPool(cores);

    public void textFile(int dimensions, int nums, int cores) {
        this.D = dimensions;
        this.N = nums;
        this.cores = cores;

        w = new double[D];
        for (int i = 0; i < D; i++) {
            w[i] = 2 * i * 0.037 - 1;
        }

        try {
            int offset = 0;
            cacheBytes = new Chunk[cores];
            for(int i = 0; i < cores; i ++){
                cacheBytes[i] = new Chunk(D, N * (D+1) * 8 / cores);
            }

            int partitionId = 0;
            for (int i = 0; i < N; i++) {
                int y;
                if (i % 2 == 0)
                    y = 0;
                else
                    y = 1;

                if(partitionId >= cores) {
                    partitionId -= cores;
                    offset += (D + 1) * 8;
                }
                cacheBytes[partitionId].putValue(y, offset);
                offset += 8;
                for (int j = 0; j < D; j++) {
                    double tmp = i * 0.000013 + j * 0.00079 + y * R;
                    cacheBytes[partitionId].putValue(tmp, offset);
                    offset += 8;
                }
                partitionId += 1;
                offset -= (D + 1) * 8;
            }
        }catch(Exception e){
            System.out.println("textFile error: " + e);
        }
    }

    public void compute(int iterations){
        for(int iter = 0; iter < iterations; iter ++) {
            int usedCores = 0;
            Future[] futures = new Future[cores];
            try {
                while (usedCores < cores) {
                    Callable callable = new RunThread(usedCores);
                    futures[usedCores] = executor.submit(callable);
                    usedCores += 1;
                }
                for (int i = 0; i < cores; i++) {
                    double[] gradient = (double[]) futures[i].get(10, TimeUnit.MINUTES);
                    for (int j = 0; j < D; j++) {
                        w[j] -= gradient[j];
                    }
                }
            }catch (Exception e){
                System.out.println("compute error: " + e);
            }
        }
//        System.out.print("Final w: ");
//        System.out.println(Arrays.toString(w));
    }

    public void shutdown(){
        executor.shutdown();
    }
}
