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

def psw_heatmap(instances_folder):
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
    plt.savefig(graphics_dir/"heat_k_from_nm.pdf", bbox_inches = 'tight')

def runtime_of_clauses(instances_folder):
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
    # Reduce the array of ps-widths to a single value
    for n in [20, 25, 30]:
        table = np.zeros((101,))
        for m in range(1, 101):
            table[m] = sum(data[n][m])/len(data[n][m])
        plt.plot(np.arange(101), table, label = 'n = {}'.format(n))
    plt.legend(loc = 'upper left')
    plt.xlabel('m')
    plt.ylabel('average runtime (seconds)')
    # Save
    plt.savefig(graphics_dir/"runtime_of_clauses.pdf")

def runtime_of_variables(instances_folder):
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
    # Reduce the array of ps-widths to a single value
    for m in [50, 60, 75, 100]:
        table = np.zeros((31,))
        for n in range(2, 31):
            table[n] = sum(data[n][m])/len(data[n][m])
        plt.plot(np.arange(31), table, label = 'm = {}'.format(m))
    plt.legend(loc = 'upper left')
    plt.xlabel('n')
    plt.ylabel('average runtime (seconds)')
    # Save
    plt.savefig(graphics_dir/"runtime_of_variables.pdf")

def runtime_of_psw(instances_folder):
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
    y = np.full((max_psw+1,), -1000)
    # Reduce the array of ps-widths to a single value
    for k in data:
        y[k] = sum(data[k])/len(data[k])
    plt.scatter(np.arange(max_psw+1), y, marker = '.')
    plt.xlabel('k')
    plt.ylabel('average runtime (seconds)')
    plt.ylim(-250,)
    # Save
    plt.savefig(graphics_dir/"runtime_of_psw.pdf")

if __name__ == "__main__":
    # Call one function at a time
    # psw_heatmap(mdir/"instances")
    # runtime_of_clauses(mdir/"instances")
    # runtime_of_variables(mdir/"instances")
    runtime_of_psw(mdir/"instances")
