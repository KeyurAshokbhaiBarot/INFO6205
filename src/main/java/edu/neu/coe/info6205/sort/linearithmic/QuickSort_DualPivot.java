package edu.neu.coe.info6205.sort.linearithmic;

import edu.neu.coe.info6205.sort.BaseHelper;
import edu.neu.coe.info6205.sort.Helper;
import edu.neu.coe.info6205.util.Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class QuickSort_DualPivot<X extends Comparable<X>> extends QuickSort<X> {

    public static final String DESCRIPTION = "QuickSort dual pivot";

    public QuickSort_DualPivot(String description, int N, Config config) {
        super(description, N, config);
        setPartitioner(createPartitioner());
    }

    /**
     * Constructor for QuickSort_3way
     *
     * @param helper an explicit instance of Helper to be used.
     */

    public QuickSort_DualPivot(Helper<X> helper) {
        super(helper);
        setPartitioner(createPartitioner());
    }

    /**
     * Constructor for QuickSort_3way
     *
     * @param N      the number elements we expect to sort.
     * @param config the configuration.
     */

    public QuickSort_DualPivot(int N, Config config) {
        this(DESCRIPTION, N, config);
    }

    @Override
    public Partitioner<X> createPartitioner() {
        return new Partitioner_DualPivot(getHelper());
    }

    public class Partitioner_DualPivot implements Partitioner<X> {

        public Partitioner_DualPivot(Helper<X> helper) {
            this.helper = helper;
        }

        /**
         * Method to partition the given partition into smaller partitions.
         *
         * @param partition the partition to divide up.
         * @return an array of partitions, whose length depends on the sorting method being used.
         */
        public List<Partition<X>> partition(Partition<X> partition) {
            final X[] xs = partition.xs;
            final int lo = partition.from;
            final int hi = partition.to - 1;
            helper.swapConditional(xs, lo, hi);
            int lt = lo + 1;
            int gt = hi - 1;
            int i = lt;
            // NOTE: we are trying to avoid checking on instrumented for every time in the inner loop for performance reasons (probably a silly idea).
            // NOTE: if we were using Scala, it would be easy to set up a comparer function and a swapper function. With java, it's possible but much messier.
            if (helper.instrumented()) {
                while (i <= gt) {
                    if (helper.compare(xs, i, lo) < 0) helper.swap(xs, lt++, i++);
                    else if (helper.compare(xs, i, hi) > 0) helper.swap(xs, i, gt--);
                    else i++;
                }
                helper.swap(xs, lo, --lt);
                helper.swap(xs, hi, ++gt);
            } else {
                while (i <= gt) {
                    X x = xs[i];
                    if (x.compareTo(xs[lo]) < 0) swap(xs, lt++, i++);
                    else if (x.compareTo(xs[hi]) > 0) swap(xs, i, gt--);
                    else i++;
                }
                swap(xs, lo, --lt);
                swap(xs, hi, ++gt);
            }

            List<Partition<X>> partitions = new ArrayList<>();
            partitions.add(new Partition<>(xs, lo, lt));
            partitions.add(new Partition<>(xs, lt + 1, gt));
            partitions.add(new Partition<>(xs, gt + 1, hi + 1));
            return partitions;
        }

        // CONSIDER invoke swap in BaseHelper.
        private void swap(X[] ys, int i, int j) {
            X temp = ys[i];
            ys[i] = ys[j];
            ys[j] = temp;
        }

        private final Helper<X> helper;
    }

    public String[] getInputArray(int l) {
        //File file = new File("C:\\Users\\User\\Desktop\\n.txt");
        File file = new File("C:\\Users\\User\\Downloads\\shuffledChinese4M.txt");
        Scanner sc = null;
        try {
            sc = new Scanner(file);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        String[] a = new String[l];
        int count = 0;
        while(count < l) {
            a[count] = sc.nextLine();
            count ++;
        }
        sc.close();
        return a;
    }

    public static void main(String[] args) throws IOException {
        int count = 0;
        /*MSDStringSort msdStringSort = new MSDStringSort();
        String[] ucs = msdStringSort.getInputArray(250000);*/
        //QuickSort_DualPivot quickSort_dualPivot = new QuickSort_DualPivot()
        QuickSort_DualPivot<String> quickSort_dualPivot = new QuickSort_DualPivot<String>(BaseHelper.getHelper(QuickSort_DualPivot.class));
        String[] ucs = quickSort_dualPivot.getInputArray(10000);


        ucs = quickSort_dualPivot.sort(ucs);
        //Arrays.sort(ucs);

        for (int i = 0; i < ucs.length; i++){
            System.out.println(ucs[i]);
            count ++;
        }
        System.out.println(count);
    }
}



