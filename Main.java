import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;

/**
 * CPT212 Assignment 1 - Part 1 & 2 Combined
 * Runs both Simple Multiplication and Karatsuba for n = 1..50,
 * outputs results to CSV, and prints comparison data.
 *
 * Usage: javac *.java && java Main
 */
public class Main {

    // =========================================================
    //  Shared counter (reset before each measurement)
    // =========================================================
    static long opCount = 0;

    // =========================================================
    //  PART 1 – SIMPLE MULTIPLICATION
    // =========================================================

    /**
     * Multiplies two n-digit numbers (as digit arrays, MSB first) using
     * the simple long multiplication algorithm.
     * Counts every primitive operation.
     *
     * @param multiplicand digit array, most-significant digit first
     * @param multiplier   digit array, most-significant digit first
     * @param printSteps   print partial products and carriers if true
     * @return result as BigInteger
     */
    public static BigInteger simpleMultiply(int[] multiplicand, int[] multiplier, boolean printSteps) {
        int n = multiplicand.length; opCount++;
        int m = multiplier.length;  opCount++;

        // partialProducts[i] = (partial product row i) shifted left by i digits
        BigInteger[] partialProducts = new BigInteger[m]; opCount++;
        BigInteger[] carriers        = new BigInteger[m]; opCount++;

        // ---- Step 1: digit-by-digit multiplication ----
        for (int i = 0; i < m; i++) {                          opCount += 3;
            int mplDigit = multiplier[m - 1 - i];             opCount += 2;

            int[] pRow = new int[n + 1]; opCount++;   // partial product row
            int[] cRow = new int[n + 1]; opCount++;   // carrier row

            for (int j = 0; j < n; j++) {                      opCount += 3;
                int mcdDigit = multiplicand[n - 1 - j];        opCount += 2;
                int product  = mplDigit * mcdDigit;            opCount += 2;
                pRow[j] = product % 10;                        opCount += 2;
                cRow[j] = product / 10;                        opCount += 2;
            }

            // Convert rows to BigInteger and apply positional shift
            // Partial products shift by 10^i (position of current multiplier digit)
            // Carriers shift by 10^(i+1) because each carry at column j
            // contributes to column j+1 in the final sum
            BigInteger shift        = BigInteger.TEN.pow(i);      opCount += 2;
            BigInteger carrierShift = BigInteger.TEN.pow(i + 1);  opCount += 2;
            BigInteger partialBI = rowToBigInteger(pRow, n+1); opCount += 2;
            BigInteger carrierBI = rowToBigInteger(cRow, n+1); opCount += 2;

            partialProducts[i] = partialBI.multiply(shift);        opCount += 3;
            carriers[i]        = carrierBI.multiply(carrierShift); opCount += 3;

            // Print partial products and carriers for small inputs
            if (printSteps) {
                System.out.printf("partial products for (=%s x %d)%n",
                        digitsToString(multiplicand), mplDigit);
                System.out.println("  " + formatBigIntRow(partialProducts[i]));
                System.out.printf("carriers for (%s x %d)%n",
                        digitsToString(multiplicand), mplDigit);
                System.out.println("  " + formatBigIntRow(carriers[i]));
            }
        }

        // ---- Step 2: sum all partial products and carriers ----
        BigInteger result = BigInteger.ZERO; opCount++;
        for (int i = 0; i < m; i++) {                          opCount += 3;
            result = result.add(partialProducts[i]);           opCount += 3;
            result = result.add(carriers[i]);                  opCount += 3;
        }
        return result;
    }

    /** Converts a digit array (LSB at index 0) to BigInteger */
    private static BigInteger rowToBigInteger(int[] row, int len) {
        BigInteger val  = BigInteger.ZERO; opCount++;
        BigInteger base = BigInteger.ONE;  opCount++;
        for (int k = 0; k < len; k++) {                        opCount += 3;
            val  = val.add(BigInteger.valueOf(row[k]).multiply(base)); opCount += 4;
            base = base.multiply(BigInteger.TEN);              opCount += 2;
        }
        return val;
    }

    private static String digitsToString(int[] d) {
        StringBuilder sb = new StringBuilder();
        for (int v : d) sb.append(v);
        return sb.toString();
    }

    private static String formatBigIntRow(BigInteger v) {
        return v.toString();
    }

    // =========================================================
    //  PART 2 – KARATSUBA MULTIPLICATION
    // =========================================================

    /**
     * Karatsuba multiplication (BigInteger version) with operation counting.
     */
    public static BigInteger karatsuba(BigInteger x, BigInteger y) {
        opCount += 2; // two comparisons
        if (x.compareTo(BigInteger.TEN) < 0 && y.compareTo(BigInteger.TEN) < 0) {
            opCount++; // multiply
            return x.multiply(y);
        }

        int lenX = numLength(x); opCount += 2;
        int lenY = numLength(y); opCount += 2;
        int maxLen = Math.max(lenX, lenY); opCount += 2;

        int half = (maxLen / 2) + (maxLen % 2); opCount += 3;

        BigInteger split = BigInteger.TEN.pow(half); opCount += 2;

        BigInteger a = x.divide(split);    opCount += 2;
        BigInteger b = x.remainder(split); opCount += 2;
        BigInteger c = y.divide(split);    opCount += 2;
        BigInteger d = y.remainder(split); opCount += 2;

        BigInteger z0 = karatsuba(a, c);              opCount += 2;
        BigInteger z1 = karatsuba(a.add(b), c.add(d)); opCount += 4;
        BigInteger z2 = karatsuba(b, d);              opCount += 2;

        BigInteger shift2  = BigInteger.TEN.pow(half * 2); opCount += 3;
        BigInteger shift1  = BigInteger.TEN.pow(half);     opCount += 2;
        BigInteger middle  = z1.subtract(z0).subtract(z2).multiply(shift1); opCount += 4;
        BigInteger result  = z0.multiply(shift2).add(middle).add(z2);       opCount += 4;

        return result;
    }

    /** Returns number of decimal digits in n */
    public static int numLength(BigInteger n) {
        int len = 0; opCount++;
        while (n.compareTo(BigInteger.ZERO) > 0) {
            opCount++;  // loop compare
            len++;      opCount++;
            n = n.divide(BigInteger.TEN); opCount += 2;
        }
        return len;
    }

    // =========================================================
    //  HELPERS – Random n-digit numbers
    // =========================================================

    public static int[] randomNDigitArray(int n, Random r) {
        int[] d = new int[n];
        d[0] = r.nextInt(9) + 1;
        for (int i = 1; i < n; i++) d[i] = r.nextInt(10);
        return d;
    }

    public static BigInteger randomNDigitBigInteger(int n, Random r) {
        if (n == 1) return BigInteger.valueOf(r.nextInt(9) + 1);
        StringBuilder sb = new StringBuilder();
        sb.append((char)('1' + r.nextInt(9)));
        for (int i = 1; i < n; i++) sb.append((char)('0' + r.nextInt(10)));
        return new BigInteger(sb.toString());
    }

    // =========================================================
    //  MAIN
    // =========================================================

    public static void main(String[] args) {

        // ---- Demo: show step-by-step for small numbers ----
        System.out.println("============================================================");
        System.out.println("  PART 1: Simple Multiplication – Step-by-Step Demo");
        System.out.println("============================================================");
        System.out.println("52301 x 380:");
        opCount = 0;
        BigInteger demo1 = simpleMultiply(
            new int[]{5,2,3,0,1}, new int[]{3,8,0}, true);
        System.out.println("Result = " + demo1 + "  |  ops = " + opCount);

        System.out.println("\n1234 x 56:");
        opCount = 0;
        BigInteger demo2 = simpleMultiply(
            new int[]{1,2,3,4}, new int[]{5,6}, true);
        System.out.println("Result = " + demo2 + "  |  ops = " + opCount);

        // ---- Demo: Karatsuba correctness ----
        System.out.println("\n============================================================");
        System.out.println("  PART 2: Karatsuba – Correctness Check");
        System.out.println("============================================================");
        long[][] tests = {{1234,5678},{102,313},{1345,63456}};
        for (int t = 0; t < tests.length; t++) {
            long ex = tests[t][0] * tests[t][1];
            opCount = 0;
            long ac = karatsuba(BigInteger.valueOf(tests[t][0]),
                                BigInteger.valueOf(tests[t][1])).longValue();
            System.out.printf("Test %d: expected=%d  actual=%d  ops=%d%n",
                              t+1, ex, ac, opCount);
        }

        // ---- Benchmark: n = 1..50 ----
        System.out.println("\n============================================================");
        System.out.println("  BENCHMARK: n-digit multiplication (n = 1 to 50)");
        System.out.println("  Format: n, simpleOps, karatsubaOps");
        System.out.println("============================================================");

        int MAX_N = 50;
        Random r = new Random(42);

        long[] simpleOps   = new long[MAX_N + 1];
        long[] karatsubaOps = new long[MAX_N + 1];

        System.out.println("n,simpleOps,karatsubaOps");
        for (int n = 1; n <= MAX_N; n++) {
            int[] arrA  = randomNDigitArray(n, r);
            int[] arrB  = randomNDigitArray(n, r);
            BigInteger bigA = new BigInteger(digitsToString(arrA));
            BigInteger bigB = new BigInteger(digitsToString(arrB));

            opCount = 0;
            simpleMultiply(arrA, arrB, false);
            simpleOps[n] = opCount;

            opCount = 0;
            karatsuba(bigA, bigB);
            karatsubaOps[n] = opCount;

            System.out.println(n + "," + simpleOps[n] + "," + karatsubaOps[n]);
        }

        // Write CSV for Python graph
        StringBuilder csv = new StringBuilder("n,simpleOps,karatsubaOps\n");
        for (int n = 1; n <= MAX_N; n++) {
            csv.append(n).append(",").append(simpleOps[n]).append(",").append(karatsubaOps[n]).append("\n");
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("output/mult_ops.csv"))) {
            bw.write(csv.toString());
            System.out.println("\nCSV written to output/mult_ops.csv");
        } catch (IOException e) {
            System.err.println("Could not write CSV: " + e.getMessage());
        }
    }
}
