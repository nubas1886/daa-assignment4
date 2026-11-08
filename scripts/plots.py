"""Draws the plots from results/results.csv into results/plots/. Usage: python3 scripts/plots.py"""
import csv
import os
from collections import defaultdict

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
rows = list(csv.DictReader(open(os.path.join(ROOT, "results", "results.csv"))))
for r in rows:
    for k in ("n", "m", "result", "runs", "ops", "mem_cells"):
        r[k] = int(r[k])
    r["time_ms"] = float(r["time_ms"])
OUT = os.path.join(ROOT, "results", "plots")
os.makedirs(OUT, exist_ok=True)


def series(algo, inp, key):
    pts = sorted((r["n"], r[key]) for r in rows if r["algorithm"] == algo and r["input"] == inp)
    return [p[0] for p in pts], [p[1] for p in pts]


def finish(fig, name, title, caption):
    fig.suptitle(title, fontsize=12, fontweight="bold")
    fig.text(0.5, 0.01, caption, ha="center", fontsize=9, style="italic", wrap=True)
    fig.tight_layout(rect=(0, 0.06, 1, 0.95))
    fig.savefig(os.path.join(OUT, name), dpi=150)
    plt.close(fig)


# P1: recursion calls, log y
fig, ax = plt.subplots(figsize=(7, 4.6))
for inp, mk in (("disjoint", "o-"), ("random", "s-"), ("identical", "^-")):
    x, y = series("edit_recursive", inp, "ops")
    ax.plot(x, y, mk, label=inp)
ax.set_yscale("log")
ax.set_xlabel("string length n (characters, both strings)")
ax.set_ylabel("recursive calls (count, log scale)")
ax.grid(True, which="both", alpha=0.3)
ax.legend(title="input type")
finish(fig, "p1_recursive_calls.png", "Recursive Edit Distance: number of calls vs n",
       "Notice: on a log axis 'disjoint' and 'random' are straight lines (exponential growth); 'identical' stays tiny.")

# P2: table vs two rows, time and ops, log-log (input random)
fig, axs = plt.subplots(1, 2, figsize=(10, 4.6))
for algo, mk in (("edit_table", "o-"), ("edit_two_rows", "s--")):
    x, y = series(algo, "random", "ops")
    axs[0].plot(x, y, mk, label=algo)
    x, y = series(algo, "random", "time_ms")
    axs[1].plot(x, y, mk, label=algo)
for ax, yl in zip(axs, ("DP cells computed (count)", "time (ms, median of 5 runs)")):
    ax.set_xscale("log"); ax.set_yscale("log")
    ax.set_xlabel("string length n (characters, both strings)")
    ax.set_ylabel(yl)
    ax.grid(True, which="both", alpha=0.3)
    ax.legend()
axs[0].set_title("Counter"); axs[1].set_title("Clock")
finish(fig, "p2_table_vs_two_rows.png", "Table vs two rows (random input): cost vs n",
       "Notice: both versions compute exactly n·n cells (same line, slope 2); the two-row version is a bit faster on the clock.")

# P3: memory cells
fig, ax = plt.subplots(figsize=(7, 4.6))
for algo, mk in (("edit_table", "o-"), ("edit_two_rows", "s-")):
    x, y = series(algo, "random", "mem_cells")
    ax.plot(x, y, mk, label=algo)
ax.set_xscale("log"); ax.set_yscale("log")
ax.set_xlabel("string length n (characters, both strings)")
ax.set_ylabel("int cells allocated (count, log scale)")
ax.grid(True, which="both", alpha=0.3)
ax.legend()
finish(fig, "p3_memory_cells.png", "Memory of the DP versions vs n",
       "Notice: the table grows with slope 2 (O(n·m)), two rows with slope 1 (O(m)); at n=4000 the gap is about 2000x.")

# P4: comparisons per text character
inputs = ["random26", "random_binary", "worst_naive", "all_match"]
fig, ax = plt.subplots(figsize=(8, 4.8))
w = 0.38
for k, (algo, color) in enumerate((("search_naive", "tab:red"), ("search_kmp", "tab:blue"))):
    vals = []
    for inp in inputs:
        r = [r for r in rows if r["algorithm"] == algo and r["input"] == inp and r["n"] == 1_600_000][0]
        vals.append(r["ops"] / r["n"])
    bars = ax.bar([i + (k - 0.5) * w for i in range(len(inputs))], vals, w, label=algo, color=color)
    for b, v in zip(bars, vals):
        ax.text(b.get_x() + b.get_width() / 2, v * 1.08, f"{v:.1f}", ha="center", fontsize=8)
ax.set_yscale("log")
ax.set_xticks(range(len(inputs))); ax.set_xticklabels(inputs)
ax.set_xlabel("input type (text length n = 1 600 000, pattern length m = 100)")
ax.set_ylabel("character comparisons per text character (ratio, log scale)")
ax.grid(True, axis="y", which="both", alpha=0.3)
ax.legend()
finish(fig, "p4_comparisons_per_char.png", "String search: comparisons per text character",
       "Notice: KMP stays between 1 and 2 on every input; Naive reaches about 100 on worst_naive and all_match.")

# P5: search time log-log
fig, axs = plt.subplots(1, 2, figsize=(10, 4.6), sharey=True)
for ax, inp in zip(axs, ("worst_naive", "random26")):
    for algo, mk in (("search_naive", "o-"), ("search_kmp", "s-")):
        x, y = series(algo, inp, "time_ms")
        ax.plot(x, y, mk, label=algo)
    ax.set_xscale("log"); ax.set_yscale("log")
    ax.set_title(f"input: {inp}")
    ax.set_xlabel("text length n (characters)")
    ax.grid(True, which="both", alpha=0.3)
    ax.legend()
axs[0].set_ylabel("time (ms, median of 5 runs, log scale)")
finish(fig, "p5_search_time.png", "String search: time vs text length (pattern length 100)",
       "Notice: on worst_naive Naive is far above KMP and both grow linearly in n; on random26 they are close (KMP not faster).")
print("plots written to", OUT)
