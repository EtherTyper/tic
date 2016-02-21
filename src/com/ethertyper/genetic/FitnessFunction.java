// FitnessFunction.java
// Copyright (c) 2007-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.genetic;

public interface FitnessFunction {
	public double calculateFitness(boolean[] individual);
}
