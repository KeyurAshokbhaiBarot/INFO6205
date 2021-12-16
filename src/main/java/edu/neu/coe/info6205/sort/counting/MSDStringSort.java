package edu.neu.coe.info6205.sort.counting;

import edu.neu.coe.info6205.sort.elementary.InsertionSortMSD;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Class to implement Most significant digit string sort (a radix sort).
 */
public class MSDStringSort {

    /**
     * Sort an array of Strings using MSDStringSort.
     *
     * @param a the array to be sorted.
     */
    public void sort(String[] a) {
        int n = a.length;
        aux = new String[n];
        sort(a, 0, n, 0);
    }

    /**
     * Sort from a[lo] to a[hi] (exclusive), ignoring the first d characters of each String.
     * This method is recursive.
     *
     * @param a the array to be sorted.
     * @param lo the low index.
     * @param hi the high index (one above the highest actually processed).
     * @param d the number of characters in each String to be skipped.
     */
    private static void sort(String[] a, int lo, int hi, int d) {
        if (hi < lo + cutoff) InsertionSortMSD.sort(a, lo, hi, d);
        else {
            int[] count = new int[radix + 2];        // Compute frequency counts.
            for (int i = lo; i < hi; i++)
                count[charAt(a[i], d) + 2]++;
            for (int r = 0; r < radix + 1; r++)      // Transform counts to indices.
                count[r + 1] += count[r];
            for (int i = lo; i < hi; i++)     // Distribute.
                aux[count[charAt(a[i], d) + 1]++] = a[i];
            // Copy back.
            if (hi - lo >= 0) System.arraycopy(aux, 0, a, lo, hi - lo);
            // Recursively sort for each character value.
            // TO BE IMPLEMENTED
            for (int r = 0; r < radix; r++) {
                sort(a, lo + count[r], lo + count[r+1] - 1, d+1);
            }
        }
    }

    private static int charAt(String s, int d) {
        if (d < s.length()) return s.charAt(d);
        else return -1;
    }

    private static final int radix = 65536;
    private static final int cutoff = 15;
    private static String[] aux;       // auxiliary array for distribution

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

    public static void main(String[] args) {
        int count = 0;
        MSDStringSort msdStringSort = new MSDStringSort();
        String[] ucs = msdStringSort.getInputArray(4000000);

        msdStringSort.sort(ucs);
        for (int i = 0; i < ucs.length; i++){
            System.out.println(ucs[i]);
            count ++;
        }
        System.out.println(count);
    }
}