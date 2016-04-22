package deca;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by zx on 16-4-22.
 */
public abstract class LR {

    Random random = new Random(42);

    static class DataPoint implements Serializable {
        DataPoint(double[] x, double y) {
            this.x = x;
            this.y = y;
        }

        double[] x;
        double y;

        public String toString(){
            return "DataPoint:(" + y + Arrays.toString(x) + ")";
        }
    }

    public abstract void textFile(int dimension, int nums);

    public abstract void compute(int iterations);

    public void shutdown(){}

}
