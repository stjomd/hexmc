import multiprocessing.pool
import os
import pathlib
import random
import subprocess
import threading

# To capture memory usage in the output, call debugMemoryUsage() method in the solver source code.
# Otherwise this script will just write "unknown" instead of the peak memory usage.

# ----- Arguments ----------------------------------------------------
# Generate formulas for each (n,m) in ns x ms
ns = range(2, 101)
ms = range(1, 101)
# Amount of solver runs for each (n, m)
runs_per_pair = 5
# Amount of simultaneous threads
simultaneous_threads = os.cpu_count() + 5
# Paths (don't have to be changed if project structure is not changed)
temp_path = pathlib.Path(__file__).parent/"temp"
instances_path = pathlib.Path(__file__).parent/"instances"
reports_path = pathlib.Path(__file__).parent/"reports"
solver_path = pathlib.Path(__file__).parent.parent/"hexmc"
# ----- End of Arguments ---------------------------------------------

# Report information
report = {}
report_lock = threading.Lock()

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
        file.write("p cnf {} {}\n".format(variables, clauses))
        for comment in comments:
            file.write("c {}\n".format(comment))
        for clause in formula:
            string = ' '.join(str(x) for x in clause)
            string += " 0\n"
            file.write(string)

# Runs the solver, parses the output
def run_solver(input_path):
    command = '"{}" "{}" --verbose'.format(solver_path, input_path)
    process = subprocess.Popen(command, shell = True, stdout = subprocess.PIPE, stderr = subprocess.PIPE)
    stdout, stderr = process.communicate()
    # Raise exception if solver reported error
    if process.returncode != 0:
        # Get the error message
        message = stderr.splitlines()[0].decode("UTF-8")
        # Get the runtime, and psw if available from stdout
        time = "unknown"
        width = "unknown"
        memory = -1
        lines = stdout.splitlines()
        for line in lines:
            trimmed = line.decode("UTF-8")[5:-4]
            if trimmed.startswith("Total runtime:"):
                time = trimmed.split()[-1]
            elif trimmed.startswith("ps-width of the decomposition is"):
                width = trimmed.split()[-1]
            elif trimmed.startswith("[psw] Memory usage:"):
                value = float(trimmed.split()[-2])
                memory = max(memory, value)
        if memory == -1:
            memory = "unknown"
        raise RuntimeError(message, time, width, memory)
    # Parse output
    time = ""
    width = -1
    memory = -1
    lines = stdout.splitlines()
    for line in lines:
        trimmed = line.decode("UTF-8")[5:-4]
        if trimmed.startswith("[psw] Time elapsed:"):
            time = trimmed.split()[-1]
        elif trimmed.startswith("ps-width of the decomposition is"):
            width = int(trimmed.split()[-1])
        elif trimmed.startswith("[psw] Memory usage:"):
            value = float(trimmed.split()[-2])
            memory = max(memory, value)
    models = int(lines[-1].decode("UTF-8"))
    if memory == -1:
        memory = "unknown"
    return [time, width, models, memory]

# Perform all the actions for the pair (n, m), thread safe
def perform(n, m, runs):
    formula = construct_formula(n, m)
    # Create a temporary file, and run solver on it
    temp_file = temp_path/("temp-{}-{}.cnf".format(n, m))
    write_formula(formula, temp_file, n, m, [])
    times, widths, answers, memories = [], [], [], []
    for i in range(runs):
        try:
            time, width, answer, memory = run_solver(temp_file)
            print("n = {}, m = {}, i = {}: decomposition had ps-width {}, elapsed time was {}".format(n, m, i, width, time))
            times.append(time)
            widths.append(width)
            answers.append(answer)
            memories.append(memory)
        except RuntimeError as error:
            print("n = {}, m = {}, i = {}: runtime = {}, solver reported error: {}".format(n, m, i, error.args[1], error.args[0]))
            times.append(error.args[1])
            widths.append(error.args[2])
            answers.append("unknown")
            memories.append(error.args[3])
    print("n = {}, m = {}: ran solver {} times".format(n, m, runs))
    # Add to report
    report_lock.acquire()
    if n not in report:
        report[n] = {}
    if m not in report[n]:
        report[n][m] = {}
    report[n][m]['runtime'] = [str(x) for x in times]
    report[n][m]['ps-width'] = [str(x) for x in widths]
    report[n][m]['models'] = [str(x) for x in answers]
    report[n][m]['memory'] = [str(x) for x in memories]
    report_lock.release()
    # Save to instances folder
    path = instances_path/str(n)
    if not os.path.exists(path):
        os.makedirs(path)
    file_name = "formula-{}-{}.cnf".format(n, m)
    write_formula(formula, path/file_name, n, m, [])
    # Remove temporary file
    if os.path.isfile(temp_file):
        os.remove(temp_file)

# Writes results to report file
def write_report():
    if not os.path.exists(reports_path):
        os.makedirs(reports_path)
    for n in report:
        name = "report-{}.txt".format(n)
        with open(reports_path/name, "w") as file:
            for m in ms:
                if m not in report[n]:
                    continue
                file.write("n {} m {} ({} runs)\n".format(n, m, runs_per_pair))
                file.write("runtime {}\n".format(' '.join(report[n][m]['runtime'])))
                file.write("decomposition ps-width {}\n".format(' '.join(report[n][m]['ps-width'])))
                file.write("models {}\n".format(' '.join(report[n][m]['models'])))
                file.write("peak memory {}\n".format(' '.join(report[n][m]['memory'])))
                file.write("\n")

if __name__ == "__main__":
    # Create directories
    if not os.path.exists(temp_path):
        os.makedirs(temp_path)
    if not os.path.exists(instances_path):
        os.makedirs(instances_path)
    # Use a thread pool to submit jobs to
    pool = multiprocessing.pool.ThreadPool(processes = simultaneous_threads)
    # n is the amount of variables, m is the amount of clauses
    try:
        for n in ns:
            for m in ms:
                pool.apply_async(perform, args = (n, m, runs_per_pair))
        pool.close()
        pool.join()
        # Write report
        write_report()
        print("Wrote a report.")
        # Delete temporary folder
        if os.path.isdir(temp_path):
            os.rmdir(temp_path)
        print("Done.")
    except KeyboardInterrupt as exception:
        print(" KeyboardInterrupt")
        pool.terminate()
        print("Terminated.")
        write_report()
        print("Wrote a report.")
        exit(1)
