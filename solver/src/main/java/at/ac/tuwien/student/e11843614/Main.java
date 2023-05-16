package at.ac.tuwien.student.e11843614;

import at.ac.tuwien.student.e11843614.exception.InfiniteModelsException;
import at.ac.tuwien.student.e11843614.formula.Formula;
import at.ac.tuwien.student.e11843614.sharpsat.ModelCounting;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.sat4j.specs.TimeoutException;

import java.util.Locale;

import static java.lang.System.exit;

public class Main {

    public static void main(String[] args) {

        ArgumentParser parser = ArgumentParsers.newFor("solver")
            .locale(Locale.US)
            .singleMetavar(true)
            .build()
            .description("Accepts a CNF formula in DIMACS CNF format, and counts the number of its models.");
        parser.addArgument("input")
            .type(String.class)
            .help("the input path for the DIMACS CNF file");
        parser.addArgument("-a", "--alg")
            .type(Constants.Parameter.class)
            .setDefault(Constants.Parameter.psw)
            .help("specifies the algorithm to use for model counting (either utilizing ps-width or clique-width)."
                + " WARNING! cw is experimental and does not return correct answers, use it only for debugging."
                + " The standard value is psw (ps-width)");
        parser.addArgument("-t", "--timeout")
            .metavar("SECONDS")
            .type(int.class)
            .setDefault(0)
            .help("timeout in seconds for the SAT solver");
        parser.addArgument("--verbose")
            .type(boolean.class)
            .action(Arguments.storeTrue()) // defaults to... false
            .help("output additional information");

        try {
            Namespace namespace = parser.parseArgs(args);
            String path = namespace.getString("input");
            Constants.setAlgorithm(namespace.get("alg"));
            Constants.setTimeout(namespace.getInt("timeout"));
            Constants.setVerbose(namespace.getBoolean("verbose"));

            Formula formula = Formula.fromPath(path);
            long models = ModelCounting.count(formula);
            Logger.info(models);
        } catch (InfiniteModelsException exception) {
            Logger.info("inf");
        } catch (TimeoutException exception) {
            Logger.error("Timeout (" + Constants.timeout() + " s) exceeded");
            exit(1);
        } catch (ArithmeticException exception) {
            Logger.error("Overflow, formula might have more than " + Long.MAX_VALUE + " models");
            Logger.info(">= " + Long.MAX_VALUE);
            exit(1);
        } catch (ArgumentParserException exception) {
            parser.handleError(exception);
            exit(1);
        } catch (Exception exception) {
            exception.printStackTrace();
            exit(1);
        }

        exit(0);
    }

}
