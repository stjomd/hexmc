package at.ac.tuwien.student.e11843614;

import at.ac.tuwien.student.e11843614.exception.FormulaParseException;
import at.ac.tuwien.student.e11843614.exception.InfiniteModelsException;
import at.ac.tuwien.student.e11843614.formula.Formula;
import at.ac.tuwien.student.e11843614.counting.ModelCounting;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.sat4j.specs.TimeoutException;

import java.io.IOException;
import java.util.Properties;

import static java.lang.System.exit;

public class Main {

    public static void main(String[] args) {

        Properties properties = new Properties();
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream("project.properties"));
        } catch (IOException exception) {
            Logger.error("Unable to load the properties file");
            exception.printStackTrace();
            exit(1);
        }

        ArgumentParser parser = ArgumentParserFactory.parser(
            properties.getProperty("name"), properties.getProperty("version")
        );

        try {
            // Parse arguments
            Namespace namespace = parser.parseArgs(args);
            String path = namespace.getString("input");
            Constants.set(namespace);
            // Count models
            Formula formula = Formula.fromPath(path);
            long models = ModelCounting.count(formula, Constants.algorithm());
            Logger.info(models);
        } catch (FormulaParseException exception) {
            Logger.error(exception.getMessage());
            exit(1);
        } catch (InfiniteModelsException exception) {
            Logger.info("inf");
        } catch (TimeoutException exception) {
            Logger.error("Timeout (" + Constants.timeout() + " s) exceeded");
            exit(1);
        } catch (ArithmeticException exception) {
            Logger.debug("Formula might have more than " + Long.MAX_VALUE + " models (long overflow occurred)");
            Logger.info(">= " + Long.MAX_VALUE);
            exit(1);
        } catch (ArgumentParserException exception) {
            parser.handleError(exception);
            exit(1);
        } catch (Exception exception) {
            if (Constants.verbose()) {
                exception.printStackTrace();
            } else {
                Logger.error(exception.getMessage());
            }
            exit(1);
        }

        exit(0);
    }

}
