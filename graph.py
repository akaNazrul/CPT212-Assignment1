"""
CPT212 Assignment 1 - Graph Analysis
Plots primitive operation counts for Simple Multiplication vs Karatsuba.
Run AFTER compiling and running Main.java which produces output/mult_ops.csv
"""

import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import os

os.makedirs("output_plot", exist_ok=True)

# Load data
df = pd.read_csv("output/mult_ops.csv")

# ---- Graph 1: Simple Multiplication alone ----
plt.figure(figsize=(8, 5))
plt.plot(df["n"], df["simpleOps"], marker="o", color="#1f77b4", label="Simple Multiplication")
# Fit a quadratic curve (O(n^2) expected)
coeffs = np.polyfit(df["n"], df["simpleOps"], 2)
poly   = np.poly1d(coeffs)
x_fit  = np.linspace(df["n"].min(), df["n"].max(), 300)
plt.plot(x_fit, poly(x_fit), "--", color="red", label=f"O(n²) fit: {coeffs[0]:.2f}n²+…")
plt.title("Simple Multiplication: Primitive Operations vs Input Size (n)")
plt.xlabel("Input Size (n digits)")
plt.ylabel("Primitive Operations")
plt.legend()
plt.grid(True)
plt.tight_layout()
plt.savefig("output_plot/simple_mult_ops.png", dpi=300, bbox_inches="tight")
plt.show()

# ---- Graph 2: Karatsuba alone ----
plt.figure(figsize=(8, 5))
plt.plot(df["n"], df["karatsubaOps"], marker="s", color="#ff7f0e", label="Karatsuba")
# Fit n^log2(3) ≈ n^1.585
n_vals = df["n"].values
k_vals = df["karatsubaOps"].values
# log-log fit to estimate exponent
log_n = np.log(n_vals[5:])    # skip tiny n to avoid log(0)
log_k = np.log(k_vals[5:])
slope, intercept = np.polyfit(log_n, log_k, 1)
plt.plot(x_fit, np.exp(intercept) * x_fit**slope, "--", color="red",
         label=f"Power-law fit: n^{slope:.3f}")
plt.title("Karatsuba: Primitive Operations vs Input Size (n)")
plt.xlabel("Input Size (n digits)")
plt.ylabel("Primitive Operations")
plt.legend()
plt.grid(True)
plt.tight_layout()
plt.savefig("output_plot/karatsuba_ops.png", dpi=300, bbox_inches="tight")
plt.show()

# ---- Graph 3: Comparison ----
plt.figure(figsize=(10, 6))
plt.plot(df["n"], df["simpleOps"],    marker="o", linestyle="-",  color="#1f77b4",
         label="Simple Multiplication O(n²)")
plt.plot(df["n"], df["karatsubaOps"], marker="s", linestyle="--", color="#ff7f0e",
         label=f"Karatsuba O(n^{slope:.3f}) ≈ O(n^1.585)")
plt.title("Comparison: Simple Multiplication vs Karatsuba")
plt.xlabel("Input Size (n digits)")
plt.ylabel("Primitive Operations")
plt.legend()
plt.grid(True)
plt.tight_layout()
plt.savefig("output_plot/comparison_graph.png", dpi=300, bbox_inches="tight")
plt.show()

print("Graphs saved to output_plot/")
print(f"  simple_mult_ops.png")
print(f"  karatsuba_ops.png")
print(f"  comparison_graph.png")
print(f"\nEstimated Karatsuba exponent: n^{slope:.4f} (theoretical: n^1.585)")
