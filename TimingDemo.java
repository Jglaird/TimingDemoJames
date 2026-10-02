/* TimingDemo.java  --  Day 28 (Friday 2 October).  Teacher demo, run live.
 *
 *     javac TimingDemo.java
 *     java TimingDemo
 *
 * Tuesday's question: ArrayList and LinkedList both implement List, so the
 * SAME calls work on both.  Are they equally fast?
 *
 * Predict BEFORE you run it.  Four experiments:
 *   1. get(i) for every i          -- reading by position     (ArrayList vs LinkedList)
 *   2. add(0, x), n times          -- inserting at the front  (ArrayList vs LinkedList)
 *   3. 1,000 searches of a sorted array                       (linear vs binary)
 *   4. sorting n random ints                                  (selection vs merge)
 *
 * Your milliseconds will not match anyone else's.  The RATIOS are the point.
 * Experiments 3 and 4 also COUNT comparisons.  Counts are the same on every
 * computer, every time -- that is what Big-O really describes.
 */
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TimingDemo {

    // ---------------------------------------------------------------- 1 and 2
    static long timeGetAll(List<Integer> list) {
        long start = System.nanoTime();
        long sum = 0;
        for (int i = 0; i < list.size(); i++) {
            sum += list.get(i);          // same line of code for both lists
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        if (sum == 42) System.out.print("");   // stops the JIT skipping the loop
        return ms;
    }

    static long timeAddFront(List<Integer> list, int n) {
        long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            list.add(0, i);              // same line of code for both lists
        }
        return (System.nanoTime() - start) / 1_000_000;
    }

    // ---------------------------------------------------------------- 3: searching
    static long steps;                   // comparisons counted by the methods below
                                         // (kept in a local where possible, so counting
                                         //  does not slow down the loop being timed)

    /** O(n): look at every element in turn until we find it. */
    static int linearSearch(int[] a, int target) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == target) { steps += i + 1; return i; }
        }
        steps += a.length;
        return -1;
    }

    /** O(log n): the array is SORTED, so throw away half of it every step. */
    static int binarySearch(int[] a, int target) {
        int lo = 0, hi = a.length - 1;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            steps++;
            if (a[mid] == target) return mid;
            if (a[mid] < target) lo = mid + 1;
            else hi = mid - 1;
        }
        return -1;
    }

    // ---------------------------------------------------------------- 4: sorting
    /** O(n^2): find the smallest of what is left, swap it to the front, repeat. */
    static void selectionSort(int[] a) {
        long count = 0;
        for (int i = 0; i < a.length - 1; i++) {
            int min = i;
            for (int j = i + 1; j < a.length; j++) {
                count++;
                if (a[j] < a[min]) min = j;
            }
            int t = a[i]; a[i] = a[min]; a[min] = t;
        }
        steps += count;
    }

    /** O(n log n): split in half, sort each half, merge the two sorted halves. */
    static void mergeSort(int[] a) {
        mergeSort(a, new int[a.length], 0, a.length - 1);
    }

    static void mergeSort(int[] a, int[] tmp, int lo, int hi) {
        if (lo >= hi) return;
        int mid = (lo + hi) / 2;
        mergeSort(a, tmp, lo, mid);
        mergeSort(a, tmp, mid + 1, hi);
        int i = lo, j = mid + 1, k = lo;
        while (i <= mid && j <= hi) {
            steps++;
            tmp[k++] = (a[i] <= a[j]) ? a[i++] : a[j++];
        }
        while (i <= mid) tmp[k++] = a[i++];
        while (j <= hi)  tmp[k++] = a[j++];
        for (k = lo; k <= hi; k++) a[k] = tmp[k];
    }

    static int[] randomArray(int n, long seed) {
        Random r = new Random(seed);
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = r.nextInt(1_000_000);
        return a;
    }

    public static void main(String[] args) {
        int[] sizes = {10_000, 20_000, 40_000};

        System.out.println("Experiment 1: get(i) for every i");
        System.out.printf("%10s %14s %14s%n", "n", "ArrayList ms", "LinkedList ms");
        for (int n : sizes) {
            List<Integer> a = new ArrayList<>();
            List<Integer> b = new LinkedList<>();
            for (int i = 0; i < n; i++) { a.add(i); b.add(i); }
            System.out.printf("%10d %14d %14d%n", n, timeGetAll(a), timeGetAll(b));
        }

        System.out.println();
        System.out.println("Experiment 2: add(0, x), n times");
        System.out.printf("%10s %14s %14s%n", "n", "ArrayList ms", "LinkedList ms");
        for (int n : sizes) {
            System.out.printf("%10d %14d %14d%n", n,
                    timeAddFront(new ArrayList<>(), n),
                    timeAddFront(new LinkedList<>(), n));
        }

        // ------------------------------------------------ Experiment 3
        System.out.println();
        System.out.println("Experiment 3: 1,000 searches of a SORTED array");
        System.out.printf("%10s %12s %12s %16s %16s%n",
                "n", "linear ms", "binary ms", "linear steps", "binary steps");
        int searches = 1_000;
        int[] bigSizes = {250_000, 500_000, 1_000_000};
        for (int k = 0; k < 2_000; k++) {                     // warm-up, not timed
            linearSearch(new int[2_000], k);
            binarySearch(new int[2_000], k);
        }
        for (int n : bigSizes) {
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = 2 * i;          // sorted: 0, 2, 4, ...
            Random r = new Random(7);
            int[] targets = new int[searches];
            for (int k = 0; k < searches; k++) targets[k] = 2 * r.nextInt(n);

            long found = 0;
            steps = 0;
            long t0 = System.nanoTime();
            for (int t : targets) found += linearSearch(a, t);
            long linMs = (System.nanoTime() - t0) / 1_000_000;
            long linSteps = steps / searches;

            steps = 0;
            t0 = System.nanoTime();
            for (int t : targets) found += binarySearch(a, t);
            long binMs = (System.nanoTime() - t0) / 1_000_000;
            long binSteps = steps / searches;

            if (found == 42) System.out.print("");         // stops the JIT skipping the loop
            System.out.printf("%10d %12d %12d %16d %16d%n",
                    n, linMs, binMs, linSteps, binSteps);
        }
        System.out.println("  (steps = average comparisons for ONE search)");

        // ------------------------------------------------ Experiment 4
        System.out.println();
        System.out.println("Experiment 4: sort n random ints (both sorts get the SAME data)");
        System.out.printf("%10s %12s %12s %16s %16s%n",
                "n", "selection ms", "merge ms", "selection steps", "merge steps");
        for (int w = 0; w < 10; w++) {                        // warm-up, not timed:
            mergeSort(randomArray(40_000, w));                //   lets Java finish compiling
            selectionSort(randomArray(3_000, w));             //   both sorts first
        }
        for (int n : sizes) {
            int[] data = randomArray(n, 99);

            int[] a = data.clone();
            steps = 0;
            long t0 = System.nanoTime();
            selectionSort(a);
            long selMs = (System.nanoTime() - t0) / 1_000_000;
            long selSteps = steps;

            int[] b = data.clone();
            steps = 0;
            t0 = System.nanoTime();
            mergeSort(b);
            long merMs = (System.nanoTime() - t0) / 1_000_000;
            long merSteps = steps;

            if (!java.util.Arrays.equals(a, b)) System.out.println("  sorts disagree!");
            System.out.printf("%10d %12d %12d %16d %16d%n",
                    n, selMs, merMs, selSteps, merSteps);
        }

        System.out.println();
        System.out.println("Each time n DOUBLES, which columns...");
        System.out.println("  barely move?               (constant O(1), or O(log n): +1 step)");
        System.out.println("  roughly double?            (O(n))");
        System.out.println("  a little MORE than double? (O(n log n))");
        System.out.println("  roughly QUADRUPLE?         (O(n^2))");
        System.out.println("One O(n) call, repeated n times, is O(n^2).  That is Big-O.");
    }
}
