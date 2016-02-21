// Player.java
// Copyright (c) 2007-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.tic;

import java.io.PrintStream;
import java.util.Random;

public abstract class Player {
	protected char[][] grid;
	protected int movesSoFar, freeCount, freeRow[] = new int[9], freeCol[] = new int[9];
	private int[] tmpRand9 = new int[9], tmpFR = new int[9], tmpFC = new int[9];
	protected Random rand = new Random();
	protected boolean randomizeMoves = false;

	public boolean getRandomizeMoves() { return randomizeMoves; }
	public void setRandomizeMoves(boolean randomizeMoves) { this.randomizeMoves = randomizeMoves; }

	// Choose the next move.
	// This returns (row*3)+column for the cell in which to place our next play.
	public abstract int chooseNextMove(char[][] grid, char whichPlayer);

	// Find free cells. This populates movesSoFar, freeCount, freeRow and freeCol.
	// This should be called within the implementing class' chooseNextMove() function, before
	// doing anything.
	protected void setGridAndFindFreeCells(char[][] grid) {
		this.grid = grid;
		// Find the rows and columns for all free cells.
		movesSoFar = freeCount = 0;
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++) {
				if (grid[r][c] == ' ') {
					tmpFR[freeCount] = r;
					tmpFC[freeCount] = c;
					freeCount++;
				} else {
					movesSoFar++;
				}
			}
		}
		if (randomizeMoves) {
			// Randomize the order of the free cells.
			initRandomIndexes(tmpRand9, freeCount);
			for (int i = 0, j = 0; i < freeCount; i++) {
				j = tmpRand9[i];
				freeRow[i] = tmpFR[j];
				freeCol[i] = tmpFC[j];
			}
		} else {
			// Copy the free cells into the final array.
			for (int i = 0; i < freeCount; i++) {
				freeRow[i] = tmpFR[i];
				freeCol[i] = tmpFC[i];
			}
		}
	}

	// Return the number of free cells at the beginning of the most recent call to chooseNextMove().
	// This requires that each subclass' chooseNextMove() function calls
	// setGridAndFindFreeCells(grid) before doing anything else.
	public int getFreeCount() { return freeCount; }

	// Given a row and column, return the index into freeRow and freeCol which match it.
	// If not found, return -1.
	protected int findFreeCellIdx(int row, int col) {
		for (int i = 0; i < freeCount; i++) {
			if ( (freeRow[i] == row) && (freeCol[i] == col) ) return i;
		}
		return -1;
	}

	// Convert a free cell index to (row*3)+column for the corresponding cell.
	protected int freeCellIdxToRow3Col(int freeCellIdx) {
		return (freeRow[freeCellIdx]*3)+freeCol[freeCellIdx];
	}

	public static void showGrid(char[][] grid, PrintStream out) {
		System.out.println("-------");
		for (int r = 0; r < 3; r++) {
			System.out.print('|');
			System.out.print(grid[r][0]);
			System.out.print('|');
			System.out.print(grid[r][1]);
			System.out.print('|');
			System.out.print(grid[r][2]);
			System.out.println('|');
			if (r < 2) System.out.println("|-----|");
		}
		System.out.println("-------");
	}

	// Initialize sequential indexes into the indexes[] array, then shuffle them into random order.
	private void initRandomIndexes(int[] indexes, int numIndexes) {
		for (int i = 0; i < numIndexes; i++) indexes[i] = i;
		if ( (randomizeMoves) && (numIndexes > 1) ) {
			int r1, r2, tmp;
			for (int i = 0; i < numIndexes; i++) {
				r1 = rand.nextInt(numIndexes);
				r2 = rand.nextInt(numIndexes);
				tmp = indexes[r1];
				indexes[r1] = indexes[r2];
				indexes[r2] = tmp;
			}
		}
	}
}
