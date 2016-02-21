// IdealPlayer.java
// Copyright (c) 2007-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.tic;

public class IdealPlayer extends Player {
	private int[] forkFreeIdx = new int[9];
	private int[] forkSetupCount = new int[9];
	private int forkCount;

	private static final int[][] orders1 = {
		{ 0 },
	};
	private static final int[][] orders2 = {
		{ 0, 1 },
		{ 1, 0 },
	};
	private static final int[][] orders3 = {
		{ 0, 1, 2 },
		{ 0, 2, 1 },
		{ 1, 0, 2 },
		{ 1, 2, 0 },
		{ 2, 0, 1 },
		{ 2, 1, 0 },
	};
	private static final int[][] orders4 = {
		{ 0, 1, 2, 3 },
		{ 0, 1, 3, 2 },
		{ 0, 2, 1, 3 },
		{ 0, 2, 3, 1 },
		{ 0, 3, 1, 2 },
		{ 0, 3, 2, 1 },
		{ 1, 0, 2, 3 },
		{ 1, 0, 3, 2 },
		{ 1, 2, 0, 3 },
		{ 1, 2, 3, 0 },
		{ 1, 3, 0, 2 },
		{ 1, 3, 2, 0 },
		{ 2, 0, 1, 3 },
		{ 2, 0, 3, 1 },
		{ 2, 1, 0, 3 },
		{ 2, 1, 3, 0 },
		{ 2, 3, 0, 1 },
		{ 2, 3, 1, 0 },
		{ 3, 0, 1, 2 },
		{ 3, 0, 2, 1 },
		{ 3, 1, 0, 2 },
		{ 3, 1, 2, 0 },
		{ 3, 2, 0, 1 },
		{ 3, 2, 1, 0 },
	};
	private static final int[][][] orders = { null, orders1, orders2, orders3, orders4 };

	public IdealPlayer() {
		// By default, this player shuffles the free positions.
		setRandomizeMoves(true);
	}

	public synchronized int chooseNextMove(char[][] grid, char whichPlayer) {
		char opponent = (whichPlayer == 'X') ? 'O' : 'X';

		setGridAndFindFreeCells(grid);

		int idx;

		// Take obvious wins and obvious blocks.
		if ((idx = tryWinOrBlock(whichPlayer)) >= 0) {
			return freeCellIdxToRow3Col(idx);
		}
		if ((idx = tryWinOrBlock(opponent)) >= 0) {
			return freeCellIdxToRow3Col(idx);
		}

		// Try to fork.
		if ((idx = tryFork(whichPlayer)) >= 0) {
			return freeCellIdxToRow3Col(idx);
		}

		// Try to block opponent's fork.
		if ((idx = tryForkBlock(whichPlayer)) >= 0) {
			return freeCellIdxToRow3Col(idx);
		}

		// Take the center if availble.
		if (grid[1][1] == ' ') {
			return (1*3)+1;
		}

		// Take the opposite corner from the opponent, if available.
		if ((idx = tryOppositeCornerFromOpponent(whichPlayer)) >= 0) {
			return freeCellIdxToRow3Col(idx);
		}

		// Take any available corner.
		if ((idx = tryAnyCorner(whichPlayer)) >= 0) {
			return freeCellIdxToRow3Col(idx);
		}

		// Take any available side.
		if ((idx = tryAnySide(whichPlayer)) >= 0) {
			return freeCellIdxToRow3Col(idx);
		}

		// NOTE: We should never get here.

		return freeCellIdxToRow3Col(rand.nextInt(freeCount));
	}

	// Try to set up a win for, or block against, whichPlayer.
	// Returns index into freeRow[], freeCol[] if successful, or -1 if failure.
	private int tryWinOrBlock(char whichPlayer) {
		if (movesSoFar < 3) return -1;
		int matchCount, spaceCount, spaceIdx;
		int ri, r, ci, c, rci, rc;
		int[] rs, cs, rcs;
		if (randomizeMoves) {
			// Randomize the order in which we process rows and columns.
			rs = orders3[rand.nextInt(3)];
			cs = orders3[rand.nextInt(3)];
			rcs = orders3[rand.nextInt(3)];
		} else {
			rs = cs = rcs = orders3[0];
		}
		for (ri = 0; ri < 3; ri++) {
			r = rs[ri];
			matchCount = spaceCount = 0;
			spaceIdx = -1;
			for (ci = 0; ci < 3; ci++) {
				c = cs[ci];
				if (grid[r][c] == whichPlayer) {
					matchCount++;
				} else if (grid[r][c] == ' ') {
					spaceCount++;
					spaceIdx = c;
				}
			}
			if ( (matchCount == 2) && (spaceCount == 1) ) {
				return findFreeCellIdx(r, spaceIdx);
			}
		}
		for (ci = 0; ci < 3; ci++) {
			c = cs[ci];
			matchCount = spaceCount = 0;
			spaceIdx = -1;
			for (ri = 0; ri < 3; ri++) {
				r = rs[ri];
				if (grid[r][c] == whichPlayer) {
					matchCount++;
				} else if (grid[r][c] == ' ') {
					spaceCount++;
					spaceIdx = r;
				}
			}
			if ( (matchCount == 2) && (spaceCount == 1) ) {
				return findFreeCellIdx(spaceIdx, c);
			}
		}
		// If we're randomizing moves, randomize the order in which we do the next two inner loops.
		for (int pass = 1, step = randomizeMoves ? rand.nextInt(2) : 0;
			 pass <= 2;
			 pass++, step = (step != 0) ? 0 : 1) {
			matchCount = spaceCount = 0;
			spaceIdx = -1;
			if (step == 0) {
				for (rci = 0; rci < 3; rci++) {
					rc = rcs[rci];
					if (grid[rc][rc] == whichPlayer) {
						matchCount++;
					} else if (grid[rc][rc] == ' ') {
						spaceCount++;
						spaceIdx = rc;
					}
				}
				if ( (matchCount == 2) && (spaceCount == 1) ) {
					return findFreeCellIdx(spaceIdx, spaceIdx);
				}
			} else {
				for (rci = 0; rci < 3; rci++) {
					rc = rcs[rci];
					if (grid[rc][2-rc] == whichPlayer) {
						matchCount++;
					} else if (grid[rc][2-rc] == ' ') {
						spaceCount++;
						spaceIdx = rc;
					}
				}
				if ( (matchCount == 2) && (spaceCount == 1) ) {
					return findFreeCellIdx(spaceIdx, 2-spaceIdx);
				}
			}
		}
		return -1;
	}

	// Try to set up a two-way (or more) win situation (a fork).
	// Returns index into freeRow[], freeCol[] if successful, or -1 if failure.
	private int tryFork(char whichPlayer) {
		if (movesSoFar < 2) return -1;
		findForkCells(whichPlayer);
		if (forkCount > 0) {
			// If we only have one possible fork, take it.
			if (forkCount == 1) return forkFreeIdx[0];
			// We have more than one possible fork.
			// Find out the maximum number of forks we can set up by moving in any position.
			int maxCount = 0;
			for (int i = 0; i < forkCount; i++) {
				if (forkSetupCount[i] > maxCount) maxCount = forkSetupCount[i];
			}
			// Pick a position to play which achieves the maximum number of forks.
			for (int i = 0; i < forkCount; i++) {
				if (forkSetupCount[i] == maxCount) return forkFreeIdx[i];
			}
		}
		return -1;
	}

	// Try to block opponent's fork by forcing them to block our win.
	// Returns index into freeRow[], freeCol[] if successful, or -1 if failure.
	private int tryForkBlock(char whichPlayer) {
		if (movesSoFar <= 2) return -1;
		char opponent = (whichPlayer == 'X') ? 'O' : 'X';

		findForkCells(opponent);
		if (forkCount == 0) return -1;

		int ri, r, ci, c, rci, rc;
		int[] rs, cs, rcs;
		if (randomizeMoves) {
			// Randomize the order in which we process rows and columns.
			rs = orders3[rand.nextInt(3)];
			cs = orders3[rand.nextInt(3)];
			rcs = orders3[rand.nextInt(3)];
		} else {
			rs = cs = rcs = orders3[0];
		}
		int fr, fc, cd, numSteps, si, openr, openc, freeIdx, forkIdx, myCount, opponentCount;
		boolean found;
		for (freeIdx = 0; freeIdx < freeCount; freeIdx++) {
			fr = freeRow[freeIdx];
			fc = freeCol[freeIdx];
			if ( (fr == 1) && (fc == 1) ) {
				numSteps = 4;
			} else {
				numSteps = ( (fr == 0) || (fr == 2) ) && ( (fc == 0) || (fc == 2) ) ? 3 : 2;
			}
			int[] steps = randomizeMoves ?
				orders[numSteps][rand.nextInt(orders[numSteps].length)] :
				orders[numSteps][0];
			for (si = 0; si < numSteps; si++) {
				myCount = opponentCount = 0;
				openr = -1; openc = -1;
				switch (steps[si]) {
				case 0:		// Horizontal
					r = fr;
					for (ci = 0; ci < 3; ci++) {
						c = cs[ci];
						if (grid[r][c] == whichPlayer) {
							myCount++;
						} else if (grid[r][c] == opponent) {
							opponentCount++;
						} else if (c != fc) {
							// The row/col that will be left open if we make a move here.
							openr = r;
							openc = c;
						}
					}
					break;
				case 1:		// Vertical
					c = fc;
					for (ri = 0; ri < 3; ri++) {
						r = rs[ri];
						if (grid[r][c] == whichPlayer) {
							myCount++;
						} else if (grid[r][c] == opponent) {
							opponentCount++;
						} else if (r != fr) {
							// The row/col that will be left open if we make a move here.
							openr = r;
							openc = c;
						}
					}
					break;
				case 2:		// Diagonal
				case 3:
					if (numSteps == 4) {
						if (steps[si] == 2) {
							c = 0;
							cd = 1;
						} else {
							c = 2;
							cd = -1;
						}
					} else {
						if ( ( (fr == 0) && (fc == 0) ) || ( (fr == 2) && (fc == 2) ) ) {
							c = 0;
							cd = 1;
						} else {
							c = 2;
							cd = -1;
						}
					}
/// TODO: If randomizing moves, randomize row/column order. Be careful not to mess up the column delta (cd).
					for (r = 0; r < 3; r++, c += cd) {
						if (grid[r][c] == whichPlayer) {
							myCount++;
						} else if (grid[r][c] == opponent) {
							opponentCount++;
						} else if ( (r != fr) && (c != fc) ) {
							// The row/col that will be left open if we make a move here.
							openr = r;
							openc = c;
						}
					}
					break;
				}	// switch (steps[si])
				if ( (myCount > 0) && (opponentCount == 0) && (openr >= 0) && (openc >= 0) ) {
					// Make sure openr,openc aren't in a cell where the opponent would play to fork.
					found = false;
					for (forkIdx = 0; forkIdx < forkCount; forkIdx++) {
						if (   (freeRow[forkFreeIdx[forkIdx]] == openr)
							&& (freeCol[forkFreeIdx[forkIdx]] == openc)   ) {
							found = true;
							break;
						}
					}
					if (!found) {
						return freeIdx;
					}
				}
			}	// for (si = 0; si < numSteps; si++)
		}	// for (freeIdx = 0; freeIdx < freeCount; freeIdx++)

		return -1;
	}

	// Take the opposite corner from the opponent, if available.
	// Returns index into freeRow[], freeCol[] if successful, or -1 if failure.
	private int tryOppositeCornerFromOpponent(char whichPlayer) {
		if (movesSoFar < 1) return -1;
		char opponent = (whichPlayer == 'X') ? 'O' : 'X';
		int[] steps = randomizeMoves ? orders4[rand.nextInt(orders4.length)] : orders4[0];
		for (int si = 0; si < 4; si++) {
			switch (steps[si]) {
			case 0:
				if ( (grid[0][0] == opponent) && (grid[2][2] == ' ') ) return findFreeCellIdx(2, 2);
				break;
			case 1:
				if ( (grid[0][2] == opponent) && (grid[2][0] == ' ') ) return findFreeCellIdx(2, 0);
				break;
			case 2:
				if ( (grid[2][2] == opponent) && (grid[0][0] == ' ') ) return findFreeCellIdx(0, 0);
				break;
			case 3:
				if ( (grid[2][0] == opponent) && (grid[0][2] == ' ') ) return findFreeCellIdx(0, 2);
				break;
			}
		}
		return -1;
	}

	// Take any available corner.
	// Returns index into freeRow[], freeCol[] if successful, or -1 if failure.
	private int tryAnyCorner(char whichPlayer) {
		int[] steps = randomizeMoves ? orders4[rand.nextInt(orders4.length)] : orders4[0];
		for (int si = 0; si < 4; si++) {
			switch (steps[si]) {
			case 0:
				if (grid[0][0] == ' ') return findFreeCellIdx(0, 0);
				break;
			case 1:
				if (grid[0][2] == ' ') return findFreeCellIdx(0, 2);
				break;
			case 2:
				if (grid[2][0] == ' ') return findFreeCellIdx(2, 0);
				break;
			case 3:
				if (grid[2][2] == ' ') return findFreeCellIdx(2, 2);
				break;
			}
		}
		return -1;
	}

	// Take any available side.
	// Returns index into freeRow[], freeCol[] if successful, or -1 if failure.
	private int tryAnySide(char whichPlayer) {
		int[] steps = randomizeMoves ? orders4[rand.nextInt(orders4.length)] : orders4[0];
		for (int si = 0; si < 4; si++) {
			switch (steps[si]) {
			case 0:
				if (grid[0][1] == ' ') return findFreeCellIdx(0, 1);
				break;
			case 1:
				if (grid[1][2] == ' ') return findFreeCellIdx(1, 2);
				break;
			case 2:
				if (grid[2][1] == ' ') return findFreeCellIdx(2, 1);
				break;
			case 3:
				if (grid[1][0] == ' ') return findFreeCellIdx(1, 0);
				break;
			}
		}
		return -1;
	}

	// Try to set up a vertical or horizontal win situation.
	// Returns index into freeRow[], freeCol[] if successful, or -1 if failure.
	private int tryVertOrHorizWinSetup(char whichPlayer) {
		if (movesSoFar < 2) return -1;
		int[] rci = randomizeMoves ? orders3[rand.nextInt(orders3.length)] : orders3[0];
		for (int pass = 0, whichDir = randomizeMoves ? rand.nextInt(2) : 0;
			 pass < 2;
			 pass++, whichDir = (whichDir != 0) ? 0 : whichDir) {
			boolean takeCenter = randomizeMoves ? (rand.nextInt(2) != 0) : false;
			if (whichDir == 0) {
				// Try to set up a horizontal win.
				if (takeCenter) {
					for (int ri = 0, r = 0; ri < 3; ri++) {
						r = rci[ri];
						if (grid[r][1] == ' ') {
							if (   (grid[r][0] == whichPlayer) && (grid[r][2] == ' ')
								|| (grid[r][2] == whichPlayer) && (grid[r][0] == ' ')   ) {
								return findFreeCellIdx(r, 1);
							}
						}
					}
				} else {
					for (int ri = 0, r = 0; ri < 3; ri++) {
						r = rci[ri];
						if (grid[r][1] == ' ') {
							if ( (!randomizeMoves) || (rand.nextInt(2) == 0) ) {
								if ( (grid[r][0] == whichPlayer) && (grid[r][2] == ' ') ) {
									return findFreeCellIdx(r, 2);
								}
								if ( (grid[r][2] == whichPlayer) && (grid[r][0] == ' ') ) {
									return findFreeCellIdx(r, 0);
								}
							} else {
								if ( (grid[r][2] == whichPlayer) && (grid[r][0] == ' ') ) {
									return findFreeCellIdx(r, 0);
								}
								if ( (grid[r][0] == whichPlayer) && (grid[r][2] == ' ') ) {
									return findFreeCellIdx(r, 2);
								}
							}
						}
					}
				}
			} else {
				// Try to set up a vertical win.
				if (takeCenter) {
					for (int ci = 0, c = 0; ci < 3; ci++) {
						c = rci[ci];
						if (grid[1][c] == ' ') {
							if (   (grid[0][c] == whichPlayer) && (grid[2][c] == ' ')
								|| (grid[2][c] == whichPlayer) && (grid[0][c] == ' ')   ) {
								return findFreeCellIdx(1, c);
							}
						}
					}
				} else {
					for (int ci = 0, c = 0; ci < 3; ci++) {
						c = rci[ci];
						if (grid[1][c] == ' ') {
							if ( (grid[0][c] == whichPlayer) && (grid[2][c] == ' ') ) {
								return findFreeCellIdx(2, c);
							}
							if ( (grid[2][c] == whichPlayer) && (grid[0][c] == ' ') ) {
								return findFreeCellIdx(0, c);
							}
						}
					}
				}
			}
		}
		return -1;
	}

	// Try to set up a diagonal win situation.
	// Returns index into freeRow[], freeCol[] if successful, or -1 if failure.
	private int tryDiagWinSetup(char whichPlayer) {
		if (grid[1][1] == ' ') {
			if ( (!randomizeMoves) || (rand.nextInt(2) == 0) ) {
				// Try to take the opposing corner.
				int[] steps = randomizeMoves ? orders4[rand.nextInt(orders4.length)] : orders4[0];
				for (int si = 0; si < 4; si++) {
					switch (steps[si]) {
					case 0:
						if ( (grid[0][0] == whichPlayer) && (grid[2][2] == ' ') ) {
							return findFreeCellIdx(2, 2);
						}
						break;
					case 1:
						if ( (grid[2][2] == whichPlayer) && (grid[0][0] == ' ') ) {
							return findFreeCellIdx(0, 0);
						}
						break;
					case 2:
						if ( (grid[0][2] == whichPlayer) && (grid[2][0] == ' ') ) {
							return findFreeCellIdx(2, 0);
						}
						break;
					case 3:
						if ( (grid[2][0] == whichPlayer) && (grid[0][2] == ' ') ) {
							return findFreeCellIdx(0, 2);
						}
						break;
					}
				}
			} else {
				// Try to take the center.
				if (   ( (grid[0][0] == whichPlayer) && (grid[2][2] == ' ') )
					|| ( (grid[2][2] == whichPlayer) && (grid[0][0] == ' ') )
					|| ( (grid[0][2] == whichPlayer) && (grid[2][0] == ' ') )
					|| ( (grid[2][0] == whichPlayer) && (grid[0][2] == ' ') )   ) {
					return findFreeCellIdx(1, 1);
				}
			}
		}
		return -1;
	}

	private void findForkCells(char whichPlayer) {
		char opponent = (whichPlayer == 'X') ? 'O' : 'X';
		forkCount = 0;
		int fr, fc, r, c, myCount, opponentCount, setupCount, rdir, cdir, i;
		for (int freeIdx = 0; freeIdx < freeCount; freeIdx++) {
			fr = freeRow[freeIdx];
			fc = freeCol[freeIdx];
			setupCount = 0;
			myCount = opponentCount = 0;
			for (r = 0; r < 3; r++) {
				if (grid[r][fc] == whichPlayer) {
					myCount++;
				} else if (grid[r][fc] == opponent) {
					opponentCount++;
				}
			}
			if ( (myCount > 0) && (opponentCount == 0) ) setupCount++;
			myCount = opponentCount = 0;
			for (c = 0; c < 3; c++) {
				if (grid[fr][c] == whichPlayer) {
					myCount++;
				} else if (grid[fr][c] == opponent) {
					opponentCount++;
				}
			}
			if ( (myCount > 0) && (opponentCount == 0) ) setupCount++;
			if ( ( (fr == 0) || (fr == 2) ) && ( (fc == 0) || (fc == 2) ) ) {
				rdir = (fr == 0) ? 1 : -1;
				cdir = (fc == 0) ? 1 : -1;
				r = fr;
				c = fc;
				myCount = opponentCount = 0;
				for (i = 0; i < 3; i++, r += rdir, c += cdir) {
					if (grid[r][c] == whichPlayer) {
						myCount++;
					} else if (grid[r][c] == opponent) {
						opponentCount++;
					}
				}
				if ( (myCount > 0) && (opponentCount == 0) ) setupCount++;
			}
			if (setupCount >= 2) {
				forkFreeIdx[forkCount] = freeIdx;
				forkSetupCount[forkCount] = setupCount;
				forkCount++;
			}
		}
	}
}
