package at.ac.tuwien.student.e11843614;

import at.ac.tuwien.student.e11843614.counting.ModelCounting;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;

import java.util.Locale;

public abstract class ArgumentParserFactory {

    public static ArgumentParser parser(String name, String version) {
        ArgumentParser parser = ArgumentParsers.newFor("./" + name)
            .locale(Locale.US)
            .singleMetavar(true)
            .build()
            .version(version)
            .description("Accepts a CNF formula in DIMACS CNF format, and counts the number of its models.");
        parser.addArgument("input")
            .type(String.class)
            .help("the input path for the DIMACS CNF file");
        parser.addArgument("--version")
            .action(Arguments.version())
            .help("output the version and exit");
        parser.addArgument("-a", "--alg")
            .type(ModelCounting.Algorithm.class)
            .setDefault(ModelCounting.Algorithm.psw)
            .help("specifies the algorithm to use for model counting (either utilizing ps-width or clique-width)."
                + " WARNING! cw is experimental and does not return correct answers, use it only for debugging."
                + " The standard value is psw (ps-width)");
        parser.addArgument("-c", "--carving")
            .type(boolean.class)
            .action(Arguments.storeTrue())
            .help("use a carving decomposition as input for the psw algorithm. By default computes a decomposition heuristically");
        parser.addArgument("-t", "--timeout")
            .metavar("SECONDS")
            .type(int.class)
            .setDefault(0)
            .help("timeout in seconds for the SAT solver");
        parser.addArgument("-v", "--verbose")
            .type(boolean.class)
            .action(Arguments.storeTrue()) // defaults to... false
            .help("output additional information");
        return parser;
    }

}
