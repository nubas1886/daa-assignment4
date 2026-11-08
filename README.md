# City Search & Spell-Check — DAA Assignment 4

Edit Distance (recursive, table, two rows), Naive and KMP string search, counters, a seeded experiment, tests, plots and a report.

GitHub repository (branch `main`, tag `v1.0`): <ADD LINK HERE>

## Layout

```
src/main/java/daa/   EditDistance, Recursive/Table/TwoRowEditDistance, StringSearch, NaiveSearch, KmpSearch, Generator, Experiment
src/test/java/daa/   EditDistanceTest, SearchTest (JUnit 5)
scripts/plots.py     draws the plots from results.csv
results/results.csv  all runs of the experiment
results/plots/       p1..p5 PNG plots
REPORT.md            Experiment Design Card, plots, prediction vs result, short answers
```

## Build and test (one command)

```
mvn test
```
Requires JDK 17+ and Maven. Tests: reference values (section 7 of the task), empty strings, 100 random strings for the three Edit Distance versions, LPS arrays, overlapping matches, 100 random texts for Naive vs KMP, empty pattern, pattern longer than the text.

## Run the experiment

```
mvn -q compile exec:java            # writes results/results.csv (about 35 s)
python3 scripts/plots.py            # needs matplotlib; writes results/plots/*.png
```
Without Maven:
```
javac -d target/classes src/main/java/daa/*.java
java -cp target/classes daa.Experiment results/results.csv
```
The generator uses fixed seeds, so counters (`ops`, `mem_cells`) and result values are identical on every run; only `time_ms` varies a little.

### results.csv columns
`family, algorithm, input, n, m, result, runs, time_ms, ops, mem_cells` — `result` is the distance or the number of matches (checked against the other algorithms on every row), `ops` is counted inside the algorithm (recursive calls / DP cells / character comparisons), `mem_cells` is the number of int cells allocated by the algorithm, `time_ms` is the median of `runs` runs.

## AI use acknowledgement

This project (source code, JUnit tests, experiment runner, plotting script, results and report) was produced with the AI assistant Claude (Anthropic) at the author's request. The experiment was really run and all numbers in `results.csv` and `REPORT.md` come from that run. The author is responsible for the submission and should be able to explain the code and the report.

<!-- Author: add here, in your own words, what you personally checked, changed or ran. -->
