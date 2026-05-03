import java.math.BigInteger;
import java.util.Random;

/**
 * CPT212 Assignment 1 - Part 2
 * Karatsuba Multiplication Algorithm with primitive operation counter.
 * Modified to support large numbers via BigInteger for n-digit benchmarking.
 */
public class KaratsubaModified {

    /** Global primitive operation counter */
    static long opCount = 0;

    /**
     * Karatsuba multiplication for BigInteger inputs.
     * Counts every primitive operation: assignments, comparisons, additions, method calls.
     *
     * @param x first operand
     * @param y second operand
     * @return x * y
     */
    public static BigInteger mult(BigInteger x, BigInteger y) {

        opCount++; // comparison (x.compareTo)
        opCount++; // comparison (y.compareTo)
        // Base case: if either number is a single digit
        if (x.compareTo(BigInteger.TEN) < 0 && y.compareTo(BigInteger.TEN) < 0) {
            opCount++; // multiply
            return x.multiply(y);
        }

        // Find the number of digits in each number
        int noOneLength = numLength(x);     opCount += 2; // assignment + call
        int noTwoLength = numLength(y);     opCount += 2; // assignment + call

        // Find maximum length
        int maxNumLength = Math.max(noOneLength, noTwoLength);  opCount += 2; // assignment + call

        // Round up half the max length
        int halfMaxNumLength = (maxNumLength / 2) + (maxNumLength % 2); opCount += 3; // assignment + div + mod

        // Multiplier (10^halfMaxNumLength)
        BigInteger maxNumLengthTen = BigInteger.TEN.pow(halfMaxNumLength); opCount += 2; // assignment + call

        // Split x and y into high and low halves
        BigInteger a = x.divide(maxNumLengthTen);   opCount += 2; // assignment + divide
        BigInteger b = x.remainder(maxNumLengthTen); opCount += 2; // assignment + remainder
        BigInteger c = y.divide(maxNumLengthTen);   opCount += 2; // assignment + divide
        BigInteger d = y.remainder(maxNumLengthTen); opCount += 2; // assignment + remainder

        // Recursive calls
        BigInteger z0 = mult(a, c);              opCount += 2; // assignment + recursive call
        BigInteger z1 = mult(a.add(b), c.add(d)); opCount += 4; // assignment + 2 additions + recursive call
        BigInteger z2 = mult(b, d);              opCount += 2; // assignment + recursive call

        // Combine: z0 * 10^(2*half) + (z1 - z0 - z2) * 10^half + z2
        BigInteger shift2 = BigInteger.TEN.pow(halfMaxNumLength * 2); opCount += 3; // assignment + multiply + call
        BigInteger shift1 = BigInteger.TEN.pow(halfMaxNumLength);     opCount += 2; // assignment + call

        BigInteger middle = z1.subtract(z0).subtract(z2).multiply(shift1); opCount += 4; // assignment + 2 subs + multiply
        BigInteger ans = z0.multiply(shift2).add(middle).add(z2);          opCount += 4; // assignment + multiply + 2 adds

        return ans;
    }

    /**
     * Returns the number of decimal digits in n.
     */
    public static int numLength(BigInteger n) {
        opCount++; // assignment (noLen)
        int noLen = 0;
        opCount++; // comparison (n > 0)
        while (n.compareTo(BigInteger.ZERO) > 0) {
            opCount++; // increment noLen
            noLen++;
            opCount += 2; // divide + assignment
            n = n.divide(BigInteger.TEN);
            opCount++; // comparison (loop condition)
        }
        return noLen;
    }

    /**
     * Generates a random BigInteger with exactly n digits.
     */
    public static BigInteger randomNDigitBigInteger(int n, Random r) {
        if (n == 1) return BigInteger.valueOf(r.nextInt(9) + 1);
        StringBuilder sb = new StringBuilder();
        sb.append((char) ('1' + r.nextInt(9))); // first digit 1-9
        for (int i = 1; i < n; i++) {
            sb.append((char) ('0' + r.nextInt(10)));
        }
        return new BigInteger(sb.toString());
    }

    /** Benchmark: vary n from 1 to maxN, print CSV */
    public static void benchmark(int maxN) {
        Random r = new Random(42);
        System.out.println("n,opCount");
        for (int n = 1; n <= maxN; n++) {
            BigInteger x = randomNDigitBigInteger(n, r);
            BigInteger y = randomNDigitBigInteger(n, r);
            opCount = 0;
            mult(x, y);
            System.out.println(n + "," + opCount);
        }
    }

    public static void main(String[] args) {
        // --- Correctness tests ---
        System.out.println("===== Correctness Tests =====");

        long expected, actual;

        expected = 1234L * 5678L;
        opCount = 0;
        actual = mult(BigInteger.valueOf(1234), BigInteger.valueOf(5678)).longValue();
        System.out.println("Expected 1: " + expected);
        System.out.println("Actual   1: " + actual);
        System.out.println("ops: " + opCount);
        assert expected == actual : "Test 1 failed";

        expected = 102L * 313L;
        opCount = 0;
        actual = mult(BigInteger.valueOf(102), BigInteger.valueOf(313)).longValue();
        System.out.println("Expected 2: " + expected);
        System.out.println("Actual   2: " + actual);
        System.out.println("ops: " + opCount);
        assert expected == actual : "Test 2 failed";

        expected = 1345L * 63456L;
        opCount = 0;
        actual = mult(BigInteger.valueOf(1345), BigInteger.valueOf(63456)).longValue();
        System.out.println("Expected 3: " + expected);
        System.out.println("Actual   3: " + actual);
        System.out.println("ops: " + opCount);
        assert expected == actual : "Test 3 failed";
        System.out.println();

        // --- Benchmark ---
        System.out.println("===== Benchmark (n=1 to 50) =====");
        benchmark(50);
    }
}
