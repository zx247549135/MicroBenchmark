package deca;

import java.util.concurrent.TimeUnit;

/**
 * Created by zx on 16-4-7.
 */
public class LRHelper {

    public static void main(String[] args) {

        int dimensions = Integer.parseInt(args[1]);
        int nums = Integer.parseInt(args[2]);
        int iterations = Integer.parseInt(args[3]);

        int partitions = Integer.parseInt(args[4]);
        int cores = Integer.parseInt(args[5]);

        int type = Integer.parseInt(args[0]);
        switch (type) {
            case 1: {
                JavaLR javaLR = new JavaLR();
                long startTime = System.currentTimeMillis();
                javaLR.textFile(dimensions, nums);
                long endTime = System.currentTimeMillis();
                System.out.println("JavaLR textFile time: " + (endTime - startTime) + "ms");

                startTime = System.currentTimeMillis();
                javaLR.compute(5);
                endTime = System.currentTimeMillis();
                System.out.println("JavaLR compute time: " + (endTime - startTime) + "ms");

                startTime = System.currentTimeMillis();
                javaLR.compute(iterations);
                endTime = System.currentTimeMillis();
                System.out.println("JavaLR compute time: " + (endTime - startTime) + "ms");
                break;
            }
            case 2: {
                DecaJavaLR decaJavaLR = new DecaJavaLR();
                long startTime = System.currentTimeMillis();
                decaJavaLR.textFile(dimensions, nums);
                long endTime = System.currentTimeMillis();
                System.out.println("DecaJavaLR textFile time: " + (endTime - startTime) + "ms");

                startTime = System.currentTimeMillis();
                decaJavaLR.compute(iterations);
                endTime = System.currentTimeMillis();
                System.out.println("DecaJavaLR compute time: " + (endTime - startTime) + "ms");

                startTime = System.currentTimeMillis();
                decaJavaLR.compute(iterations);
                endTime = System.currentTimeMillis();
                System.out.println("DecaJavaLR compute time: " + (endTime - startTime) + "ms");
                break;
            }
            case 3: {
                SerializeJavaLR serializeJavaLR = new SerializeJavaLR();
                long startTime = System.currentTimeMillis();
                serializeJavaLR.textFile(dimensions, nums);
                long endTime = System.currentTimeMillis();
                System.out.println("SerializeJavaLR textFile time: " + (endTime - startTime) + "ms");

                startTime = System.currentTimeMillis();
                serializeJavaLR.compute(iterations);
                endTime = System.currentTimeMillis();
                System.out.println("SerialzieJavaLR compute time: " + (endTime - startTime) + "ms");

                startTime = System.currentTimeMillis();
                serializeJavaLR.compute(iterations);
                endTime = System.currentTimeMillis();
                System.out.println("SerialzieJavaLR compute time: " + (endTime - startTime) + "ms");
                break;
            }
            case 4: {
                MultiThreadJavaLR multiThreadJavaLR = new MultiThreadJavaLR(partitions, cores);
                long startTime = System.currentTimeMillis();
                multiThreadJavaLR.textFile(dimensions, nums);
                long endTime = System.currentTimeMillis();
                System.out.println("MultiThreadJavaLR textFile time: " + (endTime - startTime) + "ms");

                startTime = System.currentTimeMillis();
                multiThreadJavaLR.compute(5);
                endTime = System.currentTimeMillis();
                System.out.println("MultiThreadJavaLR compute time: " + (endTime - startTime) + "ms");
                multiThreadJavaLR.shutdown();
                triggerGC();

                startTime = System.currentTimeMillis();
                multiThreadJavaLR.compute(iterations);
                endTime = System.currentTimeMillis();
                System.out.println("MultiThreadJavaLR compute time: " + (endTime - startTime) + "ms");
                multiThreadJavaLR.shutdown();
                break;
            }
            case 5: {
                MultiThreadDecaJavaLR multiThreadDecaJavaLR = new MultiThreadDecaJavaLR();
                long startTime = System.currentTimeMillis();
                multiThreadDecaJavaLR.textFile(dimensions, nums, cores);
                long endTime = System.currentTimeMillis();
                System.out.println("MultiThreadDecaJavaLR textFile time: " + (endTime - startTime) + "ms");

                startTime = System.currentTimeMillis();
                multiThreadDecaJavaLR.compute(iterations);
                endTime = System.currentTimeMillis();
                System.out.println("MultiThreadDecaJavaLR compute time: " + (endTime - startTime) + "ms");

                startTime = System.currentTimeMillis();
                multiThreadDecaJavaLR.compute(iterations);
                endTime = System.currentTimeMillis();
                System.out.println("MultiThreadDecaJavaLR compute time: " + (endTime - startTime) + "ms");
                multiThreadDecaJavaLR.shutdown();
                break;
            }
            case 6: {
                MultiThreadSerializeJavaLR multiThreadSerializeJavaLR = new MultiThreadSerializeJavaLR();
                long startTime = System.currentTimeMillis();
                multiThreadSerializeJavaLR.textFile(dimensions, nums, cores);
                long endTime = System.currentTimeMillis();
                System.out.println("MultiThreadSerializeJavaLR textFile time: " + (endTime - startTime) + "ms");

                startTime = System.currentTimeMillis();
                multiThreadSerializeJavaLR.compute(iterations);
                endTime = System.currentTimeMillis();
                System.out.println("MultiThreadSerializeJavaLR compute time: " + (endTime - startTime) + "ms");

                startTime = System.currentTimeMillis();
                multiThreadSerializeJavaLR.compute(iterations);
                endTime = System.currentTimeMillis();
                System.out.println("MultiThreadSerializeJavaLR compute time: " + (endTime - startTime) + "ms");
                break;
            }
        }
    }

    private static void triggerGC() {
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
