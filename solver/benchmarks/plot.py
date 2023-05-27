import enum
import math
import os
import pathlib

import matplotlib.pyplot as plt
import numpy as np

class DataType(enum.Enum):
    runtime = 1
    memory = 2

mdir = pathlib.Path(__file__).parent
graphics_dir = mdir/"graphics"

def timestr_to_seconds(string):
    units = [float(x) for x in string.split(":")]
    return 3600*units[0] + 60*units[1] + units[2]

def psw_heatmap(instances_folder, save_name):
    indices = {}
    # In data[n][m], store the ps-widths
    data = np.zeros((31, 101, 5))
    for subdir in os.listdir(instances_folder):
        if subdir == ".DS_Store":
            continue
        # subdir: ps-width (no failed instances in this set)
        for instance in os.listdir(instances_folder/subdir):
            if not instance.endswith(".cnf"):
                continue
            with open(instances_folder/subdir/instance, "r") as instance_file:
                header = instance_file.readlines()[0].split()
                n, m = int(header[2]), int(header[3])
                if n not in indices:
                    indices[n] = {}
                if m not in indices[n]:
                    indices[n][m] = 0
                indices[n][m] += 1
                data[n, m, indices[n][m] - 1] = int(subdir)
    # Reduce the array of ps-widths to a single value
    table = np.zeros((31, 101))
    table.fill(np.nan)
    for x in range(31):
        for y in range(101):
            if x < 2 or y < 1:
                table[x, y] = 0 # placeholder, not plotted
            else:
                table[x, y] = math.log(data[x, y, 2]) # log of median
    # Use matplotlib to make a plot
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

def data_of_clauses(instances_folder, fixed_ns, max_clauses, save_name, xticks = None, data_type = DataType.runtime):
    data = {}
    for subdir in os.listdir(instances_folder):
        if subdir == ".DS_Store":
            continue
        # subdir: ps-width (no failed instances in this set)
        for instance in os.listdir(instances_folder/subdir):
            if not instance.endswith(".cnf"):
                continue
            with open(instances_folder/subdir/instance, "r") as instance_file:
                lines = instance_file.readlines()
                header = lines[0].split()
                n, m = int(header[2]), int(header[3])
                time = 0.0
                memory = 0.0
                for line in lines:
                    if line.startswith("c time:"):
                        time = timestr_to_seconds(line.split()[-1])
                    elif line.startswith("c peak memory usage:"):
                        memory = float(line.split()[-2])
                # Write to data
                if n not in data:
                    data[n] = {}
                if m not in data[n]:
                    data[n][m] = []
                if data_type == DataType.runtime:
                    data[n][m].append(time)
                elif data_type == DataType.memory:
                    data[n][m].append(memory)
    # Reduce the array of values to a single value
    fig, ax = plt.subplots(1, 1)
    for n in fixed_ns:
        table = np.zeros((max_clauses+1,))
        table.fill(np.nan)
        for m in range(1, max_clauses+1):
            table[m] = sum(data[n][m])/len(data[n][m]) # average
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

def data_of_variables(instances_folder, fixed_ms, max_variables, save_name, xticks = None, data_type = DataType.runtime):
    data = {}
    for subdir in os.listdir(instances_folder):
        if subdir == ".DS_Store":
            continue
        # subdir: ps-width (no failed instances in this set)
        for instance in os.listdir(instances_folder/subdir):
            if not instance.endswith(".cnf"):
                continue
            with open(instances_folder/subdir/instance, "r") as instance_file:
                lines = instance_file.readlines()
                header = lines[0].split()
                n, m = int(header[2]), int(header[3])
                time = 0.0
                memory = 0.0
                for line in lines:
                    if line.startswith("c time:"):
                        time = timestr_to_seconds(line.split()[-1])
                    elif line.startswith("c peak memory usage:"):
                        memory = float(line.split()[-2])
                # Write to data
                if n not in data:
                    data[n] = {}
                if m not in data[n]:
                    data[n][m] = []
                if data_type == DataType.runtime:
                    data[n][m].append(time)
                elif data_type == DataType.memory:
                    data[n][m].append(memory)
    # Reduce the array of values to a single value
    fig, ax = plt.subplots(1, 1)
    for m in fixed_ms:
        table = np.zeros((max_variables + 1,))
        table.fill(np.nan)
        for n in range(2, max_variables + 1):
            table[n] = sum(data[n][m])/len(data[n][m])
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

def data_of_psw(instances_folder, save_name, xticks = None, data_type = DataType.runtime):
    data = {}
    max_psw = 0
    for subdir in os.listdir(instances_folder):
        if subdir == ".DS_Store":
            continue
        elif subdir == "5099": # exclude outlier
            continue
        if subdir.isdigit():
            max_psw = max(max_psw, int(subdir))
        # subdir: ps-width (no failed instances in this set)
        for instance in os.listdir(instances_folder/subdir):
            if not instance.endswith(".cnf"):
                continue
            with open(instances_folder/subdir/instance, "r") as instance_file:
                lines = instance_file.readlines()
                header = lines[0].split()
                n, m = int(header[2]), int(header[3])
                time = 0.0
                memory = 0.0
                for line in lines:
                    if line.startswith("c time:"):
                        time = timestr_to_seconds(line.split()[-1])
                    elif line.startswith("c peak memory usage:"):
                        memory = float(line.split()[-2])
                # Write to data
                psw = int(subdir)
                if psw not in data:
                    data[psw] = []
                if data_type == DataType.runtime:
                    data[psw].append(time)
                elif data_type == DataType.memory:
                    data[psw].append(memory)
    y = np.full((max_psw+1,), np.nan)
    # Fill table
    for k in data:
        y[k] = sum(data[k])/len(data[k])
    fig, ax = plt.subplots(1, 1)
    plt.scatter(np.arange(max_psw+1), y, marker = '.')
    plt.xlabel(r'$k$')
    if data_type == DataType.runtime:
        plt.ylabel('average runtime (seconds)')
    elif data_type == DataType.memory:
        plt.ylabel('average peak memory usage (GiB)')
    if xticks != None:
        ax.set_xticks(xticks)
    # Save
    plt.savefig(graphics_dir/save_name)

if __name__ == "__main__":
    # Use LaTeX font
    plt.rcParams['text.usetex'] = True
    plt.rcParams['mathtext.fontset'] = 'stix'
    plt.rcParams['font.family'] = 'STIXGeneral'

    # Call one function at a time

    # ----- set: instances; runtime -----
    # psw_heatmap(mdir/"instances", "heat_k_from_nm.pdf")
    # data_of_clauses(mdir/"instances", [20, 25, 30], 100, "runtime_of_clauses.pdf", xticks = [1, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100])
    # data_of_variables(mdir/"instances", [50, 60, 75, 100], 30, "runtime_of_variables.pdf", xticks = [2, 5, 10, 15, 20, 25, 30])
    # data_of_psw(mdir/"instances", "runtime_of_psw.pdf", xticks = [2, 500, 1000, 1500, 2000, 2500, 3000])
    # ----- set: instances; memory -----
    # data_of_clauses(mdir/"instances", [20, 25, 30], 100, "memory_of_clauses.pdf", data_type = DataType.memory, xticks = [1, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100])
    # data_of_variables(mdir/"instances", [50, 60, 75, 100], 30, "memory_of_variables.pdf", data_type = DataType.memory, xticks = [2, 5, 10, 15, 20, 25, 30])
    # data_of_psw(mdir/"instances", "memory_of_psw.pdf", data_type = DataType.memory, xticks = [2, 500, 1000, 1500, 2000, 2500, 3000])

    # ----- set: instances-n63; runtime -----
    # data_of_clauses(mdir/"instances-n63", [63], 50, "runtime_of_clauses-n63.pdf", xticks = [1, 10, 20, 30, 40, 50])
    # data_of_psw(mdir/"instances-n63", "runtime_of_psw-n63.pdf", xticks = [2, 200, 400, 600, 800, 1000, 1200, 1400])
    # ----- set: instances-n63; memory -----
    # data_of_clauses(mdir/"instances-n63", [63], 50, "memory_of_clauses-n63.pdf", data_type = DataType.memory, xticks = [1, 10, 20, 30, 40, 50])
    # data_of_psw(mdir/"instances-n63", "memory_of_psw-n63.pdf", data_type = DataType.memory, xticks = [2, 200, 400, 600, 800, 1000, 1200, 1400])
