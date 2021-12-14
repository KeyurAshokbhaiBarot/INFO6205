package edu.neu.coe.info6205.sort.simple;/*
 * Copyright (c) 2009, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/**
 * Copied directly from java.util.DualPivotQuicksort (which is invisible to regular Java programmers).
 * This class implements an object-based version of DualPivotQuicksort.
 * <p>
 * This class implements the Dual-Pivot Quicksort algorithm by
 * Vladimir Yaroslavskiy, Jon Bentley, and Josh Bloch. The algorithm
 * offers O(n log(n)) performance on many data sets that cause other
 * quicksorts to degrade to quadratic performance, and is typically
 * faster than traditional (one-pivot) Quicksort implementations.
 * <p>
 * All exposed methods are package-private, designed to be invoked
 * from public methods (in class Arrays) after performing any
 * necessary array bounds checks and expanding parameters into the
 * required forms.
 *
 * @author Vladimir Yaroslavskiy
 * @author Jon Bentley
 * @author Josh Bloch
 * @version 2011.02.11 m765.827.12i:5\7pm
 * @since 1.7
 */
public final class PureDualPivotQuicksort {

    /**
     * This method was copied and adapted from Arrays.
     *
     * @param a the array to be sorted.
     */
    public static <X extends Comparable<X>> void sort(final X[] a) {
        PureDualPivotQuicksort.sort(a, 0, a.length - 1, null, 0, 0);
    }

    /**
     * Prevents instantiation.
     */
    private PureDualPivotQuicksort() {
    }

    /*
     * Tuning parameters.
     */

    /**
     * The maximum number of runs in merge sort.
     */
    private static final int MAX_RUN_COUNT = 67;

    /**
     * The maximum length of run in merge sort.
     */
    private static final int MAX_RUN_LENGTH = 33;

    /**
     * If the length of an array to be sorted is less than this
     * constant, Quicksort is used in preference to merge sort.
     */
    private static final int QUICKSORT_THRESHOLD = 286;

    /**
     * If the length of an array to be sorted is less than this
     * constant, insertion sort is used in preference to Quicksort.
     */
    private static final int INSERTION_SORT_THRESHOLD = 47;

    /**
     * If the length of a byte array to be sorted is greater than this
     * constant, counting sort is used in preference to insertion sort.
     */
    private static final int COUNTING_SORT_THRESHOLD_FOR_BYTE = 29;

    /**
     * If the length of a short or char array to be sorted is greater
     * than this constant, counting sort is used in preference to Quicksort.
     */
    private static final int COUNTING_SORT_THRESHOLD_FOR_SHORT_OR_CHAR = 3200;

    /*
     * Sorting methods for seven primitive types.
     */

    /**
     * Sorts the specified range of the array using the given
     * workspace array slice if possible for merging
     *
     * @param a        the array to be sorted
     * @param left     the index of the first element, inclusive, to be sorted
     * @param right    the index of the last element, inclusive, to be sorted
     * @param work     a workspace array (slice)
     * @param workBase origin of usable space in work array
     * @param workLen  usable size of work array
     */
    static <X extends Comparable<X>> void sort(X[] a, final int left, int right,
                                               X[] work, int workBase, final int workLen) {
        // Use Quicksort on small arrays
        if (right - left < QUICKSORT_THRESHOLD) {
            sort(a, left, right, true);
            return;
        }

        /*
         * Index run[i] is the start of i-th run
         * (ascending or descending sequence).
         */
        final int[] run = new int[MAX_RUN_COUNT + 1];
        int count = 0;
        run[0] = left;

        // Check if the array is nearly sorted
        for (int k = left; k < right; run[count] = k) {
            if (a[k].compareTo(a[k + 1]) < 0) { // ascending
                while (++k <= right && a[k - 1].compareTo(a[k]) <= 0) ;
            } else if (a[k].compareTo(a[k + 1]) > 0) { // descending
                while (++k <= right && a[k - 1].compareTo(a[k]) >= 0) ;
                for (int lo = run[count] - 1, hi = k; ++lo < --hi; ) {
                    final X t = a[lo];
                    a[lo] = a[hi];
                    a[hi] = t;
                }
            } else { // equal
                for (int m = MAX_RUN_LENGTH; ++k <= right && a[k - 1] == a[k]; ) {
                    if (--m == 0) {
                        sort(a, left, right, true);
                        return;
                    }
                }
            }

            /*
             * The array is not highly structured,
             * use Quicksort instead of merge sort.
             */
            if (++count == MAX_RUN_COUNT) {
                sort(a, left, right, true);
                return;
            }
        }

        // Check special cases
        // Implementation note: variable "right" is increased by 1.
        if (run[count] == right++) { // The last run contains one element
            run[++count] = right;
        } else if (count == 1) { // The array is already sorted
            return;
        }

        // Determine alternation base for merge
        byte odd = 0;
        for (int n = 1; (n <<= 1) < count; odd ^= 1) ;

        // Use or create temporary array b for merging
        X[] b;                 // temp array; alternates with a
        int ao, bo;              // array offsets from 'left'
        final int blen = right - left; // space needed for b
        if (work == null || workLen < blen || workBase + blen > work.length) {
            //noinspection unchecked,ConstantConditions
            work = (X[]) new Object[blen];
            workBase = 0;
        }
        if (odd == 0) {
            System.arraycopy(a, left, work, workBase, blen);
            b = a;
            bo = 0;
            a = work;
            ao = workBase - left;
        } else {
            b = work;
            ao = 0;
            bo = workBase - left;
        }

        // Merging
        for (int last; count > 1; count = last) {
            for (int k = (last = 0) + 2; k <= count; k += 2) {
                final int hi = run[k];
                final int mi = run[k - 1];
                for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
                    if (q >= hi || p < mi && a[p + ao].compareTo(a[q + ao]) <= 0) {
                        b[i + bo] = a[p++ + ao];
                    } else {
                        b[i + bo] = a[q++ + ao];
                    }
                }
                run[++last] = hi;
            }
            if ((count & 1) != 0) {
                for (int i = right, lo = run[count - 1]; --i >= lo;
                     b[i + bo] = a[i + ao]
                )
                    ;
                run[++last] = right;
            }
            final X[] t = a;
            a = b;
            b = t;
            final int o = ao;
            ao = bo;
            bo = o;
        }
    }

    /**
     * Sorts the specified range of the array by Dual-Pivot Quicksort.
     *
     * @param a        the array to be sorted
     * @param left     the index of the first element, inclusive, to be sorted
     * @param right    the index of the last element, inclusive, to be sorted
     * @param leftmost indicates if this part is the leftmost in the range
     */
    private static <X extends Comparable<X>> void sort(final X[] a, int left, int right, final boolean leftmost) {
        final int length = right - left + 1;

        // Use insertion sort on tiny arrays
        if (length < INSERTION_SORT_THRESHOLD) {
            if (leftmost) {
                /*
                 * Traditional (without sentinel) insertion sort,
                 * optimized for server VM, is used in case of
                 * the leftmost part.
                 */
                for (int i = left, j = i; i < right; j = ++i) {
                    final X ai = a[i + 1];
                    while (ai.compareTo(a[j]) < 0) {
                        a[j + 1] = a[j];
                        if (j-- == left) {
                            break;
                        }
                    }
                    a[j + 1] = ai;
                }
            } else {
                /*
                 * Skip the longest ascending sequence.
                 */
                do {
                    if (left >= right) {
                        return;
                    }
                } while (a[++left].compareTo(a[left - 1]) >= 0);

                /*
                 * Every element from adjoining part plays the role
                 * of sentinel, therefore this allows us to avoid the
                 * left range check on each iteration. Moreover, we use
                 * the more optimized algorithm, so called pair insertion
                 * sort, which is faster (in the context of Quicksort)
                 * than traditional implementation of insertion sort.
                 */
                for (int k = left; ++left <= right; k = ++left) {
                    X a1 = a[k], a2 = a[left];

                    if (a1.compareTo(a2) < 0) {
                        a2 = a1;
                        a1 = a[left];
                    }
                    while (a1.compareTo(a[--k]) < 0) {
                        a[k + 2] = a[k];
                    }
                    a[++k + 1] = a1;

                    while (a2.compareTo(a[--k]) < 0) {
                        a[k + 1] = a[k];
                    }
                    a[k + 1] = a2;
                }
                final X last = a[right];

                while (last.compareTo(a[--right]) < 0) {
                    a[right + 1] = a[right];
                }
                a[right + 1] = last;
            }
            return;
        }

        // Inexpensive approximation of length / 7
        final int seventh = (length >> 3) + (length >> 6) + 1;

        /*
         * Sort five evenly spaced elements around (and including) the
         * center element in the range. These elements will be used for
         * pivot selection as described below. The choice for spacing
         * these elements was empirically determined to work well on
         * a wide variety of inputs.
         */
        final int e3 = (left + right) >>> 1; // The midpoint
        final int e2 = e3 - seventh;
        final int e1 = e2 - seventh;
        final int e4 = e3 + seventh;
        final int e5 = e4 + seventh;

        // Sort these elements using insertion sort
        if (a[e2].compareTo(a[e1]) < 0) {
            final X t = a[e2];
            a[e2] = a[e1];
            a[e1] = t;
        }

        if (a[e3].compareTo(a[e2]) < 0) {
            final X t = a[e3];
            a[e3] = a[e2];
            a[e2] = t;
            if (t.compareTo(a[e1]) < 0) {
                a[e2] = a[e1];
                a[e1] = t;
            }
        }
        if (a[e4].compareTo(a[e3]) < 0) {
            final X t = a[e4];
            a[e4] = a[e3];
            a[e3] = t;
            if (t.compareTo(a[e2]) < 0) {
                a[e3] = a[e2];
                a[e2] = t;
                if (t.compareTo(a[e1]) < 0) {
                    a[e2] = a[e1];
                    a[e1] = t;
                }
            }
        }
        if (a[e5].compareTo(a[e4]) < 0) {
            final X t = a[e5];
            a[e5] = a[e4];
            a[e4] = t;
            if (t.compareTo(a[e3]) < 0) {
                a[e4] = a[e3];
                a[e3] = t;
                if (t.compareTo(a[e2]) < 0) {
                    a[e3] = a[e2];
                    a[e2] = t;
                    if (t.compareTo(a[e1]) < 0) {
                        a[e2] = a[e1];
                        a[e1] = t;
                    }
                }
            }
        }

        // Pointers
        int less = left;  // The index of the first element of center part
        int great = right; // The index before the first element of right part

        if (a[e1] != a[e2] && a[e2] != a[e3] && a[e3] != a[e4] && a[e4] != a[e5]) {
            /*
             * Use the second and fourth of the five sorted elements as pivots.
             * These values are inexpensive approximations of the first and
             * second terciles of the array. Note that pivot1 <= pivot2.
             */
            final X pivot1 = a[e2];
            final X pivot2 = a[e4];

            /*
             * The first and the last elements to be sorted are moved to the
             * locations formerly occupied by the pivots. When partitioning
             * is complete, the pivots are swapped back into their final
             * positions, and excluded from subsequent sorting.
             */
            a[e2] = a[left];
            a[e4] = a[right];

            /*
             * Skip elements, which are less or greater than pivot values.
             */
            while (a[++less].compareTo(pivot1) < 0) ;
            while (a[--great].compareTo(pivot2) > 0) ;

            /*
             * Partitioning:
             *
             *   left part           center part                   right part
             * +--------------------------------------------------------------+
             * |  < pivot1  |  pivot1 <= && <= pivot2  |    ?    |  > pivot2  |
             * +--------------------------------------------------------------+
             *               ^                          ^       ^
             *               |                          |       |
             *              less                        k     great
             *
             * Invariants:
             *
             *              all in (left, less)   < pivot1
             *    pivot1 <= all in [less, k)     <= pivot2
             *              all in (great, right) > pivot2
             *
             * Pointer k is the first index of ?-part.
             */
            outer:
            for (int k = less - 1; ++k <= great; ) {
                final X ak = a[k];
                if (ak.compareTo(pivot1) < 0) { // Move a[k] to left part
                    a[k] = a[less];
                    /*
                     * Here and below we use "a[i] = b; i++;" instead
                     * of "a[i++] = b;" due to performance issue.
                     */
                    a[less] = ak;
                    ++less;
                } else if (ak.compareTo(pivot2) > 0) { // Move a[k] to right part
                    while (a[great].compareTo(pivot2) > 0) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great].compareTo(pivot1) < 0) { // a[great] <= pivot2
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { // pivot1 <= a[great] <= pivot2
                        a[k] = a[great];
                    }
                    /*
                     * Here and below we use "a[i] = b; i--;" instead
                     * of "a[i--] = b;" due to performance issue.
                     */
                    a[great] = ak;
                    --great;
                }
            }

            // Swap pivots into their final positions
            a[left] = a[less - 1];
            a[less - 1] = pivot1;
            a[right] = a[great + 1];
            a[great + 1] = pivot2;

            // Sort left and right parts recursively, excluding known pivots
            sort(a, left, less - 2, leftmost);
            sort(a, great + 2, right, false);

            /*
             * If center part is too large (comprises > 4/7 of the array),
             * swap internal pivot values to ends.
             */
            if (less < e1 && e5 < great) {
                /*
                 * Skip elements, which are equal to pivot values.
                 */
                while (a[less] == pivot1) {
                    ++less;
                }

                while (a[great] == pivot2) {
                    --great;
                }

                /*
                 * Partitioning:
                 *
                 *   left part         center part                  right part
                 * +----------------------------------------------------------+
                 * | == pivot1 |  pivot1 < && < pivot2  |    ?    | == pivot2 |
                 * +----------------------------------------------------------+
                 *              ^                        ^       ^
                 *              |                        |       |
                 *             less                      k     great
                 *
                 * Invariants:
                 *
                 *              all in (*,  less) == pivot1
                 *     pivot1 < all in [less,  k)  < pivot2
                 *              all in (great, *) == pivot2
                 *
                 * Pointer k is the first index of ?-part.
                 */
                outer:
                for (int k = less - 1; ++k <= great; ) {
                    final X ak = a[k];
                    if (ak == pivot1) { // Move a[k] to left part
                        a[k] = a[less];
                        a[less] = ak;
                        ++less;
                    } else if (ak == pivot2) { // Move a[k] to right part
                        while (a[great] == pivot2) {
                            if (great-- == k) {
                                break outer;
                            }
                        }
                        if (a[great] == pivot1) { // a[great] < pivot2
                            a[k] = a[less];
                            /*
                             * Even though a[great] equals to pivot1, the
                             * assignment a[less] = pivot1 may be incorrect,
                             * if a[great] and pivot1 are floating-point zeros
                             * of different signs. Therefore in float and
                             * double sorting methods we have to use more
                             * accurate assignment a[less] = a[great].
                             */
                            a[less] = pivot1;
                            ++less;
                        } else { // pivot1 < a[great] < pivot2
                            a[k] = a[great];
                        }
                        a[great] = ak;
                        --great;
                    }
                }
            }

            // Sort center part recursively
            sort(a, less, great, false);

        } else { // Partitioning with one pivot
            /*
             * Use the third of the five sorted elements as pivot.
             * This value is inexpensive approximation of the median.
             */
            final X pivot = a[e3];

            /*
             * Partitioning degenerates to the traditional 3-way
             * (or "Dutch National Flag") schema:
             *
             *   left part    center part              right part
             * +-------------------------------------------------+
             * |  < pivot  |   == pivot   |     ?    |  > pivot  |
             * +-------------------------------------------------+
             *              ^              ^        ^
             *              |              |        |
             *             less            k      great
             *
             * Invariants:
             *
             *   all in (left, less)   < pivot
             *   all in [less, k)     == pivot
             *   all in (great, right) > pivot
             *
             * Pointer k is the first index of ?-part.
             */
            for (int k = less; k <= great; ++k) {
                if (a[k] == pivot) {
                    continue;
                }
                final X ak = a[k];
                if (ak.compareTo(pivot) < 0) { // Move a[k] to left part
                    a[k] = a[less];
                    a[less] = ak;
                    ++less;
                } else { // a[k] > pivot - Move a[k] to right part
                    while (a[great].compareTo(pivot) > 0) {
                        --great;
                    }
                    if (a[great].compareTo(pivot) < 0) { // a[great] <= pivot
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { // a[great] == pivot
                        /*
                         * Even though a[great] equals to pivot, the
                         * assignment a[k] = pivot may be incorrect,
                         * if a[great] and pivot are floating-point
                         * zeros of different signs. Therefore in float
                         * and double sorting methods we have to use
                         * more accurate assignment a[k] = a[great].
                         */
                        a[k] = pivot;
                    }
                    a[great] = ak;
                    --great;
                }
            }

            /*
             * Sort left and right parts recursively.
             * All elements from center part are equal
             * and, therefore, already sorted.
             */
            sort(a, left, less - 1, leftmost);
            sort(a, great + 1, right, false);
        }
    }
//
//    /**
//     * Sorts the specified range of the array using the given
//     * workspace array slice if possible for merging
//     *
//     * @param a the array to be sorted
//     * @param left the index of the first element, inclusive, to be sorted
//     * @param right the index of the last element, inclusive, to be sorted
//     * @param work a workspace array (slice)
//     * @param workBase origin of usable space in work array
//     * @param workLen usable size of work array
//     */
//    static void sort(long[] a, final int left, int right,
//                     long[] work, int workBase, final int workLen) {
//        // Use Quicksort on small arrays
//        if (right - left < QUICKSORT_THRESHOLD) {
//            sort(a, left, right, true);
//            return;
//        }
//
//        /*
//         * Index run[i] is the start of i-th run
//         * (ascending or descending sequence).
//         */
//        final int[] run = new int[MAX_RUN_COUNT + 1];
//        int count = 0; run[0] = left;
//
//        // Check if the array is nearly sorted
//        for (int k = left; k < right; run[count] = k) {
//            if (a[k] < a[k + 1]) { // ascending
//                while (++k <= right && a[k - 1] <= a[k]);
//            } else if (a[k] > a[k + 1]) { // descending
//                while (++k <= right && a[k - 1] >= a[k]);
//                for (int lo = run[count] - 1, hi = k; ++lo < --hi; ) {
//                    final long t = a[lo]; a[lo] = a[hi]; a[hi] = t;
//                }
//            } else { // equal
//                for (int m = MAX_RUN_LENGTH; ++k <= right && a[k - 1] == a[k]; ) {
//                    if (--m == 0) {
//                        sort(a, left, right, true);
//                        return;
//                    }
//                }
//            }
//
//            /*
//             * The array is not highly structured,
//             * use Quicksort instead of merge sort.
//             */
//            if (++count == MAX_RUN_COUNT) {
//                sort(a, left, right, true);
//                return;
//            }
//        }
//
//        // Check special cases
//        // Implementation note: variable "right" is increased by 1.
//        if (run[count] == right++) { // The last run contains one element
//            run[++count] = right;
//        } else if (count == 1) { // The array is already sorted
//            return;
//        }
//
//        // Determine alternation base for merge
//        byte odd = 0;
//        for (int n = 1; (n <<= 1) < count; odd ^= 1);
//
//        // Use or create temporary array b for merging
//        long[] b;                 // temp array; alternates with a
//        int ao, bo;              // array offsets from 'left'
//        final int blen = right - left; // space needed for b
//        if (work == null || workLen < blen || workBase + blen > work.length) {
//            work = new long[blen];
//            workBase = 0;
//        }
//        if (odd == 0) {
//            System.arraycopy(a, left, work, workBase, blen);
//            b = a;
//            bo = 0;
//            a = work;
//            ao = workBase - left;
//        } else {
//            b = work;
//            ao = 0;
//            bo = workBase - left;
//        }
//
//        // Merging
//        for (int last; count > 1; count = last) {
//            for (int k = (last = 0) + 2; k <= count; k += 2) {
//                final int hi = run[k];
//                final int mi = run[k - 1];
//                for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
//                    if (q >= hi || p < mi && a[p + ao] <= a[q + ao]) {
//                        b[i + bo] = a[p++ + ao];
//                    } else {
//                        b[i + bo] = a[q++ + ao];
//                    }
//                }
//                run[++last] = hi;
//            }
//            if ((count & 1) != 0) {
//                for (int i = right, lo = run[count - 1]; --i >= lo;
//                     b[i + bo] = a[i + ao]
//                );
//                run[++last] = right;
//            }
//            final long[] t = a; a = b; b = t;
//            final int o = ao; ao = bo; bo = o;
//        }
//    }
//
//    /**
//     * Sorts the specified range of the array by Dual-Pivot Quicksort.
//     *
//     * @param a the array to be sorted
//     * @param left the index of the first element, inclusive, to be sorted
//     * @param right the index of the last element, inclusive, to be sorted
//     * @param leftmost indicates if this part is the leftmost in the range
//     */
//    private static void sort(final long[] a, int left, int right, final boolean leftmost) {
//        final int length = right - left + 1;
//
//        // Use insertion sort on tiny arrays
//        if (length < INSERTION_SORT_THRESHOLD) {
//            if (leftmost) {
//                /*
//                 * Traditional (without sentinel) insertion sort,
//                 * optimized for server VM, is used in case of
//                 * the leftmost part.
//                 */
//                for (int i = left, j = i; i < right; j = ++i) {
//                    final long ai = a[i + 1];
//                    while (ai < a[j]) {
//                        a[j + 1] = a[j];
//                        if (j-- == left) {
//                            break;
//                        }
//                    }
//                    a[j + 1] = ai;
//                }
//            } else {
//                /*
//                 * Skip the longest ascending sequence.
//                 */
//                do {
//                    if (left >= right) {
//                        return;
//                    }
//                } while (a[++left] >= a[left - 1]);
//
//                /*
//                 * Every element from adjoining part plays the role
//                 * of sentinel, therefore this allows us to avoid the
//                 * left range check on each iteration. Moreover, we use
//                 * the more optimized algorithm, so called pair insertion
//                 * sort, which is faster (in the context of Quicksort)
//                 * than traditional implementation of insertion sort.
//                 */
//                for (int k = left; ++left <= right; k = ++left) {
//                    long a1 = a[k], a2 = a[left];
//
//                    if (a1 < a2) {
//                        a2 = a1; a1 = a[left];
//                    }
//                    while (a1 < a[--k]) {
//                        a[k + 2] = a[k];
//                    }
//                    a[++k + 1] = a1;
//
//                    while (a2 < a[--k]) {
//                        a[k + 1] = a[k];
//                    }
//                    a[k + 1] = a2;
//                }
//                final long last = a[right];
//
//                while (last < a[--right]) {
//                    a[right + 1] = a[right];
//                }
//                a[right + 1] = last;
//            }
//            return;
//        }
//
//        // Inexpensive approximation of length / 7
//        final int seventh = (length >> 3) + (length >> 6) + 1;
//
//        /*
//         * Sort five evenly spaced elements around (and including) the
//         * center element in the range. These elements will be used for
//         * pivot selection as described below. The choice for spacing
//         * these elements was empirically determined to work well on
//         * a wide variety of inputs.
//         */
//        final int e3 = (left + right) >>> 1; // The midpoint
//        final int e2 = e3 - seventh;
//        final int e1 = e2 - seventh;
//        final int e4 = e3 + seventh;
//        final int e5 = e4 + seventh;
//
//        // Sort these elements using insertion sort
//        if (a[e2] < a[e1]) { final long t = a[e2]; a[e2] = a[e1]; a[e1] = t; }
//
//        if (a[e3] < a[e2]) { final long t = a[e3]; a[e3] = a[e2]; a[e2] = t;
//            if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
//        }
//        if (a[e4] < a[e3]) { final long t = a[e4]; a[e4] = a[e3]; a[e3] = t;
//            if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
//                if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
//            }
//        }
//        if (a[e5] < a[e4]) { final long t = a[e5]; a[e5] = a[e4]; a[e4] = t;
//            if (t < a[e3]) { a[e4] = a[e3]; a[e3] = t;
//                if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
//                    if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
//                }
//            }
//        }
//
//        // Pointers
//        int less  = left;  // The index of the first element of center part
//        int great = right; // The index before the first element of right part
//
//        if (a[e1] != a[e2] && a[e2] != a[e3] && a[e3] != a[e4] && a[e4] != a[e5]) {
//            /*
//             * Use the second and fourth of the five sorted elements as pivots.
//             * These values are inexpensive approximations of the first and
//             * second terciles of the array. Note that pivot1 <= pivot2.
//             */
//            final long pivot1 = a[e2];
//            final long pivot2 = a[e4];
//
//            /*
//             * The first and the last elements to be sorted are moved to the
//             * locations formerly occupied by the pivots. When partitioning
//             * is complete, the pivots are swapped back into their final
//             * positions, and excluded from subsequent sorting.
//             */
//            a[e2] = a[left];
//            a[e4] = a[right];
//
//            /*
//             * Skip elements, which are less or greater than pivot values.
//             */
//            while (a[++less] < pivot1);
//            while (a[--great] > pivot2);
//
//            /*
//             * Partitioning:
//             *
//             *   left part           center part                   right part
//             * +--------------------------------------------------------------+
//             * |  < pivot1  |  pivot1 <= && <= pivot2  |    ?    |  > pivot2  |
//             * +--------------------------------------------------------------+
//             *               ^                          ^       ^
//             *               |                          |       |
//             *              less                        k     great
//             *
//             * Invariants:
//             *
//             *              all in (left, less)   < pivot1
//             *    pivot1 <= all in [less, k)     <= pivot2
//             *              all in (great, right) > pivot2
//             *
//             * Pointer k is the first index of ?-part.
//             */
//            outer:
//            for (int k = less - 1; ++k <= great; ) {
//                final long ak = a[k];
//                if (ak < pivot1) { // Move a[k] to left part
//                    a[k] = a[less];
//                    /*
//                     * Here and below we use "a[i] = b; i++;" instead
//                     * of "a[i++] = b;" due to performance issue.
//                     */
//                    a[less] = ak;
//                    ++less;
//                } else if (ak > pivot2) { // Move a[k] to right part
//                    while (a[great] > pivot2) {
//                        if (great-- == k) {
//                            break outer;
//                        }
//                    }
//                    if (a[great] < pivot1) { // a[great] <= pivot2
//                        a[k] = a[less];
//                        a[less] = a[great];
//                        ++less;
//                    } else { // pivot1 <= a[great] <= pivot2
//                        a[k] = a[great];
//                    }
//                    /*
//                     * Here and below we use "a[i] = b; i--;" instead
//                     * of "a[i--] = b;" due to performance issue.
//                     */
//                    a[great] = ak;
//                    --great;
//                }
//            }
//
//            // Swap pivots into their final positions
//            a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
//            a[right] = a[great + 1]; a[great + 1] = pivot2;
//
//            // Sort left and right parts recursively, excluding known pivots
//            sort(a, left, less - 2, leftmost);
//            sort(a, great + 2, right, false);
//
//            /*
//             * If center part is too large (comprises > 4/7 of the array),
//             * swap internal pivot values to ends.
//             */
//            if (less < e1 && e5 < great) {
//                /*
//                 * Skip elements, which are equal to pivot values.
//                 */
//                while (a[less] == pivot1) {
//                    ++less;
//                }
//
//                while (a[great] == pivot2) {
//                    --great;
//                }
//
//                /*
//                 * Partitioning:
//                 *
//                 *   left part         center part                  right part
//                 * +----------------------------------------------------------+
//                 * | == pivot1 |  pivot1 < && < pivot2  |    ?    | == pivot2 |
//                 * +----------------------------------------------------------+
//                 *              ^                        ^       ^
//                 *              |                        |       |
//                 *             less                      k     great
//                 *
//                 * Invariants:
//                 *
//                 *              all in (*,  less) == pivot1
//                 *     pivot1 < all in [less,  k)  < pivot2
//                 *              all in (great, *) == pivot2
//                 *
//                 * Pointer k is the first index of ?-part.
//                 */
//                outer:
//                for (int k = less - 1; ++k <= great; ) {
//                    final long ak = a[k];
//                    if (ak == pivot1) { // Move a[k] to left part
//                        a[k] = a[less];
//                        a[less] = ak;
//                        ++less;
//                    } else if (ak == pivot2) { // Move a[k] to right part
//                        while (a[great] == pivot2) {
//                            if (great-- == k) {
//                                break outer;
//                            }
//                        }
//                        if (a[great] == pivot1) { // a[great] < pivot2
//                            a[k] = a[less];
//                            /*
//                             * Even though a[great] equals to pivot1, the
//                             * assignment a[less] = pivot1 may be incorrect,
//                             * if a[great] and pivot1 are floating-point zeros
//                             * of different signs. Therefore in float and
//                             * double sorting methods we have to use more
//                             * accurate assignment a[less] = a[great].
//                             */
//                            a[less] = pivot1;
//                            ++less;
//                        } else { // pivot1 < a[great] < pivot2
//                            a[k] = a[great];
//                        }
//                        a[great] = ak;
//                        --great;
//                    }
//                }
//            }
//
//            // Sort center part recursively
//            sort(a, less, great, false);
//
//        } else { // Partitioning with one pivot
//            /*
//             * Use the third of the five sorted elements as pivot.
//             * This value is inexpensive approximation of the median.
//             */
//            final long pivot = a[e3];
//
//            /*
//             * Partitioning degenerates to the traditional 3-way
//             * (or "Dutch National Flag") schema:
//             *
//             *   left part    center part              right part
//             * +-------------------------------------------------+
//             * |  < pivot  |   == pivot   |     ?    |  > pivot  |
//             * +-------------------------------------------------+
//             *              ^              ^        ^
//             *              |              |        |
//             *             less            k      great
//             *
//             * Invariants:
//             *
//             *   all in (left, less)   < pivot
//             *   all in [less, k)     == pivot
//             *   all in (great, right) > pivot
//             *
//             * Pointer k is the first index of ?-part.
//             */
//            for (int k = less; k <= great; ++k) {
//                if (a[k] == pivot) {
//                    continue;
//                }
//                final long ak = a[k];
//                if (ak < pivot) { // Move a[k] to left part
//                    a[k] = a[less];
//                    a[less] = ak;
//                    ++less;
//                } else { // a[k] > pivot - Move a[k] to right part
//                    while (a[great] > pivot) {
//                        --great;
//                    }
//                    if (a[great] < pivot) { // a[great] <= pivot
//                        a[k] = a[less];
//                        a[less] = a[great];
//                        ++less;
//                    } else { // a[great] == pivot
//                        /*
//                         * Even though a[great] equals to pivot, the
//                         * assignment a[k] = pivot may be incorrect,
//                         * if a[great] and pivot are floating-point
//                         * zeros of different signs. Therefore in float
//                         * and double sorting methods we have to use
//                         * more accurate assignment a[k] = a[great].
//                         */
//                        a[k] = pivot;
//                    }
//                    a[great] = ak;
//                    --great;
//                }
//            }
//
//            /*
//             * Sort left and right parts recursively.
//             * All elements from center part are equal
//             * and, therefore, already sorted.
//             */
//            sort(a, left, less - 1, leftmost);
//            sort(a, great + 1, right, false);
//        }
//    }
//
//    /**
//     * Sorts the specified range of the array using the given
//     * workspace array slice if possible for merging
//     *
//     * @param a the array to be sorted
//     * @param left the index of the first element, inclusive, to be sorted
//     * @param right the index of the last element, inclusive, to be sorted
//     * @param work a workspace array (slice)
//     * @param workBase origin of usable space in work array
//     * @param workLen usable size of work array
//     */
//    static void sort(final short[] a, final int left, final int right,
//                     final short[] work, final int workBase, final int workLen) {
//        // Use counting sort on large arrays
//        if (right - left > COUNTING_SORT_THRESHOLD_FOR_SHORT_OR_CHAR) {
//            final int[] count = new int[NUM_SHORT_VALUES];
//
//            for (int i = left - 1; ++i <= right;
//                 count[a[i] - Short.MIN_VALUE]++
//            );
//            for (int i = NUM_SHORT_VALUES, k = right + 1; k > left; ) {
//                while (count[--i] == 0);
//                final short value = (short) (i + Short.MIN_VALUE);
//                int s = count[i];
//
//                do {
//                    a[--k] = value;
//                } while (--s > 0);
//            }
//        } else { // Use Dual-Pivot Quicksort on small arrays
//            doSort(a, left, right, work, workBase, workLen);
//        }
//    }
//
//    /** The number of distinct short values. */
//    private static final int NUM_SHORT_VALUES = 1 << 16;
//
//    /**
//     * Sorts the specified range of the array.
//     *
//     * @param a the array to be sorted
//     * @param left the index of the first element, inclusive, to be sorted
//     * @param right the index of the last element, inclusive, to be sorted
//     * @param work a workspace array (slice)
//     * @param workBase origin of usable space in work array
//     * @param workLen usable size of work array
//     */
//    private static void doSort(short[] a, final int left, int right,
//                               short[] work, int workBase, final int workLen) {
//        // Use Quicksort on small arrays
//        if (right - left < QUICKSORT_THRESHOLD) {
//            sort(a, left, right, true);
//            return;
//        }
//
//        /*
//         * Index run[i] is the start of i-th run
//         * (ascending or descending sequence).
//         */
//        final int[] run = new int[MAX_RUN_COUNT + 1];
//        int count = 0; run[0] = left;
//
//        // Check if the array is nearly sorted
//        for (int k = left; k < right; run[count] = k) {
//            if (a[k] < a[k + 1]) { // ascending
//                while (++k <= right && a[k - 1] <= a[k]);
//            } else if (a[k] > a[k + 1]) { // descending
//                while (++k <= right && a[k - 1] >= a[k]);
//                for (int lo = run[count] - 1, hi = k; ++lo < --hi; ) {
//                    final short t = a[lo]; a[lo] = a[hi]; a[hi] = t;
//                }
//            } else { // equal
//                for (int m = MAX_RUN_LENGTH; ++k <= right && a[k - 1] == a[k]; ) {
//                    if (--m == 0) {
//                        sort(a, left, right, true);
//                        return;
//                    }
//                }
//            }
//
//            /*
//             * The array is not highly structured,
//             * use Quicksort instead of merge sort.
//             */
//            if (++count == MAX_RUN_COUNT) {
//                sort(a, left, right, true);
//                return;
//            }
//        }
//
//        // Check special cases
//        // Implementation note: variable "right" is increased by 1.
//        if (run[count] == right++) { // The last run contains one element
//            run[++count] = right;
//        } else if (count == 1) { // The array is already sorted
//            return;
//        }
//
//        // Determine alternation base for merge
//        byte odd = 0;
//        for (int n = 1; (n <<= 1) < count; odd ^= 1);
//
//        // Use or create temporary array b for merging
//        short[] b;                 // temp array; alternates with a
//        int ao, bo;              // array offsets from 'left'
//        final int blen = right - left; // space needed for b
//        if (work == null || workLen < blen || workBase + blen > work.length) {
//            work = new short[blen];
//            workBase = 0;
//        }
//        if (odd == 0) {
//            System.arraycopy(a, left, work, workBase, blen);
//            b = a;
//            bo = 0;
//            a = work;
//            ao = workBase - left;
//        } else {
//            b = work;
//            ao = 0;
//            bo = workBase - left;
//        }
//
//        // Merging
//        for (int last; count > 1; count = last) {
//            for (int k = (last = 0) + 2; k <= count; k += 2) {
//                final int hi = run[k];
//                final int mi = run[k - 1];
//                for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
//                    if (q >= hi || p < mi && a[p + ao] <= a[q + ao]) {
//                        b[i + bo] = a[p++ + ao];
//                    } else {
//                        b[i + bo] = a[q++ + ao];
//                    }
//                }
//                run[++last] = hi;
//            }
//            if ((count & 1) != 0) {
//                for (int i = right, lo = run[count - 1]; --i >= lo;
//                     b[i + bo] = a[i + ao]
//                );
//                run[++last] = right;
//            }
//            final short[] t = a; a = b; b = t;
//            final int o = ao; ao = bo; bo = o;
//        }
//    }
//
//    /**
//     * Sorts the specified range of the array by Dual-Pivot Quicksort.
//     *
//     * @param a the array to be sorted
//     * @param left the index of the first element, inclusive, to be sorted
//     * @param right the index of the last element, inclusive, to be sorted
//     * @param leftmost indicates if this part is the leftmost in the range
//     */
//    private static void sort(final short[] a, int left, int right, final boolean leftmost) {
//        final int length = right - left + 1;
//
//        // Use insertion sort on tiny arrays
//        if (length < INSERTION_SORT_THRESHOLD) {
//            if (leftmost) {
//                /*
//                 * Traditional (without sentinel) insertion sort,
//                 * optimized for server VM, is used in case of
//                 * the leftmost part.
//                 */
//                for (int i = left, j = i; i < right; j = ++i) {
//                    final short ai = a[i + 1];
//                    while (ai < a[j]) {
//                        a[j + 1] = a[j];
//                        if (j-- == left) {
//                            break;
//                        }
//                    }
//                    a[j + 1] = ai;
//                }
//            } else {
//                /*
//                 * Skip the longest ascending sequence.
//                 */
//                do {
//                    if (left >= right) {
//                        return;
//                    }
//                } while (a[++left] >= a[left - 1]);
//
//                /*
//                 * Every element from adjoining part plays the role
//                 * of sentinel, therefore this allows us to avoid the
//                 * left range check on each iteration. Moreover, we use
//                 * the more optimized algorithm, so called pair insertion
//                 * sort, which is faster (in the context of Quicksort)
//                 * than traditional implementation of insertion sort.
//                 */
//                for (int k = left; ++left <= right; k = ++left) {
//                    short a1 = a[k], a2 = a[left];
//
//                    if (a1 < a2) {
//                        a2 = a1; a1 = a[left];
//                    }
//                    while (a1 < a[--k]) {
//                        a[k + 2] = a[k];
//                    }
//                    a[++k + 1] = a1;
//
//                    while (a2 < a[--k]) {
//                        a[k + 1] = a[k];
//                    }
//                    a[k + 1] = a2;
//                }
//                final short last = a[right];
//
//                while (last < a[--right]) {
//                    a[right + 1] = a[right];
//                }
//                a[right + 1] = last;
//            }
//            return;
//        }
//
//        // Inexpensive approximation of length / 7
//        final int seventh = (length >> 3) + (length >> 6) + 1;
//
//        /*
//         * Sort five evenly spaced elements around (and including) the
//         * center element in the range. These elements will be used for
//         * pivot selection as described below. The choice for spacing
//         * these elements was empirically determined to work well on
//         * a wide variety of inputs.
//         */
//        final int e3 = (left + right) >>> 1; // The midpoint
//        final int e2 = e3 - seventh;
//        final int e1 = e2 - seventh;
//        final int e4 = e3 + seventh;
//        final int e5 = e4 + seventh;
//
//        // Sort these elements using insertion sort
//        if (a[e2] < a[e1]) { final short t = a[e2]; a[e2] = a[e1]; a[e1] = t; }
//
//        if (a[e3] < a[e2]) { final short t = a[e3]; a[e3] = a[e2]; a[e2] = t;
//            if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
//        }
//        if (a[e4] < a[e3]) { final short t = a[e4]; a[e4] = a[e3]; a[e3] = t;
//            if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
//                if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
//            }
//        }
//        if (a[e5] < a[e4]) { final short t = a[e5]; a[e5] = a[e4]; a[e4] = t;
//            if (t < a[e3]) { a[e4] = a[e3]; a[e3] = t;
//                if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
//                    if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
//                }
//            }
//        }
//
//        // Pointers
//        int less  = left;  // The index of the first element of center part
//        int great = right; // The index before the first element of right part
//
//        if (a[e1] != a[e2] && a[e2] != a[e3] && a[e3] != a[e4] && a[e4] != a[e5]) {
//            /*
//             * Use the second and fourth of the five sorted elements as pivots.
//             * These values are inexpensive approximations of the first and
//             * second terciles of the array. Note that pivot1 <= pivot2.
//             */
//            final short pivot1 = a[e2];
//            final short pivot2 = a[e4];
//
//            /*
//             * The first and the last elements to be sorted are moved to the
//             * locations formerly occupied by the pivots. When partitioning
//             * is complete, the pivots are swapped back into their final
//             * positions, and excluded from subsequent sorting.
//             */
//            a[e2] = a[left];
//            a[e4] = a[right];
//
//            /*
//             * Skip elements, which are less or greater than pivot values.
//             */
//            while (a[++less] < pivot1);
//            while (a[--great] > pivot2);
//
//            /*
//             * Partitioning:
//             *
//             *   left part           center part                   right part
//             * +--------------------------------------------------------------+
//             * |  < pivot1  |  pivot1 <= && <= pivot2  |    ?    |  > pivot2  |
//             * +--------------------------------------------------------------+
//             *               ^                          ^       ^
//             *               |                          |       |
//             *              less                        k     great
//             *
//             * Invariants:
//             *
//             *              all in (left, less)   < pivot1
//             *    pivot1 <= all in [less, k)     <= pivot2
//             *              all in (great, right) > pivot2
//             *
//             * Pointer k is the first index of ?-part.
//             */
//            outer:
//            for (int k = less - 1; ++k <= great; ) {
//                final short ak = a[k];
//                if (ak < pivot1) { // Move a[k] to left part
//                    a[k] = a[less];
//                    /*
//                     * Here and below we use "a[i] = b; i++;" instead
//                     * of "a[i++] = b;" due to performance issue.
//                     */
//                    a[less] = ak;
//                    ++less;
//                } else if (ak > pivot2) { // Move a[k] to right part
//                    while (a[great] > pivot2) {
//                        if (great-- == k) {
//                            break outer;
//                        }
//                    }
//                    if (a[great] < pivot1) { // a[great] <= pivot2
//                        a[k] = a[less];
//                        a[less] = a[great];
//                        ++less;
//                    } else { // pivot1 <= a[great] <= pivot2
//                        a[k] = a[great];
//                    }
//                    /*
//                     * Here and below we use "a[i] = b; i--;" instead
//                     * of "a[i--] = b;" due to performance issue.
//                     */
//                    a[great] = ak;
//                    --great;
//                }
//            }
//
//            // Swap pivots into their final positions
//            a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
//            a[right] = a[great + 1]; a[great + 1] = pivot2;
//
//            // Sort left and right parts recursively, excluding known pivots
//            sort(a, left, less - 2, leftmost);
//            sort(a, great + 2, right, false);
//
//            /*
//             * If center part is too large (comprises > 4/7 of the array),
//             * swap internal pivot values to ends.
//             */
//            if (less < e1 && e5 < great) {
//                /*
//                 * Skip elements, which are equal to pivot values.
//                 */
//                while (a[less] == pivot1) {
//                    ++less;
//                }
//
//                while (a[great] == pivot2) {
//                    --great;
//                }
//
//                /*
//                 * Partitioning:
//                 *
//                 *   left part         center part                  right part
//                 * +----------------------------------------------------------+
//                 * | == pivot1 |  pivot1 < && < pivot2  |    ?    | == pivot2 |
//                 * +----------------------------------------------------------+
//                 *              ^                        ^       ^
//                 *              |                        |       |
//                 *             less                      k     great
//                 *
//                 * Invariants:
//                 *
//                 *              all in (*,  less) == pivot1
//                 *     pivot1 < all in [less,  k)  < pivot2
//                 *              all in (great, *) == pivot2
//                 *
//                 * Pointer k is the first index of ?-part.
//                 */
//                outer:
//                for (int k = less - 1; ++k <= great; ) {
//                    final short ak = a[k];
//                    if (ak == pivot1) { // Move a[k] to left part
//                        a[k] = a[less];
//                        a[less] = ak;
//                        ++less;
//                    } else if (ak == pivot2) { // Move a[k] to right part
//                        while (a[great] == pivot2) {
//                            if (great-- == k) {
//                                break outer;
//                            }
//                        }
//                        if (a[great] == pivot1) { // a[great] < pivot2
//                            a[k] = a[less];
//                            /*
//                             * Even though a[great] equals to pivot1, the
//                             * assignment a[less] = pivot1 may be incorrect,
//                             * if a[great] and pivot1 are floating-point zeros
//                             * of different signs. Therefore in float and
//                             * double sorting methods we have to use more
//                             * accurate assignment a[less] = a[great].
//                             */
//                            a[less] = pivot1;
//                            ++less;
//                        } else { // pivot1 < a[great] < pivot2
//                            a[k] = a[great];
//                        }
//                        a[great] = ak;
//                        --great;
//                    }
//                }
//            }
//
//            // Sort center part recursively
//            sort(a, less, great, false);
//
//        } else { // Partitioning with one pivot
//            /*
//             * Use the third of the five sorted elements as pivot.
//             * This value is inexpensive approximation of the median.
//             */
//            final short pivot = a[e3];
//
//            /*
//             * Partitioning degenerates to the traditional 3-way
//             * (or "Dutch National Flag") schema:
//             *
//             *   left part    center part              right part
//             * +-------------------------------------------------+
//             * |  < pivot  |   == pivot   |     ?    |  > pivot  |
//             * +-------------------------------------------------+
//             *              ^              ^        ^
//             *              |              |        |
//             *             less            k      great
//             *
//             * Invariants:
//             *
//             *   all in (left, less)   < pivot
//             *   all in [less, k)     == pivot
//             *   all in (great, right) > pivot
//             *
//             * Pointer k is the first index of ?-part.
//             */
//            for (int k = less; k <= great; ++k) {
//                if (a[k] == pivot) {
//                    continue;
//                }
//                final short ak = a[k];
//                if (ak < pivot) { // Move a[k] to left part
//                    a[k] = a[less];
//                    a[less] = ak;
//                    ++less;
//                } else { // a[k] > pivot - Move a[k] to right part
//                    while (a[great] > pivot) {
//                        --great;
//                    }
//                    if (a[great] < pivot) { // a[great] <= pivot
//                        a[k] = a[less];
//                        a[less] = a[great];
//                        ++less;
//                    } else { // a[great] == pivot
//                        /*
//                         * Even though a[great] equals to pivot, the
//                         * assignment a[k] = pivot may be incorrect,
//                         * if a[great] and pivot are floating-point
//                         * zeros of different signs. Therefore in float
//                         * and double sorting methods we have to use
//                         * more accurate assignment a[k] = a[great].
//                         */
//                        a[k] = pivot;
//                    }
//                    a[great] = ak;
//                    --great;
//                }
//            }
//
//            /*
//             * Sort left and right parts recursively.
//             * All elements from center part are equal
//             * and, therefore, already sorted.
//             */
//            sort(a, left, less - 1, leftmost);
//            sort(a, great + 1, right, false);
//        }
//    }
//
//    /**
//     * Sorts the specified range of the array using the given
//     * workspace array slice if possible for merging
//     *
//     * @param a the array to be sorted
//     * @param left the index of the first element, inclusive, to be sorted
//     * @param right the index of the last element, inclusive, to be sorted
//     * @param work a workspace array (slice)
//     * @param workBase origin of usable space in work array
//     * @param workLen usable size of work array
//     */
//    static void sort(final char[] a, final int left, final int right,
//                     final char[] work, final int workBase, final int workLen) {
//        // Use counting sort on large arrays
//        if (right - left > COUNTING_SORT_THRESHOLD_FOR_SHORT_OR_CHAR) {
//            final int[] count = new int[NUM_CHAR_VALUES];
//
//            for (int i = left - 1; ++i <= right;
//                 count[a[i]]++
//            );
//            for (int i = NUM_CHAR_VALUES, k = right + 1; k > left; ) {
//                while (count[--i] == 0);
//                final char value = (char) i;
//                int s = count[i];
//
//                do {
//                    a[--k] = value;
//                } while (--s > 0);
//            }
//        } else { // Use Dual-Pivot Quicksort on small arrays
//            doSort(a, left, right, work, workBase, workLen);
//        }
//    }
//
//    /** The number of distinct char values. */
//    private static final int NUM_CHAR_VALUES = 1 << 16;
//
//    /**
//     * Sorts the specified range of the array.
//     *
//     * @param a the array to be sorted
//     * @param left the index of the first element, inclusive, to be sorted
//     * @param right the index of the last element, inclusive, to be sorted
//     * @param work a workspace array (slice)
//     * @param workBase origin of usable space in work array
//     * @param workLen usable size of work array
//     */
//    private static void doSort(char[] a, final int left, int right,
//                               char[] work, int workBase, final int workLen) {
//        // Use Quicksort on small arrays
//        if (right - left < QUICKSORT_THRESHOLD) {
//            sort(a, left, right, true);
//            return;
//        }
//
//        /*
//         * Index run[i] is the start of i-th run
//         * (ascending or descending sequence).
//         */
//        final int[] run = new int[MAX_RUN_COUNT + 1];
//        int count = 0; run[0] = left;
//
//        // Check if the array is nearly sorted
//        for (int k = left; k < right; run[count] = k) {
//            if (a[k] < a[k + 1]) { // ascending
//                while (++k <= right && a[k - 1] <= a[k]);
//            } else if (a[k] > a[k + 1]) { // descending
//                while (++k <= right && a[k - 1] >= a[k]);
//                for (int lo = run[count] - 1, hi = k; ++lo < --hi; ) {
//                    final char t = a[lo]; a[lo] = a[hi]; a[hi] = t;
//                }
//            } else { // equal
//                for (int m = MAX_RUN_LENGTH; ++k <= right && a[k - 1] == a[k]; ) {
//                    if (--m == 0) {
//                        sort(a, left, right, true);
//                        return;
//                    }
//                }
//            }
//
//            /*
//             * The array is not highly structured,
//             * use Quicksort instead of merge sort.
//             */
//            if (++count == MAX_RUN_COUNT) {
//                sort(a, left, right, true);
//                return;
//            }
//        }
//
//        // Check special cases
//        // Implementation note: variable "right" is increased by 1.
//        if (run[count] == right++) { // The last run contains one element
//            run[++count] = right;
//        } else if (count == 1) { // The array is already sorted
//            return;
//        }
//
//        // Determine alternation base for merge
//        byte odd = 0;
//        for (int n = 1; (n <<= 1) < count; odd ^= 1);
//
//        // Use or create temporary array b for merging
//        char[] b;                 // temp array; alternates with a
//        int ao, bo;              // array offsets from 'left'
//        final int blen = right - left; // space needed for b
//        if (work == null || workLen < blen || workBase + blen > work.length) {
//            work = new char[blen];
//            workBase = 0;
//        }
//        if (odd == 0) {
//            System.arraycopy(a, left, work, workBase, blen);
//            b = a;
//            bo = 0;
//            a = work;
//            ao = workBase - left;
//        } else {
//            b = work;
//            ao = 0;
//            bo = workBase - left;
//        }
//
//        // Merging
//        for (int last; count > 1; count = last) {
//            for (int k = (last = 0) + 2; k <= count; k += 2) {
//                final int hi = run[k];
//                final int mi = run[k - 1];
//                for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
//                    if (q >= hi || p < mi && a[p + ao] <= a[q + ao]) {
//                        b[i + bo] = a[p++ + ao];
//                    } else {
//                        b[i + bo] = a[q++ + ao];
//                    }
//                }
//                run[++last] = hi;
//            }
//            if ((count & 1) != 0) {
//                for (int i = right, lo = run[count - 1]; --i >= lo;
//                     b[i + bo] = a[i + ao]
//                );
//                run[++last] = right;
//            }
//            final char[] t = a; a = b; b = t;
//            final int o = ao; ao = bo; bo = o;
//        }
//    }
//
//    /**
//     * Sorts the specified range of the array by Dual-Pivot Quicksort.
//     *
//     * @param a the array to be sorted
//     * @param left the index of the first element, inclusive, to be sorted
//     * @param right the index of the last element, inclusive, to be sorted
//     * @param leftmost indicates if this part is the leftmost in the range
//     */
//    private static void sort(final char[] a, int left, int right, final boolean leftmost) {
//        final int length = right - left + 1;
//
//        // Use insertion sort on tiny arrays
//        if (length < INSERTION_SORT_THRESHOLD) {
//            if (leftmost) {
//                /*
//                 * Traditional (without sentinel) insertion sort,
//                 * optimized for server VM, is used in case of
//                 * the leftmost part.
//                 */
//                for (int i = left, j = i; i < right; j = ++i) {
//                    final char ai = a[i + 1];
//                    while (ai < a[j]) {
//                        a[j + 1] = a[j];
//                        if (j-- == left) {
//                            break;
//                        }
//                    }
//                    a[j + 1] = ai;
//                }
//            } else {
//                /*
//                 * Skip the longest ascending sequence.
//                 */
//                do {
//                    if (left >= right) {
//                        return;
//                    }
//                } while (a[++left] >= a[left - 1]);
//
//                /*
//                 * Every element from adjoining part plays the role
//                 * of sentinel, therefore this allows us to avoid the
//                 * left range check on each iteration. Moreover, we use
//                 * the more optimized algorithm, so called pair insertion
//                 * sort, which is faster (in the context of Quicksort)
//                 * than traditional implementation of insertion sort.
//                 */
//                for (int k = left; ++left <= right; k = ++left) {
//                    char a1 = a[k], a2 = a[left];
//
//                    if (a1 < a2) {
//                        a2 = a1; a1 = a[left];
//                    }
//                    while (a1 < a[--k]) {
//                        a[k + 2] = a[k];
//                    }
//                    a[++k + 1] = a1;
//
//                    while (a2 < a[--k]) {
//                        a[k + 1] = a[k];
//                    }
//                    a[k + 1] = a2;
//                }
//                final char last = a[right];
//
//                while (last < a[--right]) {
//                    a[right + 1] = a[right];
//                }
//                a[right + 1] = last;
//            }
//            return;
//        }
//
//        // Inexpensive approximation of length / 7
//        final int seventh = (length >> 3) + (length >> 6) + 1;
//
//        /*
//         * Sort five evenly spaced elements around (and including) the
//         * center element in the range. These elements will be used for
//         * pivot selection as described below. The choice for spacing
//         * these elements was empirically determined to work well on
//         * a wide variety of inputs.
//         */
//        final int e3 = (left + right) >>> 1; // The midpoint
//        final int e2 = e3 - seventh;
//        final int e1 = e2 - seventh;
//        final int e4 = e3 + seventh;
//        final int e5 = e4 + seventh;
//
//        // Sort these elements using insertion sort
//        if (a[e2] < a[e1]) { final char t = a[e2]; a[e2] = a[e1]; a[e1] = t; }
//
//        if (a[e3] < a[e2]) { final char t = a[e3]; a[e3] = a[e2]; a[e2] = t;
//            if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
//        }
//        if (a[e4] < a[e3]) { final char t = a[e4]; a[e4] = a[e3]; a[e3] = t;
//            if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
//                if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
//            }
//        }
//        if (a[e5] < a[e4]) { final char t = a[e5]; a[e5] = a[e4]; a[e4] = t;
//            if (t < a[e3]) { a[e4] = a[e3]; a[e3] = t;
//                if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
//                    if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
//                }
//            }
//        }
//
//        // Pointers
//        int less  = left;  // The index of the first element of center part
//        int great = right; // The index before the first element of right part
//
//        if (a[e1] != a[e2] && a[e2] != a[e3] && a[e3] != a[e4] && a[e4] != a[e5]) {
//            /*
//             * Use the second and fourth of the five sorted elements as pivots.
//             * These values are inexpensive approximations of the first and
//             * second terciles of the array. Note that pivot1 <= pivot2.
//             */
//            final char pivot1 = a[e2];
//            final char pivot2 = a[e4];
//
//            /*
//             * The first and the last elements to be sorted are moved to the
//             * locations formerly occupied by the pivots. When partitioning
//             * is complete, the pivots are swapped back into their final
//             * positions, and excluded from subsequent sorting.
//             */
//            a[e2] = a[left];
//            a[e4] = a[right];
//
//            /*
//             * Skip elements, which are less or greater than pivot values.
//             */
//            while (a[++less] < pivot1);
//            while (a[--great] > pivot2);
//
//            /*
//             * Partitioning:
//             *
//             *   left part           center part                   right part
//             * +--------------------------------------------------------------+
//             * |  < pivot1  |  pivot1 <= && <= pivot2  |    ?    |  > pivot2  |
//             * +--------------------------------------------------------------+
//             *               ^                          ^       ^
//             *               |                          |       |
//             *              less                        k     great
//             *
//             * Invariants:
//             *
//             *              all in (left, less)   < pivot1
//             *    pivot1 <= all in [less, k)     <= pivot2
//             *              all in (great, right) > pivot2
//             *
//             * Pointer k is the first index of ?-part.
//             */
//            outer:
//            for (int k = less - 1; ++k <= great; ) {
//                final char ak = a[k];
//                if (ak < pivot1) { // Move a[k] to left part
//                    a[k] = a[less];
//                    /*
//                     * Here and below we use "a[i] = b; i++;" instead
//                     * of "a[i++] = b;" due to performance issue.
//                     */
//                    a[less] = ak;
//                    ++less;
//                } else if (ak > pivot2) { // Move a[k] to right part
//                    while (a[great] > pivot2) {
//                        if (great-- == k) {
//                            break outer;
//                        }
//                    }
//                    if (a[great] < pivot1) { // a[great] <= pivot2
//                        a[k] = a[less];
//                        a[less] = a[great];
//                        ++less;
//                    } else { // pivot1 <= a[great] <= pivot2
//                        a[k] = a[great];
//                    }
//                    /*
//                     * Here and below we use "a[i] = b; i--;" instead
//                     * of "a[i--] = b;" due to performance issue.
//                     */
//                    a[great] = ak;
//                    --great;
//                }
//            }
//
//            // Swap pivots into their final positions
//            a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
//            a[right] = a[great + 1]; a[great + 1] = pivot2;
//
//            // Sort left and right parts recursively, excluding known pivots
//            sort(a, left, less - 2, leftmost);
//            sort(a, great + 2, right, false);
//
//            /*
//             * If center part is too large (comprises > 4/7 of the array),
//             * swap internal pivot values to ends.
//             */
//            if (less < e1 && e5 < great) {
//                /*
//                 * Skip elements, which are equal to pivot values.
//                 */
//                while (a[less] == pivot1) {
//                    ++less;
//                }
//
//                while (a[great] == pivot2) {
//                    --great;
//                }
//
//                /*
//                 * Partitioning:
//                 *
//                 *   left part         center part                  right part
//                 * +----------------------------------------------------------+
//                 * | == pivot1 |  pivot1 < && < pivot2  |    ?    | == pivot2 |
//                 * +----------------------------------------------------------+
//                 *              ^                        ^       ^
//                 *              |                        |       |
//                 *             less                      k     great
//                 *
//                 * Invariants:
//                 *
//                 *              all in (*,  less) == pivot1
//                 *     pivot1 < all in [less,  k)  < pivot2
//                 *              all in (great, *) == pivot2
//                 *
//                 * Pointer k is the first index of ?-part.
//                 */
//                outer:
//                for (int k = less - 1; ++k <= great; ) {
//                    final char ak = a[k];
//                    if (ak == pivot1) { // Move a[k] to left part
//                        a[k] = a[less];
//                        a[less] = ak;
//                        ++less;
//                    } else if (ak == pivot2) { // Move a[k] to right part
//                        while (a[great] == pivot2) {
//                            if (great-- == k) {
//                                break outer;
//                            }
//                        }
//                        if (a[great] == pivot1) { // a[great] < pivot2
//                            a[k] = a[less];
//                            /*
//                             * Even though a[great] equals to pivot1, the
//                             * assignment a[less] = pivot1 may be incorrect,
//                             * if a[great] and pivot1 are floating-point zeros
//                             * of different signs. Therefore in float and
//                             * double sorting methods we have to use more
//                             * accurate assignment a[less] = a[great].
//                             */
//                            a[less] = pivot1;
//                            ++less;
//                        } else { // pivot1 < a[great] < pivot2
//                            a[k] = a[great];
//                        }
//                        a[great] = ak;
//                        --great;
//                    }
//                }
//            }
//
//            // Sort center part recursively
//            sort(a, less, great, false);
//
//        } else { // Partitioning with one pivot
//            /*
//             * Use the third of the five sorted elements as pivot.
//             * This value is inexpensive approximation of the median.
//             */
//            final char pivot = a[e3];
//
//            /*
//             * Partitioning degenerates to the traditional 3-way
//             * (or "Dutch National Flag") schema:
//             *
//             *   left part    center part              right part
//             * +-------------------------------------------------+
//             * |  < pivot  |   == pivot   |     ?    |  > pivot  |
//             * +-------------------------------------------------+
//             *              ^              ^        ^
//             *              |              |        |
//             *             less            k      great
//             *
//             * Invariants:
//             *
//             *   all in (left, less)   < pivot
//             *   all in [less, k)     == pivot
//             *   all in (great, right) > pivot
//             *
//             * Pointer k is the first index of ?-part.
//             */
//            for (int k = less; k <= great; ++k) {
//                if (a[k] == pivot) {
//                    continue;
//                }
//                final char ak = a[k];
//                if (ak < pivot) { // Move a[k] to left part
//                    a[k] = a[less];
//                    a[less] = ak;
//                    ++less;
//                } else { // a[k] > pivot - Move a[k] to right part
//                    while (a[great] > pivot) {
//                        --great;
//                    }
//                    if (a[great] < pivot) { // a[great] <= pivot
//                        a[k] = a[less];
//                        a[less] = a[great];
//                        ++less;
//                    } else { // a[great] == pivot
//                        /*
//                         * Even though a[great] equals to pivot, the
//                         * assignment a[k] = pivot may be incorrect,
//                         * if a[great] and pivot are floating-point
//                         * zeros of different signs. Therefore in float
//                         * and double sorting methods we have to use
//                         * more accurate assignment a[k] = a[great].
//                         */
//                        a[k] = pivot;
//                    }
//                    a[great] = ak;
//                    --great;
//                }
//            }
//
//            /*
//             * Sort left and right parts recursively.
//             * All elements from center part are equal
//             * and, therefore, already sorted.
//             */
//            sort(a, left, less - 1, leftmost);
//            sort(a, great + 1, right, false);
//        }
//    }
//
//    /** The number of distinct byte values. */
//    private static final int NUM_BYTE_VALUES = 1 << 8;
//
//    /**
//     * Sorts the specified range of the array.
//     *
//     * @param a the array to be sorted
//     * @param left the index of the first element, inclusive, to be sorted
//     * @param right the index of the last element, inclusive, to be sorted
//     */
//    static void sort(final byte[] a, final int left, final int right) {
//        // Use counting sort on large arrays
//        if (right - left > COUNTING_SORT_THRESHOLD_FOR_BYTE) {
//            final int[] count = new int[NUM_BYTE_VALUES];
//
//            for (int i = left - 1; ++i <= right;
//                 count[a[i] - Byte.MIN_VALUE]++
//            );
//            for (int i = NUM_BYTE_VALUES, k = right + 1; k > left; ) {
//                while (count[--i] == 0);
//                final byte value = (byte) (i + Byte.MIN_VALUE);
//                int s = count[i];
//
//                do {
//                    a[--k] = value;
//                } while (--s > 0);
//            }
//        } else { // Use insertion sort on small arrays
//            for (int i = left, j = i; i < right; j = ++i) {
//                final byte ai = a[i + 1];
//                while (ai < a[j]) {
//                    a[j + 1] = a[j];
//                    if (j-- == left) {
//                        break;
//                    }
//                }
//                a[j + 1] = ai;
//            }
//        }
//    }
//
//    /**
//     * Sorts the specified range of the array using the given
//     * workspace array slice if possible for merging
//     *
//     * @param a the array to be sorted
//     * @param left the index of the first element, inclusive, to be sorted
//     * @param right the index of the last element, inclusive, to be sorted
//     * @param work a workspace array (slice)
//     * @param workBase origin of usable space in work array
//     * @param workLen usable size of work array
//     */
//    static void sort(final float[] a, int left, int right,
//                     final float[] work, final int workBase, final int workLen) {
//        /*
//         * Phase 1: Move NaNs to the end of the array.
//         */
//        while (left <= right && Float.isNaN(a[right])) {
//            --right;
//        }
//        for (int k = right; --k >= left; ) {
//            final float ak = a[k];
//            if (ak != ak) { // a[k] is NaN
//                a[k] = a[right];
//                a[right] = ak;
//                --right;
//            }
//        }
//
//        /*
//         * Phase 2: Sort everything except NaNs (which are already in place).
//         */
//        doSort(a, left, right, work, workBase, workLen);
//
//        /*
//         * Phase 3: Place negative zeros before positive zeros.
//         */
//        int hi = right;
//
//        /*
//         * Find the first zero, or first positive, or last negative element.
//         */
//        while (left < hi) {
//            final int middle = (left + hi) >>> 1;
//            final float middleValue = a[middle];
//
//            if (middleValue < 0.0f) {
//                left = middle + 1;
//            } else {
//                hi = middle;
//            }
//        }
//
//        /*
//         * Skip the last negative value (if any) or all leading negative zeros.
//         */
//        while (left <= right && Float.floatToRawIntBits(a[left]) < 0) {
//            ++left;
//        }
//
//        /*
//         * Move negative zeros to the beginning of the sub-range.
//         *
//         * Partitioning:
//         *
//         * +----------------------------------------------------+
//         * |   < 0.0   |   -0.0   |   0.0   |   ?  ( >= 0.0 )   |
//         * +----------------------------------------------------+
//         *              ^          ^         ^
//         *              |          |         |
//         *             left        p         k
//         *
//         * Invariants:
//         *
//         *   all in (*,  left)  <  0.0
//         *   all in [left,  p) == -0.0
//         *   all in [p,     k) ==  0.0
//         *   all in [k, right] >=  0.0
//         *
//         * Pointer k is the first index of ?-part.
//         */
//        for (int k = left, p = left - 1; ++k <= right; ) {
//            final float ak = a[k];
//            if (ak != 0.0f) {
//                break;
//            }
//            if (Float.floatToRawIntBits(ak) < 0) { // ak is -0.0f
//                a[k] = 0.0f;
//                a[++p] = -0.0f;
//            }
//        }
//    }
//
//    /**
//     * Sorts the specified range of the array.
//     *
//     * @param a the array to be sorted
//     * @param left the index of the first element, inclusive, to be sorted
//     * @param right the index of the last element, inclusive, to be sorted
//     * @param work a workspace array (slice)
//     * @param workBase origin of usable space in work array
//     * @param workLen usable size of work array
//     */
//    private static void doSort(float[] a, final int left, int right,
//                               float[] work, int workBase, final int workLen) {
//        // Use Quicksort on small arrays
//        if (right - left < QUICKSORT_THRESHOLD) {
//            sort(a, left, right, true);
//            return;
//        }
//
//        /*
//         * Index run[i] is the start of i-th run
//         * (ascending or descending sequence).
//         */
//        final int[] run = new int[MAX_RUN_COUNT + 1];
//        int count = 0; run[0] = left;
//
//        // Check if the array is nearly sorted
//        for (int k = left; k < right; run[count] = k) {
//            if (a[k] < a[k + 1]) { // ascending
//                while (++k <= right && a[k - 1] <= a[k]);
//            } else if (a[k] > a[k + 1]) { // descending
//                while (++k <= right && a[k - 1] >= a[k]);
//                for (int lo = run[count] - 1, hi = k; ++lo < --hi; ) {
//                    final float t = a[lo]; a[lo] = a[hi]; a[hi] = t;
//                }
//            } else { // equal
//                for (int m = MAX_RUN_LENGTH; ++k <= right && a[k - 1] == a[k]; ) {
//                    if (--m == 0) {
//                        sort(a, left, right, true);
//                        return;
//                    }
//                }
//            }
//
//            /*
//             * The array is not highly structured,
//             * use Quicksort instead of merge sort.
//             */
//            if (++count == MAX_RUN_COUNT) {
//                sort(a, left, right, true);
//                return;
//            }
//        }
//
//        // Check special cases
//        // Implementation note: variable "right" is increased by 1.
//        if (run[count] == right++) { // The last run contains one element
//            run[++count] = right;
//        } else if (count == 1) { // The array is already sorted
//            return;
//        }
//
//        // Determine alternation base for merge
//        byte odd = 0;
//        for (int n = 1; (n <<= 1) < count; odd ^= 1);
//
//        // Use or create temporary array b for merging
//        float[] b;                 // temp array; alternates with a
//        int ao, bo;              // array offsets from 'left'
//        final int blen = right - left; // space needed for b
//        if (work == null || workLen < blen || workBase + blen > work.length) {
//            work = new float[blen];
//            workBase = 0;
//        }
//        if (odd == 0) {
//            System.arraycopy(a, left, work, workBase, blen);
//            b = a;
//            bo = 0;
//            a = work;
//            ao = workBase - left;
//        } else {
//            b = work;
//            ao = 0;
//            bo = workBase - left;
//        }
//
//        // Merging
//        for (int last; count > 1; count = last) {
//            for (int k = (last = 0) + 2; k <= count; k += 2) {
//                final int hi = run[k];
//                final int mi = run[k - 1];
//                for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
//                    if (q >= hi || p < mi && a[p + ao] <= a[q + ao]) {
//                        b[i + bo] = a[p++ + ao];
//                    } else {
//                        b[i + bo] = a[q++ + ao];
//                    }
//                }
//                run[++last] = hi;
//            }
//            if ((count & 1) != 0) {
//                for (int i = right, lo = run[count - 1]; --i >= lo;
//                     b[i + bo] = a[i + ao]
//                );
//                run[++last] = right;
//            }
//            final float[] t = a; a = b; b = t;
//            final int o = ao; ao = bo; bo = o;
//        }
//    }
//
//    /**
//     * Sorts the specified range of the array by Dual-Pivot Quicksort.
//     *
//     * @param a the array to be sorted
//     * @param left the index of the first element, inclusive, to be sorted
//     * @param right the index of the last element, inclusive, to be sorted
//     * @param leftmost indicates if this part is the leftmost in the range
//     */
//    private static void sort(final float[] a, int left, int right, final boolean leftmost) {
//        final int length = right - left + 1;
//
//        // Use insertion sort on tiny arrays
//        if (length < INSERTION_SORT_THRESHOLD) {
//            if (leftmost) {
//                /*
//                 * Traditional (without sentinel) insertion sort,
//                 * optimized for server VM, is used in case of
//                 * the leftmost part.
//                 */
//                for (int i = left, j = i; i < right; j = ++i) {
//                    final float ai = a[i + 1];
//                    while (ai < a[j]) {
//                        a[j + 1] = a[j];
//                        if (j-- == left) {
//                            break;
//                        }
//                    }
//                    a[j + 1] = ai;
//                }
//            } else {
//                /*
//                 * Skip the longest ascending sequence.
//                 */
//                do {
//                    if (left >= right) {
//                        return;
//                    }
//                } while (a[++left] >= a[left - 1]);
//
//                /*
//                 * Every element from adjoining part plays the role
//                 * of sentinel, therefore this allows us to avoid the
//                 * left range check on each iteration. Moreover, we use
//                 * the more optimized algorithm, so called pair insertion
//                 * sort, which is faster (in the context of Quicksort)
//                 * than traditional implementation of insertion sort.
//                 */
//                for (int k = left; ++left <= right; k = ++left) {
//                    float a1 = a[k], a2 = a[left];
//
//                    if (a1 < a2) {
//                        a2 = a1; a1 = a[left];
//                    }
//                    while (a1 < a[--k]) {
//                        a[k + 2] = a[k];
//                    }
//                    a[++k + 1] = a1;
//
//                    while (a2 < a[--k]) {
//                        a[k + 1] = a[k];
//                    }
//                    a[k + 1] = a2;
//                }
//                final float last = a[right];
//
//                while (last < a[--right]) {
//                    a[right + 1] = a[right];
//                }
//                a[right + 1] = last;
//            }
//            return;
//        }
//
//        // Inexpensive approximation of length / 7
//        final int seventh = (length >> 3) + (length >> 6) + 1;
//
//        /*
//         * Sort five evenly spaced elements around (and including) the
//         * center element in the range. These elements will be used for
//         * pivot selection as described below. The choice for spacing
//         * these elements was empirically determined to work well on
//         * a wide variety of inputs.
//         */
//        final int e3 = (left + right) >>> 1; // The midpoint
//        final int e2 = e3 - seventh;
//        final int e1 = e2 - seventh;
//        final int e4 = e3 + seventh;
//        final int e5 = e4 + seventh;
//
//        // Sort these elements using insertion sort
//        if (a[e2] < a[e1]) { final float t = a[e2]; a[e2] = a[e1]; a[e1] = t; }
//
//        if (a[e3] < a[e2]) { final float t = a[e3]; a[e3] = a[e2]; a[e2] = t;
//            if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
//        }
//        if (a[e4] < a[e3]) { final float t = a[e4]; a[e4] = a[e3]; a[e3] = t;
//            if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
//                if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
//            }
//        }
//        if (a[e5] < a[e4]) { final float t = a[e5]; a[e5] = a[e4]; a[e4] = t;
//            if (t < a[e3]) { a[e4] = a[e3]; a[e3] = t;
//                if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
//                    if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
//                }
//            }
//        }
//
//        // Pointers
//        int less  = left;  // The index of the first element of center part
//        int great = right; // The index before the first element of right part
//
//        if (a[e1] != a[e2] && a[e2] != a[e3] && a[e3] != a[e4] && a[e4] != a[e5]) {
//            /*
//             * Use the second and fourth of the five sorted elements as pivots.
//             * These values are inexpensive approximations of the first and
//             * second terciles of the array. Note that pivot1 <= pivot2.
//             */
//            final float pivot1 = a[e2];
//            final float pivot2 = a[e4];
//
//            /*
//             * The first and the last elements to be sorted are moved to the
//             * locations formerly occupied by the pivots. When partitioning
//             * is complete, the pivots are swapped back into their final
//             * positions, and excluded from subsequent sorting.
//             */
//            a[e2] = a[left];
//            a[e4] = a[right];
//
//            /*
//             * Skip elements, which are less or greater than pivot values.
//             */
//            while (a[++less] < pivot1);
//            while (a[--great] > pivot2);
//
//            /*
//             * Partitioning:
//             *
//             *   left part           center part                   right part
//             * +--------------------------------------------------------------+
//             * |  < pivot1  |  pivot1 <= && <= pivot2  |    ?    |  > pivot2  |
//             * +--------------------------------------------------------------+
//             *               ^                          ^       ^
//             *               |                          |       |
//             *              less                        k     great
//             *
//             * Invariants:
//             *
//             *              all in (left, less)   < pivot1
//             *    pivot1 <= all in [less, k)     <= pivot2
//             *              all in (great, right) > pivot2
//             *
//             * Pointer k is the first index of ?-part.
//             */
//            outer:
//            for (int k = less - 1; ++k <= great; ) {
//                final float ak = a[k];
//                if (ak < pivot1) { // Move a[k] to left part
//                    a[k] = a[less];
//                    /*
//                     * Here and below we use "a[i] = b; i++;" instead
//                     * of "a[i++] = b;" due to performance issue.
//                     */
//                    a[less] = ak;
//                    ++less;
//                } else if (ak > pivot2) { // Move a[k] to right part
//                    while (a[great] > pivot2) {
//                        if (great-- == k) {
//                            break outer;
//                        }
//                    }
//                    if (a[great] < pivot1) { // a[great] <= pivot2
//                        a[k] = a[less];
//                        a[less] = a[great];
//                        ++less;
//                    } else { // pivot1 <= a[great] <= pivot2
//                        a[k] = a[great];
//                    }
//                    /*
//                     * Here and below we use "a[i] = b; i--;" instead
//                     * of "a[i--] = b;" due to performance issue.
//                     */
//                    a[great] = ak;
//                    --great;
//                }
//            }
//
//            // Swap pivots into their final positions
//            a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
//            a[right] = a[great + 1]; a[great + 1] = pivot2;
//
//            // Sort left and right parts recursively, excluding known pivots
//            sort(a, left, less - 2, leftmost);
//            sort(a, great + 2, right, false);
//
//            /*
//             * If center part is too large (comprises > 4/7 of the array),
//             * swap internal pivot values to ends.
//             */
//            if (less < e1 && e5 < great) {
//                /*
//                 * Skip elements, which are equal to pivot values.
//                 */
//                while (a[less] == pivot1) {
//                    ++less;
//                }
//
//                while (a[great] == pivot2) {
//                    --great;
//                }
//
//                /*
//                 * Partitioning:
//                 *
//                 *   left part         center part                  right part
//                 * +----------------------------------------------------------+
//                 * | == pivot1 |  pivot1 < && < pivot2  |    ?    | == pivot2 |
//                 * +----------------------------------------------------------+
//                 *              ^                        ^       ^
//                 *              |                        |       |
//                 *             less                      k     great
//                 *
//                 * Invariants:
//                 *
//                 *              all in (*,  less) == pivot1
//                 *     pivot1 < all in [less,  k)  < pivot2
//                 *              all in (great, *) == pivot2
//                 *
//                 * Pointer k is the first index of ?-part.
//                 */
//                outer:
//                for (int k = less - 1; ++k <= great; ) {
//                    final float ak = a[k];
//                    if (ak == pivot1) { // Move a[k] to left part
//                        a[k] = a[less];
//                        a[less] = ak;
//                        ++less;
//                    } else if (ak == pivot2) { // Move a[k] to right part
//                        while (a[great] == pivot2) {
//                            if (great-- == k) {
//                                break outer;
//                            }
//                        }
//                        if (a[great] == pivot1) { // a[great] < pivot2
//                            a[k] = a[less];
//                            /*
//                             * Even though a[great] equals to pivot1, the
//                             * assignment a[less] = pivot1 may be incorrect,
//                             * if a[great] and pivot1 are floating-point zeros
//                             * of different signs. Therefore in float and
//                             * double sorting methods we have to use more
//                             * accurate assignment a[less] = a[great].
//                             */
//                            a[less] = a[great];
//                            ++less;
//                        } else { // pivot1 < a[great] < pivot2
//                            a[k] = a[great];
//                        }
//                        a[great] = ak;
//                        --great;
//                    }
//                }
//            }
//
//            // Sort center part recursively
//            sort(a, less, great, false);
//
//        } else { // Partitioning with one pivot
//            /*
//             * Use the third of the five sorted elements as pivot.
//             * This value is inexpensive approximation of the median.
//             */
//            final float pivot = a[e3];
//
//            /*
//             * Partitioning degenerates to the traditional 3-way
//             * (or "Dutch National Flag") schema:
//             *
//             *   left part    center part              right part
//             * +-------------------------------------------------+
//             * |  < pivot  |   == pivot   |     ?    |  > pivot  |
//             * +-------------------------------------------------+
//             *              ^              ^        ^
//             *              |              |        |
//             *             less            k      great
//             *
//             * Invariants:
//             *
//             *   all in (left, less)   < pivot
//             *   all in [less, k)     == pivot
//             *   all in (great, right) > pivot
//             *
//             * Pointer k is the first index of ?-part.
//             */
//            for (int k = less; k <= great; ++k) {
//                if (a[k] == pivot) {
//                    continue;
//                }
//                final float ak = a[k];
//                if (ak < pivot) { // Move a[k] to left part
//                    a[k] = a[less];
//                    a[less] = ak;
//                    ++less;
//                } else { // a[k] > pivot - Move a[k] to right part
//                    while (a[great] > pivot) {
//                        --great;
//                    }
//                    if (a[great] < pivot) { // a[great] <= pivot
//                        a[k] = a[less];
//                        a[less] = a[great];
//                        ++less;
//                    } else { // a[great] == pivot
//                        /*
//                         * Even though a[great] equals to pivot, the
//                         * assignment a[k] = pivot may be incorrect,
//                         * if a[great] and pivot are floating-point
//                         * zeros of different signs. Therefore in float
//                         * and double sorting methods we have to use
//                         * more accurate assignment a[k] = a[great].
//                         */
//                        a[k] = a[great];
//                    }
//                    a[great] = ak;
//                    --great;
//                }
//            }
//
//            /*
//             * Sort left and right parts recursively.
//             * All elements from center part are equal
//             * and, therefore, already sorted.
//             */
//            sort(a, left, less - 1, leftmost);
//            sort(a, great + 1, right, false);
//        }
//    }
//
//    /**
//     * Sorts the specified range of the array using the given
//     * workspace array slice if possible for merging
//     *
//     * @param a the array to be sorted
//     * @param left the index of the first element, inclusive, to be sorted
//     * @param right the index of the last element, inclusive, to be sorted
//     * @param work a workspace array (slice)
//     * @param workBase origin of usable space in work array
//     * @param workLen usable size of work array
//     */
//    static <X extends Comparable<X>> void sort(final X[] a, int left, int right,
//                     final X[] work, final int workBase, final int workLen) {
//        /*
//         * Phase 1: Move NaNs to the end of the array.
//         */
//        while (left <= right && X.isNaN(a[right])) {
//            --right;
//        }
//        for (int k = right; --k >= left; ) {
//            final X ak = a[k];
//            if (ak != ak) { // a[k] is NaN
//                a[k] = a[right];
//                a[right] = ak;
//                --right;
//            }
//        }
//
//        /*
//         * Phase 2: Sort everything except NaNs (which are already in place).
//         */
//        doSort(a, left, right, work, workBase, workLen);
//
//        /*
//         * Phase 3: Place negative zeros before positive zeros.
//         */
//        int hi = right;
//
//        /*
//         * Find the first zero, or first positive, or last negative element.
//         */
//        while (left < hi) {
//            final int middle = (left + hi) >>> 1;
//            final X middleValue = a[middle];
//
//            if (middleValue < 0.0d) {
//                left = middle + 1;
//            } else {
//                hi = middle;
//            }
//        }
//
//        /*
//         * Skip the last negative value (if any) or all leading negative zeros.
//         */
//        while (left <= right && X.doubleToRawLongBits(a[left]) < 0) {
//            ++left;
//        }
//
//        /*
//         * Move negative zeros to the beginning of the sub-range.
//         *
//         * Partitioning:
//         *
//         * +----------------------------------------------------+
//         * |   < 0.0   |   -0.0   |   0.0   |   ?  ( >= 0.0 )   |
//         * +----------------------------------------------------+
//         *              ^          ^         ^
//         *              |          |         |
//         *             left        p         k
//         *
//         * Invariants:
//         *
//         *   all in (*,  left)  <  0.0
//         *   all in [left,  p) == -0.0
//         *   all in [p,     k) ==  0.0
//         *   all in [k, right] >=  0.0
//         *
//         * Pointer k is the first index of ?-part.
//         */
//        for (int k = left, p = left - 1; ++k <= right; ) {
//            final double ak = a[k];
//            if (ak != 0.0d) {
//                break;
//            }
//            if (Double.doubleToRawLongBits(ak) < 0) { // ak is -0.0d
//                a[k] = 0.0d;
//                a[++p] = -0.0d;
//            }
//        }
//    }
//
//    /**
//     * Sorts the specified range of the array.
//     *
//     * @param a the array to be sorted
//     * @param left the index of the first element, inclusive, to be sorted
//     * @param right the index of the last element, inclusive, to be sorted
//     * @param work a workspace array (slice)
//     * @param workBase origin of usable space in work array
//     * @param workLen usable size of work array
//     */
//    private static void doSort(double[] a, final int left, int right,
//                               double[] work, int workBase, final int workLen) {
//        // Use Quicksort on small arrays
//        if (right - left < QUICKSORT_THRESHOLD) {
//            sort(a, left, right, true);
//            return;
//        }
//
//        /*
//         * Index run[i] is the start of i-th run
//         * (ascending or descending sequence).
//         */
//        final int[] run = new int[MAX_RUN_COUNT + 1];
//        int count = 0; run[0] = left;
//
//        // Check if the array is nearly sorted
//        for (int k = left; k < right; run[count] = k) {
//            if (a[k] < a[k + 1]) { // ascending
//                while (++k <= right && a[k - 1] <= a[k]);
//            } else if (a[k] > a[k + 1]) { // descending
//                while (++k <= right && a[k - 1] >= a[k]);
//                for (int lo = run[count] - 1, hi = k; ++lo < --hi; ) {
//                    final double t = a[lo]; a[lo] = a[hi]; a[hi] = t;
//                }
//            } else { // equal
//                for (int m = MAX_RUN_LENGTH; ++k <= right && a[k - 1] == a[k]; ) {
//                    if (--m == 0) {
//                        sort(a, left, right, true);
//                        return;
//                    }
//                }
//            }
//
//            /*
//             * The array is not highly structured,
//             * use Quicksort instead of merge sort.
//             */
//            if (++count == MAX_RUN_COUNT) {
//                sort(a, left, right, true);
//                return;
//            }
//        }
//
//        // Check special cases
//        // Implementation note: variable "right" is increased by 1.
//        if (run[count] == right++) { // The last run contains one element
//            run[++count] = right;
//        } else if (count == 1) { // The array is already sorted
//            return;
//        }
//
//        // Determine alternation base for merge
//        byte odd = 0;
//        for (int n = 1; (n <<= 1) < count; odd ^= 1);
//
//        // Use or create temporary array b for merging
//        double[] b;                 // temp array; alternates with a
//        int ao, bo;              // array offsets from 'left'
//        final int blen = right - left; // space needed for b
//        if (work == null || workLen < blen || workBase + blen > work.length) {
//            work = new double[blen];
//            workBase = 0;
//        }
//        if (odd == 0) {
//            System.arraycopy(a, left, work, workBase, blen);
//            b = a;
//            bo = 0;
//            a = work;
//            ao = workBase - left;
//        } else {
//            b = work;
//            ao = 0;
//            bo = workBase - left;
//        }
//
//        // Merging
//        for (int last; count > 1; count = last) {
//            for (int k = (last = 0) + 2; k <= count; k += 2) {
//                final int hi = run[k];
//                final int mi = run[k - 1];
//                for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
//                    if (q >= hi || p < mi && a[p + ao] <= a[q + ao]) {
//                        b[i + bo] = a[p++ + ao];
//                    } else {
//                        b[i + bo] = a[q++ + ao];
//                    }
//                }
//                run[++last] = hi;
//            }
//            if ((count & 1) != 0) {
//                for (int i = right, lo = run[count - 1]; --i >= lo;
//                     b[i + bo] = a[i + ao]
//                );
//                run[++last] = right;
//            }
//            final double[] t = a; a = b; b = t;
//            final int o = ao; ao = bo; bo = o;
//        }
//    }
//
//    /**
//     * Sorts the specified range of the array by Dual-Pivot Quicksort.
//     *
//     * @param a the array to be sorted
//     * @param left the index of the first element, inclusive, to be sorted
//     * @param right the index of the last element, inclusive, to be sorted
//     * @param leftmost indicates if this part is the leftmost in the range
//     */
//    private static void sort(final double[] a, int left, int right, final boolean leftmost) {
//        final int length = right - left + 1;
//
//        // Use insertion sort on tiny arrays
//        if (length < INSERTION_SORT_THRESHOLD) {
//            if (leftmost) {
//                /*
//                 * Traditional (without sentinel) insertion sort,
//                 * optimized for server VM, is used in case of
//                 * the leftmost part.
//                 */
//                for (int i = left, j = i; i < right; j = ++i) {
//                    final double ai = a[i + 1];
//                    while (ai < a[j]) {
//                        a[j + 1] = a[j];
//                        if (j-- == left) {
//                            break;
//                        }
//                    }
//                    a[j + 1] = ai;
//                }
//            } else {
//                /*
//                 * Skip the longest ascending sequence.
//                 */
//                do {
//                    if (left >= right) {
//                        return;
//                    }
//                } while (a[++left] >= a[left - 1]);
//
//                /*
//                 * Every element from adjoining part plays the role
//                 * of sentinel, therefore this allows us to avoid the
//                 * left range check on each iteration. Moreover, we use
//                 * the more optimized algorithm, so called pair insertion
//                 * sort, which is faster (in the context of Quicksort)
//                 * than traditional implementation of insertion sort.
//                 */
//                for (int k = left; ++left <= right; k = ++left) {
//                    double a1 = a[k], a2 = a[left];
//
//                    if (a1 < a2) {
//                        a2 = a1; a1 = a[left];
//                    }
//                    while (a1 < a[--k]) {
//                        a[k + 2] = a[k];
//                    }
//                    a[++k + 1] = a1;
//
//                    while (a2 < a[--k]) {
//                        a[k + 1] = a[k];
//                    }
//                    a[k + 1] = a2;
//                }
//                final double last = a[right];
//
//                while (last < a[--right]) {
//                    a[right + 1] = a[right];
//                }
//                a[right + 1] = last;
//            }
//            return;
//        }
//
//        // Inexpensive approximation of length / 7
//        final int seventh = (length >> 3) + (length >> 6) + 1;
//
//        /*
//         * Sort five evenly spaced elements around (and including) the
//         * center element in the range. These elements will be used for
//         * pivot selection as described below. The choice for spacing
//         * these elements was empirically determined to work well on
//         * a wide variety of inputs.
//         */
//        final int e3 = (left + right) >>> 1; // The midpoint
//        final int e2 = e3 - seventh;
//        final int e1 = e2 - seventh;
//        final int e4 = e3 + seventh;
//        final int e5 = e4 + seventh;
//
//        // Sort these elements using insertion sort
//        if (a[e2] < a[e1]) { final double t = a[e2]; a[e2] = a[e1]; a[e1] = t; }
//
//        if (a[e3] < a[e2]) { final double t = a[e3]; a[e3] = a[e2]; a[e2] = t;
//            if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
//        }
//        if (a[e4] < a[e3]) { final double t = a[e4]; a[e4] = a[e3]; a[e3] = t;
//            if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
//                if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
//            }
//        }
//        if (a[e5] < a[e4]) { final double t = a[e5]; a[e5] = a[e4]; a[e4] = t;
//            if (t < a[e3]) { a[e4] = a[e3]; a[e3] = t;
//                if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
//                    if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
//                }
//            }
//        }
//
//        // Pointers
//        int less  = left;  // The index of the first element of center part
//        int great = right; // The index before the first element of right part
//
//        if (a[e1] != a[e2] && a[e2] != a[e3] && a[e3] != a[e4] && a[e4] != a[e5]) {
//            /*
//             * Use the second and fourth of the five sorted elements as pivots.
//             * These values are inexpensive approximations of the first and
//             * second terciles of the array. Note that pivot1 <= pivot2.
//             */
//            final double pivot1 = a[e2];
//            final double pivot2 = a[e4];
//
//            /*
//             * The first and the last elements to be sorted are moved to the
//             * locations formerly occupied by the pivots. When partitioning
//             * is complete, the pivots are swapped back into their final
//             * positions, and excluded from subsequent sorting.
//             */
//            a[e2] = a[left];
//            a[e4] = a[right];
//
//            /*
//             * Skip elements, which are less or greater than pivot values.
//             */
//            while (a[++less] < pivot1);
//            while (a[--great] > pivot2);
//
//            /*
//             * Partitioning:
//             *
//             *   left part           center part                   right part
//             * +--------------------------------------------------------------+
//             * |  < pivot1  |  pivot1 <= && <= pivot2  |    ?    |  > pivot2  |
//             * +--------------------------------------------------------------+
//             *               ^                          ^       ^
//             *               |                          |       |
//             *              less                        k     great
//             *
//             * Invariants:
//             *
//             *              all in (left, less)   < pivot1
//             *    pivot1 <= all in [less, k)     <= pivot2
//             *              all in (great, right) > pivot2
//             *
//             * Pointer k is the first index of ?-part.
//             */
//            outer:
//            for (int k = less - 1; ++k <= great; ) {
//                final double ak = a[k];
//                if (ak < pivot1) { // Move a[k] to left part
//                    a[k] = a[less];
//                    /*
//                     * Here and below we use "a[i] = b; i++;" instead
//                     * of "a[i++] = b;" due to performance issue.
//                     */
//                    a[less] = ak;
//                    ++less;
//                } else if (ak > pivot2) { // Move a[k] to right part
//                    while (a[great] > pivot2) {
//                        if (great-- == k) {
//                            break outer;
//                        }
//                    }
//                    if (a[great] < pivot1) { // a[great] <= pivot2
//                        a[k] = a[less];
//                        a[less] = a[great];
//                        ++less;
//                    } else { // pivot1 <= a[great] <= pivot2
//                        a[k] = a[great];
//                    }
//                    /*
//                     * Here and below we use "a[i] = b; i--;" instead
//                     * of "a[i--] = b;" due to performance issue.
//                     */
//                    a[great] = ak;
//                    --great;
//                }
//            }
//
//            // Swap pivots into their final positions
//            a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
//            a[right] = a[great + 1]; a[great + 1] = pivot2;
//
//            // Sort left and right parts recursively, excluding known pivots
//            sort(a, left, less - 2, leftmost);
//            sort(a, great + 2, right, false);
//
//            /*
//             * If center part is too large (comprises > 4/7 of the array),
//             * swap internal pivot values to ends.
//             */
//            if (less < e1 && e5 < great) {
//                /*
//                 * Skip elements, which are equal to pivot values.
//                 */
//                while (a[less] == pivot1) {
//                    ++less;
//                }
//
//                while (a[great] == pivot2) {
//                    --great;
//                }
//
//                /*
//                 * Partitioning:
//                 *
//                 *   left part         center part                  right part
//                 * +----------------------------------------------------------+
//                 * | == pivot1 |  pivot1 < && < pivot2  |    ?    | == pivot2 |
//                 * +----------------------------------------------------------+
//                 *              ^                        ^       ^
//                 *              |                        |       |
//                 *             less                      k     great
//                 *
//                 * Invariants:
//                 *
//                 *              all in (*,  less) == pivot1
//                 *     pivot1 < all in [less,  k)  < pivot2
//                 *              all in (great, *) == pivot2
//                 *
//                 * Pointer k is the first index of ?-part.
//                 */
//                outer:
//                for (int k = less - 1; ++k <= great; ) {
//                    final double ak = a[k];
//                    if (ak == pivot1) { // Move a[k] to left part
//                        a[k] = a[less];
//                        a[less] = ak;
//                        ++less;
//                    } else if (ak == pivot2) { // Move a[k] to right part
//                        while (a[great] == pivot2) {
//                            if (great-- == k) {
//                                break outer;
//                            }
//                        }
//                        if (a[great] == pivot1) { // a[great] < pivot2
//                            a[k] = a[less];
//                            /*
//                             * Even though a[great] equals to pivot1, the
//                             * assignment a[less] = pivot1 may be incorrect,
//                             * if a[great] and pivot1 are floating-point zeros
//                             * of different signs. Therefore in float and
//                             * double sorting methods we have to use more
//                             * accurate assignment a[less] = a[great].
//                             */
//                            a[less] = a[great];
//                            ++less;
//                        } else { // pivot1 < a[great] < pivot2
//                            a[k] = a[great];
//                        }
//                        a[great] = ak;
//                        --great;
//                    }
//                }
//            }
//
//            // Sort center part recursively
//            sort(a, less, great, false);
//
//        } else { // Partitioning with one pivot
//            /*
//             * Use the third of the five sorted elements as pivot.
//             * This value is inexpensive approximation of the median.
//             */
//            final double pivot = a[e3];
//
//            /*
//             * Partitioning degenerates to the traditional 3-way
//             * (or "Dutch National Flag") schema:
//             *
//             *   left part    center part              right part
//             * +-------------------------------------------------+
//             * |  < pivot  |   == pivot   |     ?    |  > pivot  |
//             * +-------------------------------------------------+
//             *              ^              ^        ^
//             *              |              |        |
//             *             less            k      great
//             *
//             * Invariants:
//             *
//             *   all in (left, less)   < pivot
//             *   all in [less, k)     == pivot
//             *   all in (great, right) > pivot
//             *
//             * Pointer k is the first index of ?-part.
//             */
//            for (int k = less; k <= great; ++k) {
//                if (a[k] == pivot) {
//                    continue;
//                }
//                final double ak = a[k];
//                if (ak < pivot) { // Move a[k] to left part
//                    a[k] = a[less];
//                    a[less] = ak;
//                    ++less;
//                } else { // a[k] > pivot - Move a[k] to right part
//                    while (a[great] > pivot) {
//                        --great;
//                    }
//                    if (a[great] < pivot) { // a[great] <= pivot
//                        a[k] = a[less];
//                        a[less] = a[great];
//                        ++less;
//                    } else { // a[great] == pivot
//                        /*
//                         * Even though a[great] equals to pivot, the
//                         * assignment a[k] = pivot may be incorrect,
//                         * if a[great] and pivot are floating-point
//                         * zeros of different signs. Therefore in float
//                         * and double sorting methods we have to use
//                         * more accurate assignment a[k] = a[great].
//                         */
//                        a[k] = a[great];
//                    }
//                    a[great] = ak;
//                    --great;
//                }
//            }
//
//            /*
//             * Sort left and right parts recursively.
//             * All elements from center part are equal
//             * and, therefore, already sorted.
//             */
//            sort(a, left, less - 1, leftmost);
//            sort(a, great + 1, right, false);
//        }
//    }
}
