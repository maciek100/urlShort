package urlShortener.service;

import org.testng.annotations.Test;

import java.util.Stack;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class Asteroids {

    private static boolean asteroidsDestroyed( int mass, int [] asteroids) {
        Stack<Integer> stack = new Stack<>();
        for (int asteroid : asteroids) {
            stack.push(asteroid);
        }
        while ( !stack.isEmpty() && stack.peek() < mass) {
            mass += stack.pop();
            System.out.println(mass + " " + (!stack.isEmpty() ? stack.peek() : "Empty"));
        }
        return stack.isEmpty();
    }

    @Test
    public void testAsteroids01 () {
        int [] asteroids = new int [] {21,19,9,5};
        assertTrue(asteroidsDestroyed(10, asteroids));
    }
    @Test
    public void testAsteroids02 () {
        int [] asteroids = new int [] {21,19,20,5};
        assertFalse(asteroidsDestroyed(10, asteroids));
    }
    @Test
    public void testAsteroids03 () {
        int [] asteroids = new int [] {5,21,19,9};
        assertTrue(asteroidsDestroyed(10, asteroids));
    }

    //curl -X POST http://localhost:8080/api/v1/shorten -H "Content-Type: application/json" -d '{"url": "https://www.google.com"}'
}
