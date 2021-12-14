package edu.neu.coe.info6205.sort;

import edu.neu.coe.info6205.graphs.BFS_and_prims.StdRandom;
import edu.neu.coe.info6205.sort.counting.MSDStringSort;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

//from:https://algs4.cs.princeton.edu/23quicksort/QuickDualPivot.java.html
public class DualPivotQuickSort {

    // quicksort the array a[] using dual-pivot quicksort
    public static void sort(Comparable[] a) {
        StdRandom.shuffle(a);
        sort(a, 0, a.length - 1);
        assert isSorted(a);
    }

    // quicksort the subarray a[lo .. hi] using dual-pivot quicksort
    private static void sort(Comparable[] a, int lo, int hi) {
        if (hi <= lo) return;

        // make sure a[lo] <= a[hi]
        if (less(a[hi], a[lo])) exch(a, lo, hi);

        int lt = lo + 1, gt = hi - 1;
        int i = lo + 1;
        while (i <= gt) {
            if       (less(a[i], a[lo])) exch(a, lt++, i++);
            else if  (less(a[hi], a[i])) exch(a, i, gt--);
            else                         i++;
        }
        exch(a, lo, --lt);
        exch(a, hi, ++gt);

        // recursively sort three subarrays
        sort(a, lo, lt-1);
        if (less(a[lt], a[gt])) sort(a, lt+1, gt-1);
        sort(a, gt+1, hi);

        assert isSorted(a, lo, hi);
    }



    /***************************************************************************
     *  Helper sorting functions.
     ***************************************************************************/

    // is v < w ?
    private static boolean less(Comparable v, Comparable w) {
        return v.compareTo(w) < 0;
    }

    // exchange a[i] and a[j]
    private static void exch(Object[] a, int i, int j) {
        Object swap = a[i];
        a[i] = a[j];
        a[j] = swap;
    }

    /***************************************************************************
     *  Check if array is sorted - useful for debugging.
     ***************************************************************************/
    private static boolean isSorted(Comparable[] a) {
        return isSorted(a, 0, a.length - 1);
    }

    private static boolean isSorted(Comparable[] a, int lo, int hi) {
        for (int i = lo + 1; i <= hi; i++)
            if (less(a[i], a[i-1])) return false;
        return true;
    }

    // print array to standard output
    private static void show(Comparable[] a) {
        for (int i = 0; i < a.length; i++) {
            System.out.println(a[i]);
        }
    }

    public String[] getInputArray(int l) {
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

    public static void main(String[] args) {
        int count = 0;
        //MSDStringSort msdStringSort = new MSDStringSort();
        DualPivotQuickSort dualPivotQuickSort = new DualPivotQuickSort();
        String[] ucs = dualPivotQuickSort.getInputArray(250000);

        dualPivotQuickSort.sort(ucs);
        for (int i = 0; i < ucs.length; i++){
            System.out.println(ucs[i]);
            count ++;
        }
        System.out.println(count);
    }

}