package at.ac.tuwien.student.e11843614;

public abstract class Constants {

    private static boolean verbose = false;
    private static int timeout = 0;

    public static boolean verbose() {
        return verbose;
    }
    public static void setVerbose(boolean flag) {
        Constants.verbose = flag;
    }

    public static int timeout() {
        return timeout;
    }
    public static void setTimeout(int seconds) {
        Constants.timeout = seconds;
    }

}
