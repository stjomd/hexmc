package at.ac.tuwien.student.e11843614;

/**
 * A class that outputs messages to the console.
 */
public abstract class Logger {

    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String RESET = "\u001B[0m";

    /**
     * Outputs an info message to the console, even when the logger is off.
     * @param x the object to be output.
     */
    public static void info(Object x) {
        System.out.println(x);
    }

    /**
     * Outputs a debug message to the console.
     * @param x the object to be output.
     */
    public static void debug(Object x) {
        if (Constants.verbose()) {
            System.out.println(CYAN + x.toString() + RESET);
        }
    }

    /**
     * Outputs a warning message to the console.
     * @param x the object to be output.
     */
    public static void warn(Object x) {
        if (Constants.verbose()) {
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
