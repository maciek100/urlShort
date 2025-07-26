package urlShortener;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Stack;

import static org.testng.AssertJUnit.assertEquals;

public class Purgerer {
    public static int removePairsX(String s) {
        int [] array = s.chars()
                .map(x -> x - '0')
                .toArray();
        System.out.println(Arrays.toString(array));
        boolean repeat = true;
        int count = 0;
        while (array.length > 1 && repeat) {
            for (int i = 0; i < array.length - 1; i++) {
                if (array[i] + array[i + 1] == 1) {
                    array[i] = -200;
                    array[i + 1] = -200;
                    repeat = true;
                } else {
                    repeat = false;
                }
                array = Arrays.stream(array).filter(x -> x >= -6).toArray();
            }
        }
        System.out.println(Arrays.toString(array));
        return array.length;
    }

    public static int removePairs(String s) {
        Stack<Character> stack = new Stack<>();
        for (char c : s.toCharArray()) {
            if (!stack.isEmpty()) {
                if (c != '*') {
                    if ((c == '1' && stack.peek() == '0') || (c == '0' && stack.peek() == '1')) {
                        stack.pop();
                        continue;
                    }
                }
            }
            stack.push(c);
        }
        return stack.size();
    }

    @Test
    public void test01() {
        assertEquals(1, removePairs("01010"));
        assertEquals(0, removePairs("000111"));
        assertEquals(7, removePairs("000*111"));
        assertEquals(7, removePairs("10*00*111"));
        assertEquals(2, removePairs("100000000001111111"));
        assertEquals(5, removePairs("1*00000000001111111"));
    }
}
