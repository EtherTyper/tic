// CrossoverFunction.java
// Copyright (c) 2007-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.genetic;

public interface CrossoverFunction {
	public void crossover(boolean[] parent1, boolean[] parent2, boolean[] child);
}
