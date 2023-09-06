/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

/**
 * The classic "FizzBuzz" problem, written in MiniJava:
 * 
 * For all integers [1, N], print the following:
 *     - "Fizz"     if the integer is divisible by three
 *     - "Buzz"     if the integer is divisible by five
 *     - "FizzBuzz" if the integer is divisible by BOTH three and five
 * 
 * NOTE: Since strings do not exist in this version of MiniJava,
 * the following simplifications have been made to the printing:
 * 
 *     - "7777" represents "Fizz"
 *     - "8888" represents "Buzz"
 *     - "9999" represents "FizzBuzz"
 * 
 * NOTE #2: Zero will be printed at the end of execution.
 */

class Main {
    /**
     * FizzBuzz in the range [1, N] where N=15.
     */
    public static void main(String[] a) {
        System.out.println(new FizzBuzz().execute(15, 1));
    }
}

class FizzBuzz extends FizzBuzzImpl {
    /**
     * Perform FizzBuzz in specified range
     * @note Public function, for user
     * @param max Range maximum value
     * @param step Increment amount
     */
    public int execute(int max, int step) {
        m_max = max + 1;
        m_step = step;

        return this.executeImpl(1);
    }
}

class FizzBuzzImpl {
    // Range maximum value
    int m_max;
    // Increment amount
    int m_step;

    /**
     * Perform FizzBuzz in specified range
     * @note Internal, recursive method
     * @param now Current value
     */
    public int executeImpl(int now) {
        int dummy;
        int result;

        if (now < m_max) {
            dummy = this.check(now);
            result = this.executeImpl(now + m_step);
        } else {
            result = 0;
        }

        return result;
    }

    /**
     * Perform FizzBuzz check and print the appropriate value.
     * @param value Value to check
     */
    public int check(int value) {
        boolean d_three;
        boolean d_five;

        // Check if value is divisible by three
        d_three = this.divisbleBy(value, 3);
        // Check if value is divisible by five
        d_five = this.divisbleBy(value, 5);

        // FizzBuzz
        if (d_three && d_five) {
            System.out.println(9999);
        }
        // Fizz
        else if (d_three) {
            System.out.println(7777);
        }
        // Buzz
        else if (d_five) {
            System.out.println(8888);
        }
        // Nothing, print the current value instead 
        else {
            System.out.println(value);
        }

        return 0;
    }

    /**
     * Check if value is divisible by the specified divisor
     * (value % divisor == 0)
     * @param value Dividend
     * @param divisor Divisor
     */
    public boolean divisbleBy(int value, int divisor) {
        int remainder;
        boolean divisible;

        remainder = this.modulo(value, divisor);
        divisible = this.equalToZero(remainder);

        return divisible;
    }

    /**
     * Modulo operation
     * @param value Value
     * @param divisor Divisor
     * @return Remainder
     */
    public int modulo(int value, int divisor) {
        int remainder;
        remainder = value;

        // while (remainder >= divisor)
        while (!(remainder < divisor)) {
            remainder = remainder - divisor;
        }

        return remainder;
    }

    /**
     * Check if value is equal to zero.
     * MiniJava only has less-than, so we do some tricky logic.
     */
    public boolean equalToZero(int value) {
        return ((value < 1) && (!(value < 0)));
    }
}
