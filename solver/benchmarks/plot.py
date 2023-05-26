import os
import pathlib
import math

import matplotlib.pyplot as plt
import numpy as np

mdir = pathlib.Path(__file__).parent
graphics_dir = mdir/"graphics"

def psw_heatmap(instances_folder):
    indices = {}
    # In data[n][m], store the ps-widths
    data = np.zeros((31, 101, 5))
    for subdir in os.listdir(instances_folder):
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
    heatmap = plt.imshow(table, cmap='viridis', aspect='auto')
    # Add single ticks to left bottom for 1/2
    xt = ax.get_xticks() 
    xt = np.append(xt, 1)
    yt = ax.get_yticks() 
    yt = np.append(yt, 2)
    ax.set_xticks(xt)
    ax.set_yticks(yt)
    # Add legend/colorbar
    plt.colorbar(heatmap, label = "log(k)")
    # Limits
    plt.ylim(2, 30)
    plt.xlim(1, 100)
    plt.xlabel('m')
    plt.ylabel('n')
    # Save
    plt.savefig(graphics_dir/"heat_k_from_nm.pdf")

if __name__ == "__main__":
    psw_heatmap(mdir/"instances")
