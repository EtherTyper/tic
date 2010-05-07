// RandomCrossoverFunction.java
// Copyright (c) 2007-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.genetic;

import java.util.Random;

public class RandomCrossoverFunction implements CrossoverFunction {
	private Random rand = new Random();

	public void crossover(boolean[] parent1, boolean[] parent2, boolean[] child) {
		for (int i = 0; i < child.length; i++) {
			child[i] = (rand.nextInt(2) != 0) ? true : false;
		}
	}
}
