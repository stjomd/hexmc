package at.ac.tuwien.student.e11843614;

import at.ac.tuwien.student.e11843614.formula.Formula;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Main {
    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("solver").build()
            .description("Accepts a CNF formula in DIMACS CNF format, and counts the number of its models.");
        parser.addArgument("input")
            .type(String.class)
            .help("the input path for the DIMACS CNF file");
        try {
            Namespace namespace = parser.parseArgs(args);
            String path = namespace.getString("input");
            Formula formula = Formula.fromPath(path);
        } catch (ArgumentParserException exception) {
            parser.handleError(exception);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
