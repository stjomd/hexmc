import os
import pathlib
import random

max_width = 10
temp_path = pathlib.Path(__file__).parent / "temp"
solver_path = pathlib.Path(__file__).parent.parent / "hexmc"

def construct_formula(variables, clauses):
    formula = []
    for m in range(clauses):
        # The amount of literals that this clause will contain
        amount_of_literals = random.randrange(1, variables + 1)
        clause = []
        for _ in range(amount_of_literals):
            variable = random.randrange(1, variables + 1)
            while variable in clause or -variable in clause:
                variable = random.randrange(1, variables + 1)
            sign = random.choice([1, -1])
            clause.append(sign * variable)
        clause.sort(key = lambda n : abs(n))
        formula.append(clause)
    # Some variables might not have been added, determine which
    not_added = []
    for variable in range(1, variables + 1):
        # Go through clauses, check if variable is contained
        contained = False
        for clause in formula:
            if variable in clause or -variable in clause:
                contained = True
                break
        if not contained:
            not_added.append(variable)
    # Add these variables somewhere
    for variable in not_added:
        sign = random.choice([1, -1])
        clause_index = random.randrange(len(formula))
        formula[clause_index].append(sign * variable)
    return formula

def write_formula(formula, id, variables):
    path = temp_path / (str(id) + ".cnf")
    with open(path, "w") as file:
        file.write("p cnf " + str(variables) + " " + str(len(formula)) + "\n")
        for clause in formula:
            string = ' '.join(str(x) for x in clause)
            string += " 0\n"
            file.write(string)

def run_solver(id):
    input_path = temp_path / (str(id) + ".cnf")
    command = '"' + str(solver_path) + '" "' + str(input_path) + '" --verbose'
    stream = os.popen(command)
    output = stream.readlines()
    # Parse output
    width = -1
    time = ""
    for line in output:
        trimmed = line[5:-5]
        if trimmed.startswith("ps-width of the decomposition is"):
            width = int(trimmed.split()[-1])
        elif trimmed.startswith("[psw] Time elapsed:"):
            time = trimmed.split()[-1]
    print(width)
    print(time)

if __name__ == "__main__":
    if not os.path.exists(temp_path):
        os.makedirs(temp_path)
    formula = construct_formula(12, 3)
    write_formula(formula, 1, 12)
    run_solver(1)