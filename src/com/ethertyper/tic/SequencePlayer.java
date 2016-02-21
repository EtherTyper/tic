// SequencePlayer.java
// Copyright (c) 2007-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.tic;

// Player which can play multiple games, beginning with the first available position
// for each move, and skipping to the next possible combination each time gameDone()
// is called.
public class SequencePlayer extends Player {
	private int startingFreeCount = 0;
	private int[] nextMoveIdx = new int[10];
	private boolean sequenceDone = false;
///
public boolean debug = false;
private boolean debug_beginseq = true;

	public SequencePlayer() {
		// For consistent results, we must NOT shuffle the free positions.
		setRandomizeMoves(false);
	}

	public synchronized int chooseNextMove(char[][] grid, char whichPlayer) {
		if (sequenceDone) {
			throw new InternalError("SequencePlayer.chooseNextMove(): sequenceDone is set");
		}
///System.out.println("SequencePlayer chooseNextMove()");
///
///for (int i = 0; i < 10; i++) System.out.print(" "+nextMoveIdx[i]);
///System.out.println();
		setGridAndFindFreeCells(grid);
		int freeIdx = nextMoveIdx[freeCount];
///
if (debug) {
	if (debug_beginseq) {
		debug_beginseq = false;
		System.out.print(this);
	}
	System.out.print(" "+freeIdx);
}
		return (freeRow[freeIdx]*3)+freeCol[freeIdx];
	}

	// Reset to playing the first possible move.
	// startingFreeCount can optionally be set to a value less than 9,
	// which enables us to begin the game at a spot other than the
	// beginning of the game, first player.
	public synchronized void reset(int startingFreeCount) {
		if (startingFreeCount < 0) {
			startingFreeCount = 0;
		} else if (startingFreeCount > 9) {
			startingFreeCount = 9;
		}
		this.startingFreeCount = startingFreeCount;
///System.out.println("SequencePlayer reset("+startingFreeCount+")");
		for (int i = 0; i < 10; i++) nextMoveIdx[i] = 0;
		sequenceDone = (startingFreeCount == 0);
///
if (!sequenceDone) debug_beginseq = true;
	}

	// This must be called after each game, to cause this player to
	// try the next possible game sequence upon the next iteration.
	public synchronized void gameDone() {
		if (!sequenceDone) {
			int fc;
			for (fc = startingFreeCount; fc > 0; fc -= 2) { ; }
			fc += 2;
			for (; fc <= startingFreeCount; fc += 2) {
				nextMoveIdx[fc]++;
				if (nextMoveIdx[fc] < fc) break;
				nextMoveIdx[fc] = 0;
				if (fc >= startingFreeCount) {
					sequenceDone = true;
				}
			}
///
if (debug) System.out.println();
debug_beginseq = true;
		}
	}

	public boolean isSequenceDone() {
		return sequenceDone;
	}
}
