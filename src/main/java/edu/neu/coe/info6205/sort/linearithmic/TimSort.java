/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.info6205.sort.linearithmic;

import edu.neu.coe.info6205.sort.BaseHelper;
import edu.neu.coe.info6205.sort.Helper;
import edu.neu.coe.info6205.sort.SortWithHelper;
import edu.neu.coe.info6205.sort.counting.MSDStringSort;
import edu.neu.coe.info6205.util.Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Sorter which delegates to Timsort via Arrays.sort.
 *
 * @param <X>
 */
public class TimSort<X extends Comparable<X>> extends SortWithHelper<X> {

    /**
     * Constructor for TimSort
     *
     * @param helper an explicit instance of Helper to be used.
     */
    public TimSort(Helper<X> helper) {
        super(helper);
    }

    /**
     * Constructor for TimSort
     *
     * @param N      the number elements we expect to sort.
     * @param config the configuration.
     */
    public TimSort(int N, Config config) {
        super(DESCRIPTION, N, config);
    }

    public TimSort() throws IOException {
        this(new BaseHelper<>(DESCRIPTION, Config.load(TimSort.class)));
    }

    public void sort(X[] xs, int from, int to) {
        Arrays.sort(xs, from, to);
    }

    public static final String DESCRIPTION = "Timsort";

    public String[] getInputArray(int l) {
        File file = new File("C:\\Users\\User\\Desktop\\n.txt");
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
        TimSort<String> timSort = new TimSort<String>();
        String[] ucs = timSort.getInputArray(100);


        ucs = timSort.sort(ucs);
        //Arrays.sort(ucs);

        for (int i = 0; i < ucs.length; i++){
            System.out.println(ucs[i]);
            count ++;
        }
        System.out.println(count);
    }
}

