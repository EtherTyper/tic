// GeneticAlgorithm.java
// Copyright (c) 2007-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.genetic;

import java.util.*;

public class GeneticAlgorithm {
	protected final Random rand = new Random();
	private boolean recalcFitnessForSurvivors = false;

	protected Comparator reverseFitnessComparator = new Comparator() {
		public int compare(Object o1, Object o2) {
			if (((Individual)o1).fitness > ((Individual)o2).fitness) return -1;
			if (((Individual)o1).fitness < ((Individual)o2).fitness) return 1;
			return 0;
		}

		public boolean equals(Object obj) {
			if (obj == this) return true;
			return false;
		}
	};

	private class Individual {
		protected boolean[] bits;
		protected FitnessFunction fitnessFunction;
		protected CrossoverFunction crossoverFunction;
		protected double fitness;

		public Individual(
			int numBits,
			FitnessFunction fitnessFunction,
			CrossoverFunction crossoverFunction) {

			bits = new boolean[numBits];
			this.fitnessFunction = fitnessFunction;
			this.crossoverFunction = crossoverFunction;
		}

		public void randomize() {
			for (int i = 0; i < bits.length; i++) {
				bits[i] = rand.nextBoolean();
			}
		}

		public void calculateFitness() {
			fitness = fitnessFunction.calculateFitness(bits);
		}

		public void reproduce(Individual spouse, Individual child) {
			crossoverFunction.crossover(bits, spouse.bits, child.bits);
		}

		public boolean equals(Object o) {
			if (o instanceof Individual) {
				if (((Individual)o).bits.length == bits.length) {
					for (int i = 0; i < bits.length; i++) {
						if (((Individual)o).bits[i] != bits[i]) {
							return false;
						}
					}
				}
			}
			return super.equals(o);
		}
	}

	protected int numBits;
	protected FitnessFunction fitnessFunction;
	protected CrossoverFunction crossoverFunction;
	protected int numIndividuals;
	protected int numFittestToSurvive;

	protected Individual [] individuals;

	public GeneticAlgorithm(
		int numBits,
		FitnessFunction fitnessFunction,
		CrossoverFunction crossoverFunction,
		int numIndividuals,
		int numFittestToSurvive) {

		this.numBits = numBits;
		this.fitnessFunction = fitnessFunction;
		this.crossoverFunction = crossoverFunction;
		this.numIndividuals = numIndividuals;
		this.numFittestToSurvive = numFittestToSurvive;

		individuals = new Individual[numIndividuals];
		for (int i = 0; i < numIndividuals; i++) {
			individuals[i] = new Individual(numBits, fitnessFunction, crossoverFunction);
		}

		randomize();
	}

	public boolean getRecalcFitnessForSurvivors() {
		return recalcFitnessForSurvivors;
	}

	public void setRecalcFitnessForSurvivors(boolean recalcFitnessForSurvivors) {
		this.recalcFitnessForSurvivors = recalcFitnessForSurvivors;
	}

	public void randomize() {
		for (int i = 0; i < numIndividuals; i++) {
			individuals[i].randomize();
			individuals[i].calculateFitness();
		}
		Arrays.sort(individuals, reverseFitnessComparator);
	}

	public void runOneGeneration() {
		for (int ci = numFittestToSurvive, i1 = 0, i2 = 0; ci < numIndividuals;) {
			i1 = rand.nextInt(numFittestToSurvive);
			i2 = rand.nextInt(numFittestToSurvive);
			if ( (i1 != i2) && (!individuals[i1].equals(individuals[i2])) ) {
				individuals[i1].reproduce(individuals[i2], individuals[ci]);
				if (!recalcFitnessForSurvivors) {
					individuals[ci].calculateFitness();
				}
				ci++;
			}
		}
		if (recalcFitnessForSurvivors) {
			for (int i = 0; i < numIndividuals; i++) {
				individuals[i].calculateFitness();
			}
		}

		Arrays.sort(individuals, reverseFitnessComparator);
		// Shuffle the most fit individuals.
		int numWithMostFitScore = 0;
		while ((numWithMostFitScore < numIndividuals) &&
			   (individuals[numWithMostFitScore].fitness == individuals[0].fitness)) {
			numWithMostFitScore++;
		}
		if (numWithMostFitScore >= 2) {
			for (int swaps = numWithMostFitScore-1; swaps > 0;) {
				int i1 = rand.nextInt(numWithMostFitScore);
				int i2 = rand.nextInt(numWithMostFitScore);
				if (i1 != i2) {
					Individual ind1 = individuals[i1];
					individuals[i1] = individuals[i2];
					individuals[i2] = ind1;
					swaps--;
				}
			}
		}
	}

	public boolean[] getIndividualSolution(int individualIdx) {
		return individuals[individualIdx].bits;
	}

	public double getIndividualFitness(int individualIdx) {
		return individuals[individualIdx].fitness;
	}
}
