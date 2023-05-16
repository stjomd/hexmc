package at.ac.tuwien.student.e11843614;

import net.sourceforge.argparse4j.inf.Namespace;

public abstract class Constants {

    public enum Parameter {
        psw, cw
    }

    private static Parameter algorithm = Parameter.psw;
    private static boolean carving = false;
    private static int timeout = 0;
    private static boolean verbose = false;

    public static void set(Namespace namespace) {
        algorithm = namespace.get("alg");
        carving = namespace.getBoolean("carving");
        timeout = namespace.getInt("timeout");
        verbose = namespace.getBoolean("verbose");
    }

    public static Parameter algorithm() {
        return algorithm;
    }

    public static boolean carving() {
        return carving;
    }

    public static int timeout() {
        return timeout;
    }

    public static boolean verbose() {
        return verbose;
    }

}
