// BackPropagationNeuralNetwork.java
// Copyright (c) 2007-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.neural;

public class BackPropagationNeuralNetwork {
	protected int numLayers;
	protected double[] learningRate = new double[] { 0.1 };
	protected double momentum = 0.8;
	protected ActivationFunction[] activationFunction =
		new ActivationFunction[] { new TanhActivationFunction() };
	protected int[] numNeurons;
	protected int outputLayer;
	protected double[][] neuronValue;
	protected double[][] threshold;
	protected double[][][] weight;
	protected double[][][] lastWeightChange;
	protected double[][] errorGradient;
	protected java.util.Random rand = new java.util.Random();

	public BackPropagationNeuralNetwork(int[] numNeurons) {
		this.numNeurons = numNeurons;

		numLayers = numNeurons.length;
		outputLayer = numLayers-1;

		neuronValue = new double[numLayers][];
		threshold = new double[numLayers][];
		weight = new double[numLayers-1][][];
		lastWeightChange = new double[numLayers-1][][];
		errorGradient = new double[numLayers][];
		for (int layer = 0; layer < numLayers; layer++) {
			neuronValue[layer] = new double[numNeurons[layer]];
			threshold[layer] = new double[numNeurons[layer]];
			errorGradient[layer] = new double[numNeurons[layer]];
			if (layer > 0) {
				int prevLayer = layer-1;
				weight[prevLayer] = new double[numNeurons[prevLayer]][];
				lastWeightChange[prevLayer] = new double[numNeurons[prevLayer]][];
				for (int prevNeuron = 0; prevNeuron < weight[prevLayer].length; prevNeuron++) {
					weight[prevLayer][prevNeuron] = new double[numNeurons[layer]];
					lastWeightChange[prevLayer][prevNeuron] = new double[numNeurons[layer]];
				}
			}
		}

		randomize();
	}

	public void setLearningRate(double learningRate) {
		setLearningRate(new double[] { learningRate });
	}

	public void setLearningRate(double[] learningRate) { this.learningRate = learningRate; }

	public double getLearningRate(int layer) {
		if (layer < learningRate.length) return learningRate[layer];
		return learningRate[0];
	}

	public void setMomentum(double momentum) { this.momentum = momentum; }

	public double getMomentum() { return this.momentum; }

	public void setActivationFunction(ActivationFunction activationFunction) {
		setActivationFunction(new ActivationFunction[] { activationFunction });
	}

	public void setActivationFunction(ActivationFunction[] activationFunction) {
		this.activationFunction = activationFunction;
	}

	public ActivationFunction getActivationFunction(int layer) {
		if (layer < activationFunction.length) return activationFunction[layer];
		return activationFunction[0];
	}

	public double[] getInputs() { return neuronValue[0]; }

	public double[] getOutputs() { return neuronValue[numLayers-1]; }

	public void randomize() {
		for (int layer = 1, prevLayer = 0; layer < numLayers; layer++, prevLayer++) {
			for (int neuron = 0; neuron < numNeurons[layer]; neuron++) {
				threshold[layer][neuron] = (rand.nextDouble()-0.5)/2;
				for (int prevNeuron = 0; prevNeuron < numNeurons[prevLayer]; prevNeuron++) {
					weight[prevLayer][prevNeuron][neuron] = (rand.nextDouble()-0.5)/2;
					lastWeightChange[prevLayer][prevNeuron][neuron] = 0.0;
				}
			}
		}
	}

	public void forwardPropagate() {
		for (int layer = 1, prevLayer = 0; layer < numLayers; layer++, prevLayer++) {
			ActivationFunction act = getActivationFunction(layer);
			for (int neuron = 0; neuron < numNeurons[layer]; neuron++) {
				double sum = 0.0;
				for (int prevNeuron = 0; prevNeuron < (numNeurons[prevLayer]); prevNeuron++) {
					sum +=
						neuronValue[prevLayer][prevNeuron] *
						weight[prevLayer][prevNeuron][neuron];
				}
				neuronValue[layer][neuron] = act.calc(sum-threshold[layer][neuron]);
			}
		}
	}

	public void backPropagate(double[] expectedOutputs) {
		double momentum = getMomentum();
		for (int layer = numLayers-1, prevLayer = numLayers-2, nextLayer = numLayers;
			 layer > 0;
			 layer--, prevLayer--, nextLayer--) {
			double prevLearningRate = getLearningRate(prevLayer);
			ActivationFunction act = getActivationFunction(layer);
			for (int neuron = 0; neuron < numNeurons[layer]; neuron++) {
				if (layer == outputLayer) {
					errorGradient[layer][neuron] =
						act.calcInverse(neuronValue[layer][neuron]) *
						(expectedOutputs[neuron]-neuronValue[layer][neuron]);
				} else {
					double sum = 0.0;
					for (int nextNeuron = 0; nextNeuron < numNeurons[nextLayer]; nextNeuron++) {
						sum +=
							errorGradient[nextLayer][nextNeuron] *
							weight[layer][neuron][nextNeuron];
					}
					errorGradient[layer][neuron] =
						act.calcInverse(neuronValue[layer][neuron]) * sum;
				}
				for (int prevNeuron = 0; prevNeuron < numNeurons[prevLayer]; prevNeuron ++) {
					double weightChange =
						prevLearningRate *
						neuronValue[prevLayer][prevNeuron] *
						errorGradient[layer][neuron];
					weight[prevLayer][prevNeuron][neuron] +=
						weightChange + (lastWeightChange[prevLayer][prevNeuron][neuron] * momentum);
					lastWeightChange[prevLayer][prevNeuron][neuron] = weightChange;
				}
				threshold[layer][neuron] -= prevLearningRate * errorGradient[layer][neuron];
			}
		}
	}

	public String encode() {
		StringBuffer sb = new StringBuffer();
		String nl = System.getProperty("line.separator");

		sb.append("numLayers:");
		sb.append(Integer.toString(numLayers));
		sb.append(nl);

		sb.append("numNeurons:");
		for (int layer = 0; layer < numLayers; layer++) {
			if (layer > 0) sb.append(',');
			sb.append(Integer.toString(numNeurons[layer]));
		}
		sb.append(nl);

		sb.append("learningRate:");
		for (int layer = 0; layer < numLayers; layer++) {
			if (layer > 0) sb.append(',');
			sb.append(Double.toString(getLearningRate(layer)));
		}
		sb.append(nl);

		sb.append("momentum:");
		sb.append(Double.toString(momentum));
		sb.append(nl);

		sb.append("activationFunction:");
		for (int layer = 0; layer < numLayers; layer++) {
			if (layer > 0) sb.append(',');
			sb.append(getActivationFunction(layer).getClass().getName());
		}
		sb.append(nl);

		for (int layer = 1; layer < numLayers; layer++) {
			sb.append("threshold["+layer+"]:");
			for (int neuron = 0; neuron < numNeurons[layer]; neuron++) {
				if (neuron > 0) sb.append(',');
				sb.append(Double.toString(threshold[layer][neuron]));
			}
			sb.append(nl);
		}

		for (int layer = 0, nextLayer = 1; nextLayer < numLayers; layer++, nextLayer++) {
			for (int neuron = 0; neuron < neuronValue[layer].length; neuron++) {
				for (int nextNeuron = 0; nextNeuron < neuronValue[nextLayer].length; nextNeuron++) {
					sb.append("weight["+layer+"]["+neuron+"]["+nextNeuron+"]:");
					sb.append(Double.toString(weight[layer][neuron][nextNeuron]));
					sb.append(nl);
				}
			}
		}

		for (int layer = 0, nextLayer = 1; nextLayer < numLayers; layer++, nextLayer++) {
			for (int neuron = 0; neuron < neuronValue[layer].length; neuron++) {
				for (int nextNeuron = 0; nextNeuron < neuronValue[nextLayer].length; nextNeuron++) {
					sb.append("lastWeightChange["+layer+"]["+neuron+"]["+nextNeuron+"]:");
					sb.append(Double.toString(lastWeightChange[layer][neuron][nextNeuron]));
					sb.append(nl);
				}
			}
		}

		for (int layer = 1; layer < numLayers; layer++) {
			sb.append("errorGradient["+layer+"]:");
			for (int neuron = 0; neuron < neuronValue[layer].length; neuron++) {
				if (neuron > 0) sb.append(',');
				sb.append(Double.toString(errorGradient[layer][neuron]));
			}
			sb.append(nl);
		}

		return sb.toString();
	}
}
