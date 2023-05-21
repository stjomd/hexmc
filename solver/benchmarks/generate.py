import os
from pathlib import Path
import random
import sys

# Amount of formulas generated for each (n, m)
tries_per_combination = 5

# Paths (don't have to be changed if project structure not changed)
temp_path = Path(__file__).parent / "temp"
instances_path = Path(__file__).parent / "instances"
solver_path = Path(__file__).parent.parent / "hexmc"

# Constructs a random formula
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

# Writes the formula into a DIMACS format file
def write_formula(formula, path, variables, clauses):
    with open(path, "w") as file:
        file.write("p cnf " + str(variables) + " " + str(clauses) + "\n")
        for clause in formula:
            string = ' '.join(str(x) for x in clause)
            string += " 0\n"
            file.write(string)

# Runs the solver, parses the output
def run_solver(path):
    command = '"' + str(solver_path) + '" "' + str(path) + '" --verbose'
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
    models = int(output[-1])
    return [width, time, models]

if __name__ == "__main__":
    # Parse arguments
    size = 0
    if len(sys.argv) != 2:
        print("usage: python3 generate.py <size>")
        exit(1)
    else:
        size = int(sys.argv[1])
    # Create directories
    if not os.path.exists(temp_path):
        os.makedirs(temp_path)
    if not os.path.exists(instances_path):
        os.makedirs(instances_path)
    temp_file = temp_path / "temp.cnf"
    # Create a dict where we store how many instances for specific ps-width we obtained
    progress = {}
    # n is the amount of variables, m is the amount of clauses
    for n in range(2, size):
        for m in range(1, size):
            for i in range(tries_per_combination):
                formula = construct_formula(n, m)
                # Create a temporary file, and run solver on it
                write_formula(formula, temp_file, n, m)
                width, time, models = run_solver(temp_file)
                # Record in progress dict
                if not width in progress:
                    progress[width] = 0
                progress[width] += 1
                # Save to instances folder
                path = instances_path / str(width)
                if not os.path.exists(path):
                    os.makedirs(path)
                file_name = "psw-" + str(width) + "-order-" + str(progress[width]) + ".cnf"
                write_formula(formula, path / file_name, n, m)
                # Output to console
                print("n = {}, m = {}, i = {}: decomposition had ps-width {}, elapsed time was {}".format(n, m, i, width, time))
    # Delete temporary files
    if os.path.isfile(temp_file):
        os.remove(temp_file)
    if os.path.isdir(temp_path):
        os.rmdir(temp_path)
    print("Done.")
