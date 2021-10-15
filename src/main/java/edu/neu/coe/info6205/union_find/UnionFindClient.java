package edu.neu.coe.info6205.union_find;

import edu.neu.coe.info6205.graphs.BFS_and_prims.StdRandom;

import java.util.Scanner;

public class UnionFindClient {
    public static int count(int n) {
        UF_HWQUPC uf_hwqupc = new UF_HWQUPC(n);
        int m = 0;
        while (uf_hwqupc.components() > 1) {
            int a = StdRandom.uniform(n);
            int b = StdRandom.uniform(n);
            if (!uf_hwqupc.isConnected(a,b)) {
                uf_hwqupc.union(a,b);
            }
            m++;
        }
        return m;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter number of Sites: ");
        int sites = scanner.nextInt();
        int t = 20;
        for (int i = sites; i < 100000000; i +=i) {
            double sum = 0;
            for (int j = 0; j < t; j++) {
                sum += count(i);
            }
            System.out.println("Number of Objects 'n': "+i+" and Number of pairs (m): "+ sum/t);
        }

    }
}
