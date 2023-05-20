package at.ac.tuwien.student.e11843614.formula;

import at.ac.tuwien.student.e11843614.Logger;
import at.ac.tuwien.student.e11843614.exception.FormulaParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * An object responsible for parsing a propositional formula in CNF format from a DIMACS CNF file.
 */
public class FormulaReader {

    private final String path;
    private Integer variableBound = Integer.MAX_VALUE;
    private Integer clauseBound = Integer.MAX_VALUE;

    public FormulaReader(String path) {
        this.path = path;
    }

    /**
     * Reads the DIMACS CNF file and returns the respective Formula object.
     * @return a formula parsed from the file.
     * @throws FileNotFoundException if the file is not found.
     * @throws FormulaParseException if an error occurs during parsing.
     */
    public Formula parseFormula() throws FileNotFoundException, FormulaParseException {
        File file = new File(path);
        Scanner scanner = new Scanner(file);
        Formula formula = new Formula();
        int line = 0;
        int variables = 0;
        int clauses = 0;
        while (scanner.hasNextLine()) {
            line++;
            String[] items = scanner.nextLine().split(" ");
            String prefix = path + ":" + line + ": "; // for error messages
            if (items[0].equals("")) {
                // empty line
                continue;
            } else if (items[0].equals("c")) {
                // comment line
                continue;
            } else if (items[0].equals("p")) {
                // header line
                if (!items[1].equals("cnf")) {
                    throw new FormulaParseException(prefix + "expected 'cnf' in header, got \"" + items[1] + "\"");
                }
                try {
                    variableBound = Integer.parseInt(items[2]);
                } catch (NumberFormatException exception) {
                    throw new FormulaParseException(prefix + "expected a number, got \"" + items[2] + "\"", exception);
                }
                try {
                    clauseBound = Integer.parseInt(items[3]);
                } catch (NumberFormatException exception) {
                    throw new FormulaParseException(prefix + "expected a number, got \"" + items[3] + "\"", exception);
                }
            } else {
                // clause line
                clauses++;
                if (clauses > clauseBound) {
                    throw new FormulaParseException(
                        String.format(prefix + "exceeded the amount of clauses (header specifies %d clauses)", clauseBound)
                    );
                }
                if (!items[items.length - 1].equals("0")) {
                    throw new FormulaParseException(prefix + "clause line does not terminate with zero");
                }
                Clause clause = new Clause();
                for (String item : items) {
                    if (item.equals("0")) {
                        break;
                    } else {
                        try {
                            int literal = Integer.parseInt(item);
                            int variable = Math.abs(literal);
                            clause.addLiteral(literal);
                            if (variable > variables) {
                                variables = variable;
                            }
                            if (variables > variableBound) {
                                throw new FormulaParseException(
                                    String.format(prefix + "exceeded the amount of variables (header specifies %d variables)", variableBound)
                                );
                            }
                        } catch (NumberFormatException exception) {
                            throw new FormulaParseException(prefix + "expected a number, got \"" + item + "\"", exception);
                        }
                    }
                }
                formula.addClause(clause);
            }
        }
        scanner.close();
        if (variables != variableBound) {
            Logger.warn(
                String.format("Header specifies %d variables, parsed: %d", variableBound, variables)
            );
        }
        if (clauses != clauseBound) {
            Logger.warn(
                String.format("Header specifies %d clauses, parsed: %d", clauseBound, clauses)
            );
        }
        return formula;
    }

}
