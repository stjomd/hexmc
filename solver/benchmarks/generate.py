import multiprocessing.pool
import os
import pathlib
import random
import subprocess
import sys
import threading

# Amount of formulas generated for each (n, m)
tries_per_combination = 5
# Amount of simultaneous threads
simultaneous_threads = os.cpu_count() + 4

# Paths (don't have to be changed if project structure not changed)
temp_path = pathlib.Path(__file__).parent / "temp"
instances_path = pathlib.Path(__file__).parent / "instances"
failed_path = instances_path / "failed"
solver_path = pathlib.Path(__file__).parent.parent / "hexmc"

# Info about already stored instances
# Refilled in restore_progress if continuing after a break
progress = {}
fails = 0
progress_lock = threading.Lock()
fails_lock = threading.Lock()

# Info about instances per (n, m) pair
# Used to output a message when 'tries_per_combination' instances are processed
pair_info = {}
pair_info_lock = threading.Lock()

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
def write_formula(formula, path, variables, clauses, comments):
    with open(path, "w") as file:
        file.write("p cnf " + str(variables) + " " + str(clauses) + "\n")
        for comment in comments:
            file.write("c " + comment + "\n")
        for clause in formula:
            string = ' '.join(str(x) for x in clause)
            string += " 0\n"
            file.write(string)

# Runs the solver, parses the output
def run_solver(path):
    command = '"' + str(solver_path) + '" "' + str(path) + '" --verbose'
    process = subprocess.Popen(command, shell = True, stdout = subprocess.PIPE, stderr = subprocess.PIPE)
    stdout, stderr = process.communicate()
    # Raise exception if solver reported error
    if process.returncode != 0:
        # Get the error message
        message = stderr.splitlines()[0].decode("UTF-8")
        # Get the runtime from stdout
        time = ""
        lines = stdout.splitlines()
        for line in lines:
            trimmed = line.decode("UTF-8")[5:-4]
            if trimmed.startswith("Total runtime:"):
                time = trimmed.split()[-1]
        raise RuntimeError(message, time)
    # Parse output
    width = -1
    time = ""
    lines = stdout.splitlines()
    for line in lines:
        trimmed = line.decode("UTF-8")[5:-4]
        if trimmed.startswith("ps-width of the decomposition is"):
            width = int(trimmed.split()[-1])
        elif trimmed.startswith("[psw] Time elapsed:"):
            time = trimmed.split()[-1]
    models = int(lines[-1].decode("UTF-8"))
    return [width, time, models]

# Fills the progress dict and fails value again, to ensure old instances are not overwritten
def restore_progress():
    global fails
    for directory in os.listdir(instances_path):
        if directory == "failed":
            for instance in os.listdir(instances_path / directory):
                if instance.endswith(".cnf"):
                    fails += 1
        elif directory.isdigit():
            order = 0
            for instance in os.listdir(instances_path / directory):
                if instance.endswith(".cnf"):
                    order = max(order, int(instance.split('.')[0].split('-')[-1]))
            width = int(directory)
            progress[width] = order

# Update pair info, thread safe
def increment_pair_info(n, m):
    pair_info_lock.acquire()
    if n not in pair_info:
        pair_info[n] = {}
    if m not in pair_info[n]:
        pair_info[n][m] = 0
    pair_info[n][m] += 1
    pair_info_lock.release()
    if pair_info[n][m] == tries_per_combination:
        print("n = {}, m = {}: processed {} instances".format(n, m, pair_info[n][m]))

# Perform all the actions for the triple (n, m, i)
def perform(n, m, i):
    global fails
    temp_file = temp_path / "temp-{}-{}-{}.cnf".format(n, m, i)
    formula = construct_formula(n, m)
    # Create a temporary file, and run solver on it
    write_formula(formula, temp_file, n, m, [])
    try:
        width, time, models = run_solver(temp_file)
    except RuntimeError as error:
        fails_lock.acquire()
        fails += 1
        fails_lock.release()
        print("n = {}, m = {}, i = {}: runtime = {}, solver reported error: {}".format(n, m, i, error.args[1], error.args[0]))
        write_formula(formula, failed_path / (str(fails) + ".cnf"), n, m, [
            "solver reported error:",
            str(error.args[0]),
            "runtime: " + error.args[1]
        ])
        increment_pair_info(n, m)
        return
    # Record in progress dict
    progress_lock.acquire()
    if not width in progress:
        progress[width] = 0
    progress[width] += 1
    progress_lock.release()
    # Save to instances folder
    path = instances_path / str(width)
    if not os.path.exists(path):
        os.makedirs(path)
    file_name = "psw-" + str(width) + "-order-" + str(progress[width]) + ".cnf"
    write_formula(formula, path / file_name, n, m, [
        "ps-width: " + str(width),
        "models: " + str(models),
        "time: " + time
    ])
    # Remove temporary file
    if os.path.isfile(temp_file):
        os.remove(temp_file)
    # Output to console
    print("n = {}, m = {}, i = {}: decomposition had ps-width {}, elapsed time was {}".format(n, m, i, width, time))
    increment_pair_info(n, m)

if __name__ == "__main__":
    # Parse arguments
    size = 0
    start_from_n = 2
    start_from_m = None
    if len(sys.argv) == 2:
        size = int(sys.argv[1])
    elif len(sys.argv) == 4:
        size = int(sys.argv[1])
        start_from_n = int(sys.argv[2])
        start_from_m = int(sys.argv[3])
        print("Continuing from n = {}, m = {}".format(start_from_n, start_from_m))
        # Restore progress values so that we don't overwrite any instances
        restore_progress()
    else:
        print("usage: python3 generate.py <max size> [<start n> <start m>]")
        exit(1)
    # Create directories
    if not os.path.exists(temp_path):
        os.makedirs(temp_path)
    if not os.path.exists(instances_path):
        os.makedirs(instances_path)
    if not os.path.exists(failed_path):
        os.makedirs(failed_path)
    # Use a thread pool to submit jobs to
    pool = multiprocessing.pool.ThreadPool(processes = simultaneous_threads)
    # n is the amount of variables, m is the amount of clauses
    try:
        for n in range(start_from_n, size + 1):
            # To start from (n,m), if n==start_from_n, we set range to [start_from_m, size]. Otherwise do as usual [1, size].
            clause_range = range(1, size + 1)
            if start_from_m != None and n == start_from_n:
                clause_range = range(start_from_m, size + 1)
            # Iterate over m
            for m in clause_range:
                # Submit jobs to the pool
                for i in range(tries_per_combination):
                    pool.apply_async(perform, args = (n, m, i))
        pool.close()
        pool.join()
        # Delete temporary folder
        if os.path.isdir(temp_path):
            os.rmdir(temp_path)
        print("Done.")
    except KeyboardInterrupt as exception:
        print(" KeyboardInterrupt")
        pool.terminate()
        print("Terminated all threads.")
        exit(1)
