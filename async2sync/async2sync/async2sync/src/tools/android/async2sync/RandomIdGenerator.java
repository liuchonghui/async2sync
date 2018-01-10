package tools.android.async2sync;

import java.io.Serializable;
import java.util.Random;

public class RandomIdGenerator implements Serializable {

    public static String randomId() {
        return randomId(10);
    }

    public static String randomId(int length) {
        if (length < 1) {
            return null;
        }
        // Create a char buffer to put random letters and numbers in.
        char[] randBuffer = new char[length];
        for (int i = 0; i < randBuffer.length; i++) {
            randBuffer[i] = numbersAndLetters[randGen.nextInt(71)];
        }
        return new String(randBuffer);
    }

    private static Random randGen = new Random();

    private static char[] numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz"
            + "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
}
