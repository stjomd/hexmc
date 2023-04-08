package at.ac.tuwien.student.e11843614;

/**
 * A class that outputs messages to the console.
 */
public abstract class Logger {

    private static boolean on = false;

    private static final String YELLOW = "\u001B[33m";
    private static final String RESET = "\u001B[0m";

    /**
     * Turns the logger on or off. If off, the logger will output only error messages.
     * @param verbose a boolean value that indicates whether this logger is on.
     */
    public static void set(boolean verbose) {
        Logger.on = verbose;
    }

    /**
     * Outputs an info message to the console.
     * @param x the object to be output.
     */
    public static void info(Object x) {
        if (on) {
            System.out.println(x);
        }
    }

    /**
     * Outputs a warning message to the console.
     * @param x the object to be output.
     */
    public static void warn(Object x) {
        if (on) {
            System.out.println(YELLOW + x.toString() + RESET);
        }
    }

    /**
     * Outputs an error message to stderr, even when the logger is off.
     * @param x the object to be output.
     */
    public static void error(Object x) {
        System.err.println(x);
    }

}
