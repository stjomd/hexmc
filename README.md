# hexmc

hexmc is a #SAT solver implemented as part of my bachelor thesis at the Vienna University of Technology.
#SAT, or propositional model counting, is the problem of counting the amount of satisfying assignments, or models, of a propositional formula.
The submitted version is `v1.0.0`.

## Details

The implementation is based on two dynamic algorithms, one parameterized by the ps-width of the formula, and one parameterized by the clique-width of the formula's signed incidence graph.
However, the latter requires inputs for which, to my knowledge, there exist no computation methods yet, and therefore it is not used.
It is still included with the source code and can be run by setting `--alg=cw` for studying purposes, but this returns false answers at the time.


For more information on the underlying algorithms, refer to:
- *Sigve Hortemo Sæther, Jan Arne Telle, Martin Vatshelle:* Solving #SAT and MaxSAT by Dynamic Programming. J. Artif. Intell. Res. 54: 59-82 (2015)
- *Eldar Fischer, Johann A. Makowsky, Elena V. Ravve:* Counting truth assignments of formulas of bounded tree-width or clique-width. Discret. Appl. Math. 156(4): 511-529 (2008)
- *Neha Lodha, Sebastian Ordyniak, Stefan Szeider:* A SAT Approach to Branchwidth. ACM Trans. Comput. Log. 20(3): 15:1-15:24 (2019)
- *Marijn Heule, Stefan Szeider:* A SAT Approach to Clique-Width. ACM Trans. Comput. Log. 16(3): 24:1-24:27 (2015)

## Installation
You will need JDK 11 to compile the source code and run the solver.
Maven is used for build automation and comes as a wrapper with this project, and does not have to be additionally installed.

The following command clones the repository and compiles the source code:

```
git clone https://github.com/stjomd/hexmc.git && cd hexmc && ./build
```
It produces a `.jar` file in the `target` directory.
You can use the script `./hexmc` to run the solver.
This script should remain in the main directory.
If you wish to run the solver from other directories, it is recommended you create another script that runs `./hexmc`.

## Usage

```
./hexmc input [--help] [--version] [--alg {psw,cw}] [--carving] [--timeout SECONDS] [--verbose]
```
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Parameter | Description
| --: | :----
| `input` | The path to the DIMACS CNF file.
| `-h, --help` | Output usage, help information, and exit.
| `--version` | Output the current version and exit.
| `-a, --alg {psw,cw}` | The algorithm to use for model counting (parameterized by ps-width or clique-width).
| `-c, --carving` | Compute a carving decomposition to use in the dynamic algorithm parameterized by ps-width (often increases runtime significantly). By default uses a random decomposition.
| `-t, --timeout SECONDS` | The timeout (in seconds).
| `-v, --verbose` | Output additional information to the console.

## Input Format

hexmc uses the DIMACS format for CNF formulas with slight modifications (weaker requirements).
```
p cnf 7 3
c Comment line
1 2 -3 -4 0
3 -4 5 0
-4 6 7 0
```
The header line `p cnf n m` states that the formula has at most `n` variables and at most `m` clauses.
It is optional, and the values `n` and `m` are inferred if it is not present.
Any line starting with `c` is a comment line, which is ignored by the parser.
Any other line represents a clause, with literals being non-zero integers separated by spaces, and negative signs denoting negation.
Clause lines must be terminated by `0` (zero).

## Limitations

- No optimization for efficiency.
hexmc was easily beaten by even older #SAT solvers on all inputs in our tests, and is not suitable for practical use.
- Computation with the `cw` algorithm is incomplete, due to lack of computation of signed parse trees (unsigned parse trees are calculated instead at the time), and returns false answers.
- Decompositions are calculated using SAT encodings, which is a major bottleneck due to the encoding size.
Because of this, for the `psw` algorithm, a quick, non-optimal decomposition is computed.
- In the computation of unsigned parse trees, the recoloring nodes are currently determined using brute force, not in polynomial time.
- Big number libraries were not used, thus `long` overflows can only be avoided on inputs with at most 63 variables.
hexmc does detect overflows and exits with an error should one occur.
