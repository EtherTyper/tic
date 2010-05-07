// IncrementCrossoverFunction.java
// Copyright (c) 2007-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.genetic;

public class IncrementCrossoverFunction implements CrossoverFunction {
	private boolean[] bits;

	public IncrementCrossoverFunction(int numBits) {
		this.bits = new boolean[numBits];
	}

	public void crossover(boolean[] parent1, boolean[] parent2, boolean[] child) {
		System.arraycopy(bits, 0, child, 0, bits.length);
		for (int i = bits.length-1; i >= 0; i--) {
			bits[i] = !bits[i];
			if (bits[i]) break;
		}
	}
}
