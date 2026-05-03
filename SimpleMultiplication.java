import java.math.BigInteger;
import java.util.Random;

/**
 * CPT212 Assignment 1 - Part 1
 * Simple Multiplication Algorithm using digit-by-digit multiplication
 * Supports large numbers via BigInteger, with operation counter for complexity analysis.
 */
public class SimpleMultiplication {

    /** Global counter for primitive operations */
    static long opCount = 0;

    /**
     * Performs simple long multiplication on two n-digit numbers represented as digit arrays.
     * Prints partial products and carriers at every step for small inputs.
     * 
     * @param multiplicand digit array of the multiplicand (most significant digit first)
     * @param multiplier   digit array of the multiplier (most significant digit first)
     * @param printSteps   whether to print partial products and carriers
     * @return BigInteger result of the multiplication
     */
    public static BigInteger simpleMultiply(int[] multiplicand, int[] multiplier, boolean printSteps) {
        int n = multiplicand.length;       opCount++;  // assignment
        int m = multiplier.length;         opCount++;  // assignment

        // partialRows[i] stores the partial product row for multiplier digit i (from rightmost)
        // Each row has n+1 positions to accommodate carry overflow
        BigInteger[] partialProducts = new BigInteger[m]; opCount++;  // assignment
        BigInteger[] carriers        = new BigInteger[m]; opCount++;  // assignment

        // Step 1: Multiply each multiplier digit by the entire multiplicand
        for (int i = 0; i < m; i++) {                           opCount += 3; // init, compare, increment
            int multiplierDigit = multiplier[m - 1 - i];        opCount += 2; // assignment + array access
            opCount++;                                           // assignment (partialProducts[i])

            // Build the partial product row for this multiplier digit
            // partialRow has n+1 digits to allow a leading carry
            int[] partialRow = new int[n + 1];                  opCount++;  // assignment
            int[] carrierRow = new int[n + 1];                  opCount++;  // assignment

            for (int j = 0; j < n; j++) {                       opCount += 3; // init, compare, increment
                int multiplicandDigit = multiplicand[n - 1 - j]; opCount += 2; // assignment + array access
                int product = multiplierDigit * multiplicandDigit; opCount += 2; // assignment + multiply
                int partialDigit = product % 10;               opCount += 2; // assignment + mod
                int carrierDigit = product / 10;               opCount += 2; // assignment + div

                partialRow[j] = partialDigit;                  opCount += 2; // assignment + array write
                carrierRow[j] = carrierDigit;                  opCount += 2; // assignment + array write
            }

            // Convert rows to BigInteger (shifted left by i positions = multiply by 10^i)
            BigInteger partialVal = rowToBigInteger(partialRow, n + 1); opCount += 2; // assignment + call
            BigInteger carrierVal = rowToBigInteger(carrierRow, n + 1); opCount += 2; // assignment + call

            // Partials shift by 10^i; carriers shift by 10^(i+1)
            // because each carry at column j contributes to column j+1
            BigInteger shift        = BigInteger.TEN.pow(i);     opCount += 2; // assignment + call
            BigInteger carrierShift = BigInteger.TEN.pow(i + 1); opCount += 2; // assignment + call
            partialProducts[i] = partialVal.multiply(shift);        opCount += 3; // assignment + array write + call
            carriers[i]        = carrierVal.multiply(carrierShift); opCount += 3; // assignment + array write + call

            if (printSteps) {
                System.out.println("partial products for (=" + arrayToString(multiplicand) +
                        " x " + multiplierDigit + ")");
                System.out.println(formatRow(partialRow, i, n));
                System.out.println("carriers for (" + arrayToString(multiplicand) +
                        " x " + multiplierDigit + ")");
                System.out.println(formatRow(carrierRow, i, n));
            }
        }

        // Step 2: Sum all partial products and carriers
        BigInteger result = BigInteger.ZERO;                    opCount++;  // assignment
        for (int i = 0; i < m; i++) {                          opCount += 3; // init, compare, increment
            result = result.add(partialProducts[i]);           opCount += 3; // assignment + array access + call
            result = result.add(carriers[i]);                  opCount += 3; // assignment + array access + call
        }
        return result;
    }

    /**
     * Converts a digit array (least significant digit first) to BigInteger.
     */
    private static BigInteger rowToBigInteger(int[] row, int len) {
        BigInteger val = BigInteger.ZERO;                       opCount++;
        BigInteger base = BigInteger.ONE;                       opCount++;
        for (int k = 0; k < len; k++) {                        opCount += 3;
            val = val.add(BigInteger.valueOf(row[k]).multiply(base)); opCount += 4;
            base = base.multiply(BigInteger.TEN);              opCount += 2;
        }
        return val;
    }

    /** Formats a digit row with proper shift for display */
    private static String formatRow(int[] row, int shift, int n) {
        StringBuilder sb = new StringBuilder();
        int totalWidth = n + shift + 2;
        // Build number string (MSB first)
        for (int k = n; k >= 0; k--) {
            sb.append(row[k]);
        }
        // Remove leading zeros but keep at least one digit
        String s = sb.toString().replaceFirst("^0+(?!$)", "");
        // Right-pad for shift
        StringBuilder padded = new StringBuilder(s);
        for (int k = 0; k < shift; k++) padded.append("0");
        return String.format("%" + totalWidth + "s", padded.toString());
    }

    /** Converts an int array to a readable string */
    private static String arrayToString(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int d : arr) sb.append(d);
        return sb.toString();
    }

    /**
     * Generates a random n-digit number as a digit array (no leading zeros).
     */
    public static int[] randomNDigitNumber(int n, Random r) {
        int[] digits = new int[n];
        digits[0] = r.nextInt(9) + 1;   // first digit: 1-9
        for (int i = 1; i < n; i++) {
            digits[i] = r.nextInt(10);
        }
        return digits;
    }

    /** Demo: small numbers to show step-by-step output */
    private static void demonstrateSmallNumbers() {
        System.out.println("===== Small Number Demo (52301 x 380) =====");
        int[] a = {5, 2, 3, 0, 1};
        int[] b = {3, 8, 0};
        opCount = 0;
        BigInteger result = simpleMultiply(a, b, true);
        System.out.println("Result: " + result);
        System.out.println("Primitive operations: " + opCount);
        System.out.println();
    }

    /** Benchmark: vary n from 1 to maxN and print opCount */
    private static void benchmark(int maxN) {
        Random r = new Random(42);
        System.out.println("n,opCount");
        for (int n = 1; n <= maxN; n++) {
            int[] a = randomNDigitNumber(n, r);
            int[] b = randomNDigitNumber(n, r);
            opCount = 0;
            simpleMultiply(a, b, false);
            System.out.println(n + "," + opCount);
        }
    }

    public static void main(String[] args) {
        // --- Small demo with printed steps ---
        demonstrateSmallNumbers();

        // --- Second small demo ---
        System.out.println("===== Small Number Demo (1234 x 56) =====");
        int[] x = {1, 2, 3, 4};
        int[] y = {5, 6};
        opCount = 0;
        BigInteger res2 = simpleMultiply(x, y, true);
        System.out.println("Result: " + res2);
        System.out.println();

        // --- Benchmark for graph (n = 1 to 50; for 10000 use separate run) ---
        System.out.println("===== Benchmark (n=1 to 50) =====");
        benchmark(50);
    }
}
