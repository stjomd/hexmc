package at.ac.tuwien.student.e11843614;

import at.ac.tuwien.student.e11843614.formula.Formula;
import at.ac.tuwien.student.e11843614.counting.ModelCounting;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang3.time.StopWatch;

import java.io.IOException;
import java.util.Properties;

public class Main {

    private static final StopWatch stopwatch = new StopWatch();

    public static void main(String[] args) {
        stopwatch.start();
        // Load properties file
        Properties properties = new Properties();
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream("project.properties"));
        } catch (IOException exception) {
            Logger.error("Unable to load the properties file");
            System.exit(1);
        }
        // Set up argument parser
        ArgumentParser parser = ArgumentParserFactory.parser(
            properties.getProperty("name"), properties.getProperty("version")
        );
        // Perform operations & handle errors
        try {
            // Parse arguments
            Namespace namespace = parser.parseArgs(args);
            String path = namespace.getString("input");
            Constants.set(namespace);
            // Count models
            Formula formula = Formula.fromPath(path);
            long models = ModelCounting.count(formula, Constants.algorithm());
            Logger.info(models);
        } catch (ArgumentParserException exception) {
            parser.handleError(exception);
            System.exit(1);
        } catch (ArithmeticException exception) {
            Logger.error("Long overflow occurred");
            Logger.info(">= " + Long.MAX_VALUE);
            gracefulExit(exception);
        } catch (Throwable exception) {
            Logger.error(exception.getMessage());
            gracefulExit(exception);
        }
        // Exit successfully
        stopwatch.stop();
        System.exit(0);
    }

    /**
     * Prints the stack trace in verbose mode, and exists with exit code 1.
     * @param exception the exception.
     */
    private static void gracefulExit(Throwable exception) {
        stopwatch.stop();
        Logger.debug("Total runtime: " + stopwatch.formatTime());
        if (exception != null && Constants.verbose()) {
            exception.printStackTrace();
        }
        System.exit(1);
    }

}
