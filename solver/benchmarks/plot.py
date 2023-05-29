import enum
import math
import os
import pathlib

import matplotlib.pyplot as plt
import numpy as np

main_dir = pathlib.Path(__file__).parent
graphics_dir = main_dir/"graphics"

# ----- Helpers -------------------------------------------------

class DataType(enum.Enum):
    runtime = 1
    memory = 2

class SolverRun:
    n, m = None, None
    runtime = None
    width = None
    models = None
    memory = None
    error = None
    def __init__(self, n, m):
        self.n = n
        self.m = m

def timestr_to_seconds(string):
    units = [float(x) for x in string.split(":")]
    return 3600*units[0] + 60*units[1] + units[2]

# Parses the reports in the specified folders, calls action for each (n, m)
# action(n, m, runs): n -- #variables, m -- #clauses, runs -- array with the data
def parse_reports(reports_path, action):
    for report in os.listdir(reports_path):
        if not (report.startswith("report-") and report.endswith(".txt")):
            continue
        current_n = int(report.split('-')[1][:-4])
        with open(reports_path/report, "r") as file:
            current_m = 0
            runs = [] # runs data for (current_n, current_m)
            for line in file.readlines():
                items = line.split()
                if line.startswith("n ") and line.endswith("runs)\n"):
                    # New data line
                    runs = []
                    current_m = int(items[3])
                elif line.startswith("runtime"):
                    # Runtime line
                    for runtime in items[1:]:
                        run = SolverRun(current_n, current_m)
                        run.runtime = timestr_to_seconds(runtime)
                        runs.append(run)
                elif line.startswith("decomposition ps-width"):
                    # Decomposition ps-width line
                    i = 0
                    for width in items[2:]:
                        if width != "unknown":
                            runs[i].width = int(width)
                        i += 1
                elif line.startswith("models"):
                    # Model amount line
                    i = 0
                    for models in items[1:]:
                        if models != "unknown":
                            runs[i].models = int(models)
                        i += 1
                elif line.startswith("peak memory"):
                    # Peak memory usage line
                    i = 0
                    for memory in items[2:]:
                        if memory != "unknown":
                            runs[i].memory = float(memory)
                        i += 1
                elif line.startswith("\trun "):
                    # Error message line
                    run_number = int(items[1][:-1])
                    message = ' '.join(items[2:])
                    runs[run_number - 1].error = message
                elif line == "\n":
                    # Empty line, parsing (n, m) finished
                    if len(runs) > 0:
                        action(current_n, current_m, runs)
                    runs = []

# ----- Plots -------------------------------------------------

def psw_heatmap(reports_path, save_name):
    # Define function passed into parse_reports
    table = np.zeros((31, 101))
    table.fill(np.nan)
    def fill_table(n, m, runs):
        widths = [run.width for run in runs]
        average = math.log(sum(widths) / len(widths))
        table[n, m] = average
    # Parse reports
    parse_reports(reports_path, fill_table)
    # Matplotlib
    fig, ax = plt.subplots(1, 1)
    heatmap = plt.imshow(table, cmap='viridis')
    # Add single ticks to left bottom for 1/2
    xt = ax.get_xticks() 
    xt = np.append(xt, [1, 10, 30, 50, 70, 90])
    yt = ax.get_yticks() 
    yt = np.append(yt, 2)
    ax.set_xticks(xt)
    ax.set_yticks(yt)
    # Add legend/colorbar
    plt.colorbar(heatmap, label = r"$\log(k)$", location = 'top')
    # Limits
    plt.ylim(1.5, 30.5)
    plt.xlim(0.5, 100.5)
    plt.xlabel(r'$m$')
    plt.ylabel(r'$n$', rotation = 0, labelpad = 10)
    # Save
    plt.savefig(graphics_dir/save_name, bbox_inches = 'tight')

def data_of_clauses(reports_path, fixed_ns, max_clauses, save_name, xticks = None, data_type = DataType.runtime):
    # Define function passed into parse_reports
    data = {}
    def fill_table(n, m, runs):
        values = []
        if data_type == DataType.runtime:
            values = [run.runtime for run in runs]
        elif data_type == DataType.memory:
            values = [run.memory for run in runs]
        average = sum(values) / len(values)
        if n not in data:
            data[n] = {}
        if m not in data[n]:
            data[n][m] = 0
        data[n][m] = average
    # Parse reports
    parse_reports(reports_path, fill_table)
    # Matplotlib
    fig, ax = plt.subplots(1, 1)
    for n in fixed_ns:
        table = np.zeros((max_clauses+1,))
        table.fill(np.nan)
        for m in range(1, max_clauses+1):
            table[m] = data[n][m]
        plt.plot(np.arange(max_clauses+1), table, label = r'$n$ = {}'.format(n), marker = '.')
    plt.legend(loc = 'upper left')
    plt.xlabel(r'$m$')
    if data_type == DataType.runtime:
        plt.ylabel('average runtime (seconds)')
    elif data_type == DataType.memory:
        plt.ylabel('average peak memory usage (GiB)')
    if xticks != None:
        ax.set_xticks(xticks)
    # Save
    plt.savefig(graphics_dir/save_name)

def data_of_variables(reports_path, fixed_ms, max_variables, save_name, xticks = None, data_type = DataType.runtime):
    # Define function passed into parse_reports
    data = {}
    def fill_table(n, m, runs):
        values = []
        if data_type == DataType.runtime:
            values = [run.runtime for run in runs]
        elif data_type == DataType.memory:
            values = [run.memory for run in runs]
        average = sum(values) / len(values)
        if n not in data:
            data[n] = {}
        if m not in data[n]:
            data[n][m] = 0
        data[n][m] = average
    # Parse reports
    parse_reports(reports_path, fill_table)
    # Matplotlib
    fig, ax = plt.subplots(1, 1)
    for m in fixed_ms:
        table = np.zeros((max_variables + 1,))
        table.fill(np.nan)
        for n in range(2, max_variables + 1):
            table[n] = data[n][m]
        plt.plot(np.arange(max_variables + 1), table, label = r'$m$ = {}'.format(m), marker = '.')
    plt.legend(loc = 'upper left')
    plt.xlabel(r'$n$')
    if data_type == DataType.runtime:
        plt.ylabel('average runtime (seconds)')
    elif data_type == DataType.memory:
        plt.ylabel('average peak memory usage (GiB)')
    if xticks != None:
        ax.set_xticks(xticks)
    # Save
    plt.savefig(graphics_dir/save_name)

def data_of_psw(reports_path, save_name, xticks = None, data_type = DataType.runtime):
    # Define function passed into parse_reports
    data = {}
    def fill_table(n, m, runs):
        values = []
        widths = [run.width for run in runs]
        if data_type == DataType.runtime:
            values = [run.runtime for run in runs]
        elif data_type == DataType.memory:
            values = [run.memory for run in runs]
        for i in range(len(runs)):
            k, value = widths[i], values[i]
            if k not in data:
                data[k] = []
            data[k].append(value)
    # Parse reports
    parse_reports(reports_path, fill_table)
    # Determine max ps-width
    max_psw = 0
    for k in data:
        max_psw = max(max_psw, k)
    # Matplotlib
    table = np.full((max_psw+1,), np.nan)
    for k in data:
        table[k] = sum(data[k]) / len(data[k])
    fig, ax = plt.subplots(1, 1)
    plt.scatter(np.arange(max_psw+1), table, marker = '.')
    plt.xlabel(r'$k$')
    if data_type == DataType.runtime:
        plt.ylabel('average runtime (seconds)')
    elif data_type == DataType.memory:
        plt.ylabel('average peak memory usage (GiB)')
    if xticks != None:
        ax.set_xticks(xticks)
    # Save
    plt.savefig(graphics_dir/save_name)

# ----- Main -------------------------------------------------

if __name__ == "__main__":
    # Use LaTeX font
    plt.rcParams['text.usetex'] = True
    plt.rcParams['mathtext.fontset'] = 'stix'
    plt.rcParams['font.family'] = 'STIXGeneral'

    # Call one function at a time

    # ----- set: bm; runtime -----
    # psw_heatmap(main_dir/"reports", "heat_k_from_nm.pdf")
    # data_of_clauses(main_dir/"reports", [20, 25, 30], 100, "runtime_of_clauses.pdf", xticks = [1, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100])
    # data_of_variables(main_dir/"reports", [50, 60, 75, 100], 30, "runtime_of_variables.pdf", xticks = [2, 5, 10, 15, 20, 25, 30])
    # data_of_psw(main_dir/"reports", "runtime_of_psw.pdf", xticks = [2, 500, 1000, 1500, 2000, 2500, 3000])
    # ----- set: bm; memory -----
    # data_of_clauses(main_dir/"reports", [20, 25, 30], 100, "memory_of_clauses.pdf", data_type = DataType.memory, xticks = [1, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100])
    # data_of_variables(main_dir/"reports", [50, 60, 75, 100], 30, "memory_of_variables.pdf", data_type = DataType.memory, xticks = [2, 5, 10, 15, 20, 25, 30])
    # data_of_psw(main_dir/"reports", "memory_of_psw.pdf", data_type = DataType.memory, xticks = [2, 500, 1000, 1500, 2000, 2500, 3000])

    # ----- set: bm-n-63; runtime -----
    # data_of_clauses(main_dir/"reports-n63", [63], 50, "runtime_of_clauses-n63.pdf", xticks = [1, 10, 20, 30, 40, 50])
    # data_of_psw(main_dir/"reports-n63", "runtime_of_psw-n63.pdf", xticks = [2, 200, 400, 600, 800, 1000, 1200, 1400])
    # ----- set: bm-n-63; memory -----
    # data_of_clauses(main_dir/"reports-n63", [63], 50, "memory_of_clauses-n63.pdf", data_type = DataType.memory, xticks = [1, 10, 20, 30, 40, 50])
    # data_of_psw(main_dir/"reports-n63", "memory_of_psw-n63.pdf", data_type = DataType.memory, xticks = [2, 200, 400, 600, 800, 1000, 1200, 1400])
