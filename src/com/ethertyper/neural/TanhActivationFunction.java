// TanhActivationFunction.java
// Copyright (c) 2007-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.neural;

public final class TanhActivationFunction
	implements ActivationFunction {

	public final double calc(double value) {
		return tanh(value);
	}

	public final double calcInverse(double value) {
		double tmp = tanh(value);
		return 1.0 - (tmp * tmp);
	}

	private final double tanh(double u) {
		double a = Math.exp(u);
		double b = Math.exp(-u);
		return (a-b)/(a+b);
	}
}
