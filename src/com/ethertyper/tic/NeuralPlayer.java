// NeuralPlayer.java
// Copyright (c) 2007-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.tic;

import com.roncemer.neural.*;

public class NeuralPlayer extends Player {
	private BackPropagationNeuralNetwork nn;
	private double[] netIns;
	private double[] netOuts;
	private double[] netExpectedOuts = new double[9];
	private char[][][] allBoards = null;
	private int[] allBoardsXNextMove = null, allBoardsONextMove = null;
	private int numPassedMoves, numFailedMoves;

	public NeuralPlayer() {
		// By default, this player does not shuffle the free positions.
		setRandomizeMoves(true);

		nn = new BackPropagationNeuralNetwork(new int[] { 18, 36, 9 });
		netIns = nn.getInputs();
		netOuts = nn.getOutputs();
	}

/// TODO: Write the constructor in BackPropagationNeuralNetwork.java which takes an encoded
/// NN as a string, then uncomment this.
///	public NeuralPlayer(String encodedNN) {
///		nn = new BackPropagationNeuralNetwork(encodedNN);
///		netIns = nn.getInputs();
///		netOuts = nn.getOutputs();
///	}

	public String encodeNN() {
		return nn.encode();
	}

	private char[][] tmpGrid = new char[3][3];
	private int[] tmpFreeRow = new int[9], tmpFreeCol = new int[9];

	public synchronized int chooseNextMove(char[][] grid, char whichPlayer) {
		lazyLoadAllBoards();

		setGridAndFindFreeCells(grid);

		for (int r = 0; r < 3; r++) System.arraycopy(grid[r], 0, tmpGrid[r], 0, 3);
		System.arraycopy(freeRow, 0, tmpFreeRow, 0, freeCount);
		System.arraycopy(freeCol, 0, tmpFreeCol, 0, freeCount);

		char opponent = (whichPlayer == 'X') ? 'O' : 'X';

		for (int rotPass = 0; rotPass < 4; rotPass++) {
			for (int flipPass = 0; flipPass < 2; flipPass++) {
				boolean found = false;
				char[][] ab;
				for (int i = 0; i < allBoards.length; i++) {
					ab = allBoards[i];
					if ((tmpGrid[0][0] == ab[0][0]) &&
						(tmpGrid[0][1] == ab[0][1]) &&
						(tmpGrid[0][2] == ab[0][2]) &&
						(tmpGrid[1][0] == ab[1][0]) &&
						(tmpGrid[1][1] == ab[1][1]) &&
						(tmpGrid[1][2] == ab[1][2]) &&
						(tmpGrid[2][0] == ab[2][0]) &&
						(tmpGrid[2][1] == ab[2][1]) &&
						(tmpGrid[2][2] == ab[2][2])) {
						found = true;
						break;
					}
				}

				if (found) {
					for (int r = 0, idx = 0; r < 3; r++) {
						for (int c = 0; c < 3; c++, idx++) {
							char ch = tmpGrid[r][c];
							netIns[idx] = (ch == whichPlayer) ? 1.0 : 0.0;
							netIns[idx+9] = (ch == opponent) ? 1.0 : 0.0;
						}
					}

					nn.forwardPropagate();

					// Use the output with the highest value.
					double maxOut = 0.0;
					int nextMove = -1;
					for (int freeIdx = 0; freeIdx < freeCount; freeIdx++) {
						int pos = (tmpFreeRow[freeIdx]*3)+tmpFreeCol[freeIdx];
						double out = netOuts[pos];
						if (out > maxOut) {
							maxOut = out;
							nextMove = pos;
						}
					}
					if (nextMove >= 0) {
						// Adjust nextMove back into the original grid orientation.
						if (flipPass > 0) {
							nextMove = AllBoards.unflipHMove(nextMove);
						}
						for (int rp = 0; rp < rotPass; rp++) {
							nextMove = AllBoards.unrotate90CCWMove(nextMove);
						}
					} else {
						// No valid move.  Choose a random move.
						int freeIdx = rand.nextInt(freeCount);
						nextMove = (freeRow[freeIdx]*3)+freeCol[freeIdx];
					}
					return nextMove;
				}	// if (found)

				AllBoards.flipHGrid(tmpGrid);
				AllBoards.flipHColInts(tmpFreeCol, freeCount);
			}	// for (int flipPass = 0; flipPass < 2; flipPass++)

			AllBoards.rotate90CCWGrid(tmpGrid);
			AllBoards.rotate90CCWRowColInts(tmpFreeRow, tmpFreeCol, freeCount);
		}	// for (int rotPass = 0; rotPass < 4; rotPass++)

		throw new InternalError("Unexpected grid");
	}

	public synchronized void trainAllBoardsOnePass() {
		lazyLoadAllBoards();

		nn.setLearningRate(new double[] { 0.002 });
		nn.setMomentum(0.8);

		numPassedMoves = numFailedMoves = 0;
		for (int i = 0; i < allBoards.length; i++) {
			char[][] grid = allBoards[i];
			int xMove = allBoardsXNextMove[i], oMove = allBoardsONextMove[i];
			if (xMove >= 0) trainOneMove('X', grid, xMove);
			if (oMove >= 0) trainOneMove('O', grid, oMove);
		}
	}

	public synchronized int getNumPassedMoves() {
		return numPassedMoves;
	}

	public synchronized int getNumFailedMoves() {
		return numFailedMoves;
	}

	private void trainOneMove(char whichPlayer, char[][] gridBeforeMove, int selectedMove) {
		char opponent = (whichPlayer == 'X') ? 'O' : 'X';
		for (int r = 0, idx = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++, idx++) {
				char ch = gridBeforeMove[r][c];
				netIns[idx] = (ch == whichPlayer) ? 1.0 : 0.0;
				netIns[idx+9] = (ch == opponent) ? 1.0 : 0.0;
				netExpectedOuts[idx] = 0.0;
			}
		}
		netExpectedOuts[selectedMove] = 1.0;
		nn.forwardPropagate();
		double[] outs = nn.getOutputs();
		double max = 0.0;
		int maxIdx = -1;
		for (int i = 0; i < 9; i++) {
			if (outs[i] > max) {
				max = outs[i];
				maxIdx = i;
			}
		}
		if (maxIdx == selectedMove) {
			numPassedMoves++;
		} else {
			numFailedMoves++;
			nn.backPropagate(netExpectedOuts);
		}
	}

	private void lazyLoadAllBoards() {
		if (allBoards == null) {
			allBoards = AllBoards.getAllBoards();
			allBoardsXNextMove = new int[allBoards.length];
			allBoardsONextMove = new int[allBoards.length];
			Player idealPlayer = new IdealPlayer();
			idealPlayer.setRandomizeMoves(false);
			for (int i = 0; i < allBoards.length; i++) {
				char[][] grid = allBoards[i];
				int xMoves = 0, oMoves = 0;
				for (int r = 0; r < 3; r++) {
					for (int c = 0; c < 3; c++) {
						switch (grid[r][c]) {
						case 'X': xMoves++; break;
						case 'O': oMoves++; break;
						}
					}
				}
				if (oMoves > xMoves) {
					// X's turn.
					allBoardsXNextMove[i] = idealPlayer.chooseNextMove(grid, 'X');
					allBoardsONextMove[i] = -1;
				} else if (xMoves > oMoves) {
					// O's turn.
					allBoardsXNextMove[i] = -1;
					allBoardsONextMove[i] = idealPlayer.chooseNextMove(grid, 'O');
				} else {
					// Either player's turn.  Calculate each player's next move.
					allBoardsXNextMove[i] = idealPlayer.chooseNextMove(grid, 'X');
					allBoardsONextMove[i] = idealPlayer.chooseNextMove(grid, 'O');
				}
			}
		}
	}
}
