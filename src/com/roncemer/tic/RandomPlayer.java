// RandomPlayer.java
// Copyright (c) 2007-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.tic;

public class RandomPlayer extends Player {
	public RandomPlayer() {
		// No need to shuffle the free positions.
		setRandomizeMoves(false);
	}

	public synchronized int chooseNextMove(char[][] grid, char whichPlayer) {
		setGridAndFindFreeCells(grid);
		int freeIdx = rand.nextInt(freeCount);
		return (freeRow[freeIdx]*3)+freeCol[freeIdx];
	}
}
