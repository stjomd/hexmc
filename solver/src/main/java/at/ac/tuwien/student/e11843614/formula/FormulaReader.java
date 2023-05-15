package at.ac.tuwien.student.e11843614.formula;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * An object responsible for parsing a propositional formula in CNF format from a DIMACS CNF file.
 */
public class FormulaReader {

    private final String path;
    private Integer clauseBound = Integer.MAX_VALUE;
    private Integer clauses = 0;

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
        while (scanner.hasNextLine()) {
            line++;
            String[] items = scanner.nextLine().split(" ");
            if (items[0].equals("c")) {
                // comment line
                continue;
            } else if (items[0].equals("p")) {
                // header line
                if (!items[1].equals("cnf")) {
                    throw new FormulaParseException("Expected 'cnf' in header, got " + items[1]);
                }
                try {
                    clauseBound = Integer.parseInt(items[3]);
                } catch (NumberFormatException exception) {
                    throw new FormulaParseException("Expected a number, " + exception.getMessage(), exception);
                }
            } else {
                // clause line
                clauses++;
                if (clauses > clauseBound) {
                    throw new FormulaParseException(
                        String.format("Size specification mismatch (header specifies %d clauses)", clauseBound)
                    );
                }
                if (!items[items.length - 1].equals("0")) {
                    throw new FormulaParseException("Clause line " + line + " does not terminate with zero");
                }
                Clause clause = new Clause();
                for (String item : items) {
                    if (item.equals("0")) {
                        break;
                    } else {
                        try {
                            clause.addLiteral(Integer.parseInt(item));
                        } catch (NumberFormatException exception) {
                            throw new FormulaParseException("Expected a number, " + exception.getMessage(), exception);
                        }
                    }
                }
                formula.addClause(clause);
            }
        }
        scanner.close();
        return formula;
    }

}
