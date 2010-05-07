// GeneticPlayer1.java
// Copyright (c) 2007-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.tic;

import java.util.*;

import com.roncemer.genetic.*;

public class GeneticPlayer1 extends Player {
	private static final int BITS_PER_INDIVIDUAL = 4;
	private static final double MUTATION_RATE = 0.2;
	private static final int NUM_INDIVIDUALS = 32;
	private static final int NUM_SURVIVORS = 4;

	private char[][][] allBoards = null;
	private int[] allBoardsXMoves = null;
	private int[] allBoardsOMoves = null;
	private GeneticAlgorithm[] gax;
	private GeneticAlgorithm[] gao;
	private Random rand = new Random();
	private boolean initialized = false;

	public GeneticPlayer1() {
		// For consistent results, we must NOT shuffle the free positions.
		setRandomizeMoves(false);
	}

	protected class MyFitnessFunction implements FitnessFunction {
		private final char[][] startingBoard;
		private final int startingBoardMovesSoFar;
		private final char whichPlayer, opponent;
		private final SequencePlayer opponentPlayer = new SequencePlayer();
		private final Tic tic;
		private int firstMove;
		private boolean firstMoveDone = false;

		public MyFitnessFunction
			(char[][] startingBoard, int startingBoardMovesSoFar, char whichPlayer) {

			this.startingBoard = startingBoard;
			this.startingBoardMovesSoFar = startingBoardMovesSoFar;
			this.whichPlayer = whichPlayer;
			this.opponent = (whichPlayer == 'X') ? 'O' : 'X';

			Player gPlayer = new Player() {
				public int chooseNextMove(char[][] grid, char whichPlayer) {
					if (whichPlayer != MyFitnessFunction.this.whichPlayer) {
						throw new InternalError
							("Expected whichPlayer to be '"+MyFitnessFunction.this.whichPlayer+"'");
					}
					setGridAndFindFreeCells(grid);
					if (!firstMoveDone) {
						firstMoveDone = true;
						if (grid[firstMove/3][firstMove%3] != ' ') {
							Player.showGrid(grid, System.err);
							throw new InternalError(
								"Attempted first move into non-empty space; firstMove="+firstMove
							);
						}
						return firstMove;
					}
					return GeneticPlayer1.this.chooseNextMove(grid, whichPlayer);
				}
			};

			if (whichPlayer == 'X') {
				tic = new Tic(gPlayer, opponentPlayer);
			} else {
				tic = new Tic(opponentPlayer, gPlayer);
			}
		}

		public double calculateFitness(boolean[] individual) {
			firstMove =
				(individual[0] ? 8 : 0) |
				(individual[1] ? 4 : 0) |
				(individual[2] ? 2 : 0) |
				(individual[3] ? 1 : 0);
			if (firstMove >= 9) {
				// Move position is out of bounds.
				return -1.0;
			}
			if (startingBoard[firstMove/3][firstMove%3] != ' ') {
				// Move position already contains a move.
				return -1.0;
			}

			// Play some games against a sequence player, starting with the reference
			// board and letting the individual select the next move based on its solution,
			// and then playing moves we've already learned from there forward.
			// The number of games the individual wins or ties is the fitness.

			int numGames = 1;
			for (int fc = (9-startingBoardMovesSoFar)-1; fc > 0; fc -= 2) numGames *= fc;

			int numGamesPlayed = 0, fitness = 0;
			opponentPlayer.reset((9-startingBoardMovesSoFar)-1);
			do {
				for (int r = 0; r < 3; r++) {
					for (int c = 0; c < 3; c++) {
						tic.grid[r][c] = startingBoard[r][c];
					}
				}
				tic.movesSoFar = startingBoardMovesSoFar;
				tic.winner = ' ';
				tic.done = false;
				firstMoveDone = false;

				char wp = whichPlayer;
				while (!tic.nextMove(wp)) {
					wp = (wp == 'X') ? 'O' : 'X';
				}
				numGamesPlayed++;
				char winner = tic.getWinner();
				// If the first move causes a win, return numGames+1
				// to make this move score higher than all others.
				if ( (winner == whichPlayer) && (tic.movesSoFar == (startingBoardMovesSoFar+1)) ) {
					return numGames+1;
				}
				if (winner != opponent) {
					fitness++;
				}
				opponentPlayer.gameDone();
			} while (!opponentPlayer.isSequenceDone());

			if (numGamesPlayed != numGames) {
				throw new InternalError(
					"Unexpected # games played; numGames="+numGames+
					" numGamesPlayed="+numGamesPlayed
				);
			}

			return fitness;
		}
	}

	public void trainOneGeneration(int freeCount) {
		if (allBoards == null) {
			allBoards = AllBoards.getAllBoards();

			Arrays.sort(
				allBoards,
				new Comparator() {
					public int compare(Object o1, Object o2) {
						char[][] b1 = (char[][])o1;
						char[][] b2 = (char[][])o2;
						int n1 = 0, n2 = 0;
						for (int r = 0; r < 3; r++) {
							for (int c = 0; c < 3; c++) {
								if (b1[r][c] == ' ') n1++;
								if (b2[r][c] == ' ') n2++;
							}
						}
						if (n1 > n2) return 1;
						if (n1 < n2) return -1;
						return 0;
					}

					public boolean equals(Object obj) {
						if (obj == this) return true;
						return false;
					}
				}
			);

			allBoardsXMoves = new int[allBoards.length];
			allBoardsOMoves = new int[allBoards.length];
			gax = new GeneticAlgorithm[allBoards.length];
			gao = new GeneticAlgorithm[allBoards.length];

			char ch;
			int movesSoFar;
			for (int boardIdx = 0; boardIdx < allBoards.length; boardIdx++) {
				allBoardsXMoves[boardIdx] = allBoardsOMoves[boardIdx] = 0;
				for (int r = 0; r < 3; r++) {
					for (int c = 0; c < 3; c++) {
						switch (allBoards[boardIdx][r][c]) {
						case 'X': allBoardsXMoves[boardIdx]++; break;
						case 'O': allBoardsOMoves[boardIdx]++; break;
						}
					}
				}
				movesSoFar = allBoardsXMoves[boardIdx] + allBoardsOMoves[boardIdx];
				if (allBoardsXMoves[boardIdx] <= allBoardsOMoves[boardIdx]) {
					gax[boardIdx] = new GeneticAlgorithm(
						BITS_PER_INDIVIDUAL,
						new MyFitnessFunction(allBoards[boardIdx], movesSoFar, 'X'),
						new OnePointCrossoverFunction(MUTATION_RATE),
///new IncrementCrossoverFunction(BITS_PER_INDIVIDUAL),
///new RandomCrossoverFunction(),
						NUM_INDIVIDUALS,
						NUM_SURVIVORS
					);
					// We have to recalc fitness for survivors of the first generation
					// because the initial randomization pass won't calculate accurate
					// fitness scores.
					gax[boardIdx].setRecalcFitnessForSurvivors(true);
				}
				if (allBoardsOMoves[boardIdx] <= allBoardsXMoves[boardIdx]) {
					gao[boardIdx] = new GeneticAlgorithm(
						BITS_PER_INDIVIDUAL,
						new MyFitnessFunction(allBoards[boardIdx], movesSoFar, 'O'),
						new OnePointCrossoverFunction(MUTATION_RATE),
///new IncrementCrossoverFunction(BITS_PER_INDIVIDUAL),
///new RandomCrossoverFunction(),
						NUM_INDIVIDUALS,
						NUM_SURVIVORS
					);
					// We have to recalc fitness for survivors of the first generation
					// because the initial randomization pass won't calculate accurate
					// fitness scores.
					gao[boardIdx].setRecalcFitnessForSurvivors(true);
				}
			}
			initialized = true;
		}	// if (allBoards == null)

		for (int boardIdx = 0; boardIdx < allBoards.length; boardIdx++) {
			if ((9-(allBoardsXMoves[boardIdx]+allBoardsOMoves[boardIdx])) == freeCount) {
				if (gax[boardIdx] != null) {
					gax[boardIdx].runOneGeneration();
					// No need to recalculate fitness for survivors after the first pass.
					gax[boardIdx].setRecalcFitnessForSurvivors(false);
				}
				if (gao[boardIdx] != null) {
					gao[boardIdx].runOneGeneration();
					// No need to recalculate fitness for survivors after the first pass.
					gao[boardIdx].setRecalcFitnessForSurvivors(false);
				}
			}
		}
	}

	char[][] tmpGrid = new char[3][3];

	public synchronized int chooseNextMove(char[][] grid, char whichPlayer) {
		setGridAndFindFreeCells(grid);

		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++) {
				tmpGrid[r][c] = grid[r][c];
			}
		}

		GeneticAlgorithm[] ga = (whichPlayer == 'X') ? gax : gao;

		int unrotflpmove = -1, move = -1;
		boolean boardFound = false;
		int boardIdx = -1;
		for (int rotPass = 0; ( (rotPass < 4) && (!boardFound) ); rotPass++) {
			for (int flipPass = 0; ( (flipPass < 2) && (!boardFound) ); flipPass++) {
				for (int i = 0; i < allBoards.length; i++) {
					if (ga[i] != null) {
						boolean match = true;
						for (int r = 0; ( (r < 3) && (match) ); r++) {
							for (int c = 0; c < 3; c++) {
								if (allBoards[i][r][c] != tmpGrid[r][c]) {
									match = false;
									break;
								}
							}
						}
						if (match) {
							boardFound = true;
							boardIdx = i;
							boolean[] solution = ga[i].getIndividualSolution(0);
							unrotflpmove = move =
								(solution[0] ? 8 : 0) |
								(solution[1] ? 4 : 0) |
								(solution[2] ? 2 : 0) |
								(solution[3] ? 1 : 0);
							if ( (move >= 0) && (move < 9) ) {
								int mr = move/3, mc = move%3;
								if (tmpGrid[mr][mc] == ' ') {
									if (flipPass > 0) {
										move = AllBoards.unflipHMove(move);
									}
									for (int rp = 0; rp < rotPass; rp++) {
										move = AllBoards.unrotate90CCWMove(move);
									}
									return move;
								}
							}
							break;
						}	// if (match)
					}	// if (ga[i] != null)
				}	// for (int i = 0; i < allBoards.length; i++)
				AllBoards.flipHGrid(tmpGrid);
			}	// for (int flipPass = 0; ( (flipPass < 2) && (!boardFound) ); flipPass++)
			AllBoards.rotate90CCWGrid(tmpGrid);
		}	// for (int rotPass = 0; ( (rotPass < 4) && (!boardFound) ); rotPass++)

		if (initialized) {
			if (boardIdx >= 0) {
				for (int j = 0; j < NUM_INDIVIDUALS; j++) {
					System.out.print(" ind "+j+" fitness="+ga[boardIdx].getIndividualFitness(j)+" ");
					boolean[] bits = ga[boardIdx].getIndividualSolution(j);
					for (int k = 0; k < BITS_PER_INDIVIDUAL; k++) {
						System.out.print(bits[k] ? '1' : '0');
					}
					System.out.println();
				}
				System.out.println("Found board:");
				Player.showGrid(allBoards[boardIdx], System.out);
			}
			System.out.println("Game board:");
			Player.showGrid(grid, System.out);
			System.out.println("whichPlayer="+whichPlayer+" boardFound="+boardFound+" boardIdx="+boardIdx+" allBoardsXHasDoubleWin[boardIdx]="/*+allBoardsXHasDoubleWin[boardIdx]+" allBoardsOHasDoubleWin[boardIdx]="+allBoardsOHasDoubleWin[boardIdx]*/+" unrotflpmove="+unrotflpmove+" move="+move);
			throw new InternalError("got random move after initialization");
		}

		int freeIdx = rand.nextInt(freeCount);
		return (freeRow[freeIdx]*3)+freeCol[freeIdx];
	}
}
