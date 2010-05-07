// GATest.java
// Copyright (c) 2007-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.genetic;

public class GATest {
	public static void main(String[] args) {
		GeneticAlgorithm ga = new GeneticAlgorithm(
			16,
			new FitnessFunction() {
				public double calculateFitness(boolean[] individual) {
					int fitness = 0;
					if (individual[0]) {
						fitness++;
						for (int i = 1; i < individual.length; i++) {
							if (individual[i] != individual[i-1]) fitness++;
						}
					}
					return (double)fitness;
				}
			},
			new OnePointCrossoverFunction(0.3),
			1000,
			10
		);

		int generation = 0;
		printResult(ga, 0);

		while (true) {
			ga.runOneGeneration();
			generation++;

			printResult(ga, generation);
			if (ga.getIndividualFitness(0) == 16.0) break;
		}
	}

	public static void printResult(GeneticAlgorithm ga, int generation) {
		boolean[] bits = ga.getIndividualSolution(0);
		System.out.print(""+generation+": ");
		for (int i = 0; i < bits.length; i++) {
			System.out.print(bits[i] ? "1" : "0");
		}
		System.out.print(" ");
		System.out.println(ga.getIndividualFitness(0));
	}
}
