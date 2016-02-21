// BackPropagationNeuralNetworkTest.java
// Copyright (c) 2007-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.neural;

public class BackPropagationNeuralNetworkTest {
	private static void print
		(double[] inputs, double[] outputs, double[] expectedOutputs) {

		System.out.print("in:");
		for (int j = 0; j < inputs.length; j++) System.out.print(" "+inputs[j]);
		System.out.print("  out:");
		for (int j = 0; j < expectedOutputs.length; j++) {
			System.out.print(" ("+expectedOutputs[j]+")");
			System.out.print(" "+outputs[j]);
		}
		System.out.println();
	}

	public static void main(String[] args) {
		BackPropagationNeuralNetwork net = new BackPropagationNeuralNetwork(new int[] { 2, 2, 1 });
		double[] inputs = net.getInputs();
		double[] outputs = net.getOutputs();

		double[][] truthTable = new double[][] {
			{ 0, 1, }, { 0 },
			{ 1, 1, }, { 1 },
			{ 1, 0, }, { 0 },
			{ 0, 0, }, { 1 },
		};

		double[] expectedOutputs;
		boolean printThisPass;

		for (int pass = 1; pass <= 10000; pass++) {
			printThisPass = (pass == 1) || ((pass % 1000) == 0);
			if (printThisPass) System.out.println("Pass "+pass+":");
			for (int idx = 0; (idx+1) < truthTable.length; idx += 2) {
				System.arraycopy(truthTable[idx], 0, inputs, 0, inputs.length);
				expectedOutputs = truthTable[idx+1];

				net.forwardPropagate();
				net.backPropagate(expectedOutputs);

				if (printThisPass) print(inputs, outputs, expectedOutputs);
			}
		}
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("Final result:");
		for (int idx = 0; (idx+1) < truthTable.length; idx += 2) {
			System.arraycopy(truthTable[idx], 0, inputs, 0, inputs.length);
			expectedOutputs = truthTable[idx+1];

			net.forwardPropagate();
			print(inputs, outputs, expectedOutputs);
		}
	}
}
