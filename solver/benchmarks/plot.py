import os
import pathlib
import math

import matplotlib.pyplot as plt
import numpy as np

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
    plt.colorbar(heatmap, label = "log(k)", location = 'top')
    # Limits
    plt.ylim(2, 30)
    plt.xlim(1, 100)
    plt.xlabel('m')
    plt.ylabel('n')
    # Save
    plt.savefig(graphics_dir/save_name, bbox_inches = 'tight')

def runtime_of_clauses(instances_folder, fixed_ns, max_clauses, save_name, xticks = None):
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
                for line in lines:
                    if line.startswith("c time:"):
                        time = timestr_to_seconds(line.split()[-1])
                # Write to data
                if n not in data:
                    data[n] = {}
                if m not in data[n]:
                    data[n][m] = []
                data[n][m].append(time)
    # Reduce the array of values to a single value
    fig, ax = plt.subplots(1, 1)
    for n in fixed_ns:
        table = np.zeros((max_clauses+1,))
        table.fill(np.nan)
        for m in range(1, max_clauses+1):
            table[m] = sum(data[n][m])/len(data[n][m])
        plt.plot(np.arange(max_clauses+1), table, label = 'n = {}'.format(n), marker = '.')
    plt.legend(loc = 'upper left')
    plt.xlabel('m')
    plt.ylabel('average runtime (seconds)')
    if xticks != None:
        ax.set_xticks(xticks)
    # Save
    plt.savefig(graphics_dir/save_name)

def runtime_of_variables(instances_folder, fixed_ms, max_variables, save_name, xticks = None):
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
                for line in lines:
                    if line.startswith("c time:"):
                        time = timestr_to_seconds(line.split()[-1])
                # Write to data
                if n not in data:
                    data[n] = {}
                if m not in data[n]:
                    data[n][m] = []
                data[n][m].append(time)
    # Reduce the array of values to a single value
    fig, ax = plt.subplots(1, 1)
    for m in fixed_ms:
        table = np.zeros((max_variables + 1,))
        table.fill(np.nan)
        for n in range(2, max_variables + 1):
            table[n] = sum(data[n][m])/len(data[n][m])
        plt.plot(np.arange(max_variables + 1), table, label = 'm = {}'.format(m), marker = '.')
    plt.legend(loc = 'upper left')
    plt.xlabel('n')
    plt.ylabel('average runtime (seconds)')
    if xticks != None:
        ax.set_xticks(xticks)
    # Save
    plt.savefig(graphics_dir/save_name)

def runtime_of_psw(instances_folder, save_name):
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
                for line in lines:
                    if line.startswith("c time:"):
                        time = timestr_to_seconds(line.split()[-1])
                # Write to data
                psw = int(subdir)
                if psw not in data:
                    data[psw] = []
                data[psw].append(time)
    y = np.full((max_psw+1,), np.nan)
    # Fill table
    for k in data:
        y[k] = sum(data[k])/len(data[k])
    fig, ax = plt.subplots(1, 1)
    plt.scatter(np.arange(max_psw+1), y, marker = '.')
    plt.xlabel('k')
    plt.ylabel('average runtime (seconds)')
    ax.set_xticks([2, 500, 1000, 1500, 2000, 2500, 3000])
    # Save
    plt.savefig(graphics_dir/save_name)

if __name__ == "__main__":
    # Call one function at a time
    # psw_heatmap(mdir/"instances", "heat_k_from_nm.pdf")
    # runtime_of_clauses(mdir/"instances", [20, 25, 30], 100, "runtime_of_clauses.pdf", xticks = [1, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100])
    # runtime_of_variables(mdir/"instances", [50, 60, 75, 100], 30, "runtime_of_variables.pdf", xticks = [2, 5, 10, 15, 20, 25, 30])
    runtime_of_psw(mdir/"instances", "runtime_of_psw.pdf")
