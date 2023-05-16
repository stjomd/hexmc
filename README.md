# bachelor

This repository contains the source code for my bachelor thesis that I wrote at the Vienna University of Technology in the winter semester of 2022 and the summer semester of 2023.
This includes the LaTeX files as well as Java code.

In my bachelor thesis, I studied FPT algorithms for the problem #SAT (propositional model counting). The goal was then to implement a simple #SAT solver using these algorithms.

## solver

The solver utilizes a dynamic algorithm w.r.t. the ps-width of a formula. I also attempted to implement an algorithm w.r.t. the clique-width of the formula's incidence graph, however there was/is no algorithm known to me at the time that would compute the necessary inputs for it.
The Java code includes this implementation too, and it can be run by setting `--alg=cw`.
This, of course, will return false answers, but might be useful in case anyone wants to complete the implementation.
Note that this solver was not optimized for efficiency, but rather for readability of the source code, and will require extremely long times and/or will run out of memory for larger formulas.

For more information on the underlying algorithms, refer to:
- *Sigve Hortemo Sæther, Jan Arne Telle, Martin Vatshelle:* Solving #SAT and MaxSAT by Dynamic Programming. J. Artif. Intell. Res. 54: 59-82 (2015)
- *Eldar Fischer, Johann A. Makowsky, Elena V. Ravve:* Counting truth assignments of formulas of bounded tree-width or clique-width. Discret. Appl. Math. 156(4): 511-529 (2008)
- *Neha Lodha, Sebastian Ordyniak, Stefan Szeider:* A SAT Approach to Branchwidth. ACM Trans. Comput. Log. 20(3): 15:1-15:24 (2019)
- *Marijn Heule, Stefan Szeider:* A SAT Approach to Clique-Width. ACM Trans. Comput. Log. 16(3): 24:1-24:27 (2015)

The source code for the #SAT solver is located in the [solver directory](solver/). You will need Maven 4.x and JDK 11 to build and run the solver.

### Usage

```
solver input [--help] [--version] [--alg {psw,cw}] [--timeout SECONDS] [--verbose]
```
| Parameter | Description
| --: | :----
| `input` | The path to the DIMACS CNF file.
| `-h, --help` | Output usage, help information, and exit.
| `--version` | Output the current version.
| `-a, --alg {psw,cw}` | The algorithm to use for model counting (ps-width or clique-width).
| `-t, --timeout SECONDS` | The timeout (in seconds) for the SAT solver – effective when computing decompositions.
| `-v, --verbose` | Output additional information to the console.
