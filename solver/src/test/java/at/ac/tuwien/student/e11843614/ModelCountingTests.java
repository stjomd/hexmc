package at.ac.tuwien.student.e11843614;

import at.ac.tuwien.student.e11843614.exception.InfiniteModelsException;
import at.ac.tuwien.student.e11843614.formula.Clause;
import at.ac.tuwien.student.e11843614.formula.Formula;
import at.ac.tuwien.student.e11843614.sharpsat.ModelCounting;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Model counting")
public class ModelCountingTests {

    // ----- Special cases ---------------------------------------------------------------------------------------------

    @Test
    @DisplayName("Empty formula has infinite models")
    public void emptyFormula_shouldReturnInf() {
        Formula formula = new Formula();
        assertThrows(InfiniteModelsException.class, () -> ModelCounting.count(formula));
    }

    @Test
    @DisplayName("Formula consisting of empty clauses has no models")
    public void formulaOfEmptyClause_shouldReturnZero() throws Exception {
        Formula formula = new Formula();
        formula.addClause();
        formula.addClause();
        formula.addClause();
        assertEquals(0, ModelCounting.count(formula));
    }

    @Test
    @DisplayName("Formula containing an empty clause has no models")
    public void formulaWithEmptyClause_shouldReturnZero() throws Exception {
        Formula formula = new Formula();
        formula.addClause(1, 2, -3);
        formula.addClause();
        formula.addClause(-1, 3, 4, -5);
        assertEquals(0, ModelCounting.count(formula));
    }

    @Test
    @DisplayName("Formula with a single clause and k variables has 2^k-1 models")
    public void formulaWithSingleClauseAndNoNegations_shouldHaveTwoRaisedMinusOneModels() throws Exception {
        Formula formula = new Formula();
        formula.addClause(new Clause());
        int amount = 1;
        for (int i = 1; i <= 30; i++) {
            if (i % 3 != 0) {
                formula.clauses().get(0).addLiteral(i);
            } else {
                formula.clauses().get(0).addLiteral(-i);
            }
            amount *= 2;
            assertEquals(
                amount - 1, ModelCounting.count(formula),
                String.format("Formula %s does not have 2^%s-1 = %s models", formula, i, amount - 1)
            );
        }
    }

    @Test
    @DisplayName("Formula with contradictory clauses has no models")
    public void formulaWithContradictoryClauses_shouldReturnZero() throws Exception {
        Formula formula = new Formula();
        formula.addClause(1, 2, 3, -4, -5);
        formula.addClause(3, 5, -6, 7);
        formula.addClause(2, 4, -5, 7);
        formula.addClause(1, -7, 8, 9);
        // Add clauses that together contradict the second clause
        formula.addClause(-3);
        formula.addClause(-5);
        formula.addClause(6);
        formula.addClause(-7);
        assertEquals(0, ModelCounting.count(formula));
    }

    @Test
    @DisplayName("Formula with too many models (long overflow) should throw exception")
    public void formulaWithTooManyModels_shouldThrowArithmeticException() {
        Formula formula = new Formula();
        formula.addClause(new Clause());
        // Long.MAX_VALUE = 2^63 - 1
        for (int i = 1; i <= 64; i++) {
            formula.clauses().get(0).addLiteral(i);
        }
        assertThrows(ArithmeticException.class, () -> ModelCounting.count(formula));
    }

    // ----- Regular cases ---------------------------------------------------------------------------------------------
    // Checked against the exact model counter sharpSAT (https://github.com/marcthurley/sharpSAT)

    @Test
    @DisplayName("Formula 1")
    public void formula1() throws Exception {
        Formula formula = new Formula();
        formula.addClause(1, 2, 3);
        formula.addClause(-2, 3);
        assertEquals(5, ModelCounting.count(formula));
    }

    @Test
    @DisplayName("Formula 2")
    public void formula2() throws Exception {
        Formula formula = new Formula();
        formula.addClause(1, 2, 3, -4, -5);
        formula.addClause(-2, -3, 5, 6);
        formula.addClause(3, -5, 6, 7, 8);
        formula.addClause(1, 2);
        formula.addClause(-3, 5, 6, -9);
        assertEquals(332, ModelCounting.count(formula));
    }

    @Test
    @DisplayName("Formula 3")
    public void formula3() throws Exception {
        Formula formula = new Formula();
        formula.addClause(1, 2, 3, -4, -5);
        formula.addClause(-2, -3, 5, 6);
        formula.addClause(3, -5, 6, 7, 8);
        formula.addClause(1, 2, -6);
        formula.addClause(-3, 5, 6, -9);
        formula.addClause(6, -7, -8, -9, -10, -11);
        formula.addClause(10, -11, 12, 13, -14);
        formula.addClause(-12, 13, 14, -15, 16, 17, 18);
        formula.addClause(-16, 17);
        formula.addClause(-17, 18, 19);
        formula.addClause(2, -16, -18, 19);
        formula.addClause(2, -3, 4, -5, -18, 19, 20);
        assertEquals(432756, ModelCounting.count(formula));
    }

    @Test
    @DisplayName("Formula 4")
    public void formula4() throws Exception {
        Formula formula = new Formula();
        formula.addClause(1, 2, 3, -4, -5);
        formula.addClause(-2, -3, 5, 6);
        formula.addClause(3, -5, 6, 7, 8);
        formula.addClause(1, 2, -6);
        formula.addClause(-3, 5, 6, -9);
        formula.addClause(6, -7, -8, -9, -10, -11);
        formula.addClause(10, -11, 12, 13, -14);
        formula.addClause(-12, 13, 14, -15, 16, 17, 18);
        formula.addClause(-16, 17);
        formula.addClause(-17, 18, 19);
        formula.addClause(2, -16, -18, 19);
        formula.addClause(2, -3, 4, -5, -18, 19, 20);
        formula.addClause(-1, 18);
        formula.addClause(-2, 20);
        formula.addClause(-3, 15);
        formula.addClause(-4, -12);
        formula.addClause(-5, 11);
        formula.addClause(-6, -17);
        formula.addClause(-7, 19);
        formula.addClause(1);
        formula.addClause(2);
        formula.addClause(3);
        formula.addClause(5);
        formula.addClause(6);
        formula.addClause(7);
        formula.addClause(12);
        assertEquals(32, ModelCounting.count(formula));
    }

    @Test
    @DisplayName("Formula 5")
    public void formula5() throws Exception {
        Formula formula = new Formula();
        formula.addClause(1, -2, -3, 4, 5, -6, -7, 8, -9);
        formula.addClause(3, -4, 6, 7, -8);
        formula.addClause(1, 5, 6, -7, -9, 10, 11);
        formula.addClause(2, -3, -4, 8, 9, -11, 12);
        formula.addClause(10, 11, -12, -13, -14, -15, 16);
        formula.addClause(10, -14, 15, 16, -17, 18);
        formula.addClause(1, 3, 4, 19, 20);
        formula.addClause(1, 3, 20, 21, 22);
        assertEquals(3684384, ModelCounting.count(formula));
    }

}
