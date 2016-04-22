package deca;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.*;

/**
 * Created by zx on 16-4-11.
 */
public class MultiThreadSerializeJavaLR {
    private int D = 10;   // Number of dimensions
    private int N = 1000;  // Number of data points
    private final double R = 0.00007;  // Scaling factor

    private int cores = 8;

    private double[] w;

    class Chunk extends ByteArrayOutputStream {
        int size;
        int count;

        Chunk(int size){
            super(size);
            this.size = size;
        }

        public void increase(int num){
            count += num;
        }

        public ByteArrayInputStream toInputStream(){
            return new ByteArrayInputStream(buf);
        }
    }

    static class DataPoint implements Serializable {

        DataPoint(){}

        DataPoint(double[] x, double y) {
            this.x = x;
            this.y = y;
        }

        double[] x;
        double y;
    }

    class RunThread implements Callable {
        int partitionId;

        RunThread(int partitionId){
            this.partitionId = partitionId;
        }

        @Override
        public Object call() {
            double[] gradient = new double[D];
            Chunk block = cacheSerializeBytes[partitionId];
            try {
                ObjectInputStream obj = new ObjectInputStream(block.toInputStream());
                Input input = new Input(obj);
                for (int i = 0; i < block.count; i++) {
                    DataPoint p = (DataPoint) kryo.readObject(input, registration.getType());
                    double dot = dot(w, p.x);
                    double tmp = (1 / (1 + Math.exp(-p.y * dot)) - 1) * p.y;
                    for (int j = 0; j < D; j++) {
                        gradient[j] += tmp * p.x[j];
                    }
                }
            }catch (Exception e){
                System.out.println("Thread error: " + e);
            }
            return gradient;
        }

        public double dot(double[] a, double[] b) {
            double x = 0;
            for (int i = 0; i < D; i++) {
                x += a[i] * b[i];
            }
            return x;
        }
    }

    private Chunk[] cacheSerializeBytes;
    Kryo kryo = new Kryo();
    Registration registration;
    ExecutorService executor = Executors.newFixedThreadPool(cores);

    public void textFile(int dimension, int nums, int cores){
        this.D = dimension;
        this.N = nums;
        this.cores = cores;

        w = new double[D];
        for (int i = 0; i < D; i++) {
            w[i] = 2 * i * 0.037 - 1;
        }
//        System.out.print("Initial w: ");
//        System.out.println(Arrays.toString(w));

        try {
            cacheSerializeBytes = new Chunk[cores];
            for(int i = 0; i < cores; i ++){
                cacheSerializeBytes[i] = new Chunk((D + 1) * 8 / cores * N + 2);
            }
            ObjectOutputStream[] objs = new ObjectOutputStream[cores];
            Output[] outputs = new Output[cores];
            for(int i = 0; i < cores; i ++) {
                objs[i] = new ObjectOutputStream(cacheSerializeBytes[i]);
                outputs[i] = new Output(objs[i]);
            }
            registration = kryo.register(DataPoint.class);
            int partitionId = 0;
            for (int i = 0; i < N; i++) {
                int y;
                if (i % 2 == 0)
                    y = 0;
                else
                    y = 1;
                double[] x = new double[D];
                for (int j = 0; j < D; j++) {
                    x[j] = i * 0.000013 + j * 0.00079 + y * R;
                }

                if(partitionId >= cores) {
                    partitionId -= cores;
                }
                DataPoint tmp = new DataPoint(x, y);
                kryo.writeObject(outputs[partitionId], tmp);
                cacheSerializeBytes[partitionId].increase(1);
                partitionId += 1;
            }
            for(int i = 0; i < cores; i ++) {
                outputs[i].flush();
                outputs[i].close();
            }
        }catch(IOException e){
            System.out.println("textFile error: " + e);
        }
    }

    public void compute(int iterations) {
        for (int iter = 0; iter < iterations; iter++) {
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
            } catch (Exception e) {
                System.out.println("compute error: " + e);
            }
        }
        System.out.print("Final w: ");
        System.out.println(Arrays.toString(w));
    }

}
