// FIXED: Shifted package route to match your actual workspace namespace
package com.restaurant.restaurant;

/**
 * JVM Bootstrapper Class.
 * Bypasses strict runtime module verification checks by launching JavaFX
 * from a generic standalone class instead of an inherited Application instance.
 */
public class Launcher {
    public static void main(String[] args) {
        // FIXED: Explicitly hands off execution flow to your package's real application root
        com.restaurant.restaurant.App.main(args);
    }
}