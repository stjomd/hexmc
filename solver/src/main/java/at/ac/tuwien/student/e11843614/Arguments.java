package at.ac.tuwien.student.e11843614;

import at.ac.tuwien.student.e11843614.counting.ModelCounting;
import net.sourceforge.argparse4j.inf.Namespace;

public abstract class Arguments {

    private static ModelCounting.Algorithm algorithm = ModelCounting.Algorithm.psw;
    private static boolean carving = false;
    private static String path = "";
    private static int timeout = 0;
    private static boolean verbose = false;

    public static void set(Namespace namespace) {
        algorithm = namespace.get("alg");
        carving = namespace.getBoolean("carving");
        path = namespace.getString("input");
        timeout = namespace.getInt("timeout");
        verbose = namespace.getBoolean("verbose");
    }

    public static ModelCounting.Algorithm algorithm() {
        return algorithm;
    }

    public static boolean carving() {
        return carving;
    }

    public static String path() {
        return path;
    }

    public static int timeout() {
        return timeout;
    }

    public static boolean verbose() {
        return verbose;
    }

}
