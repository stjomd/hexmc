package at.ac.tuwien.student.e11843614;

public abstract class Constants {

    public enum Parameter {
        psw, cw
    }

    private static Parameter algorithm = Parameter.psw;
    private static int timeout = 0;
    private static boolean verbose = false;

    public static Parameter algorithm() {
        return algorithm;
    }
    public static void setAlgorithm(Parameter parameter) {
        Constants.algorithm = parameter;
    }

    public static int timeout() {
        return timeout;
    }
    public static void setTimeout(int seconds) {
        Constants.timeout = seconds;
    }

    public static boolean verbose() {
        return verbose;
    }
    public static void setVerbose(boolean flag) {
        Constants.verbose = flag;
    }

}
