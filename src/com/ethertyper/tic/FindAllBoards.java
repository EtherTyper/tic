// FindAllBoards.java
// Copyright (c) 2007-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.tic;

import java.util.*;

public class FindAllBoards {
	private static class Board {
		public final char[] grid = new char[9];

		public Board(char[] grid) {
			System.arraycopy(grid, 0, this.grid, 0, 9);
		}

		public void copyTo(Board dest) {
			System.arraycopy(grid, 0, dest.grid, 0, 9);
		}

		public boolean equals(Board other) {
			for (int i = 0; i < 9; i++) {
				if (grid[i] != other.grid[i]) return false;
			}
			return true;
		}

		// 012 -> 258
		// 345    147
		// 678    036
		private void rotate90CCW() {
			char[] rotFlipBuf = new char[9];
			System.arraycopy(grid, 0, rotFlipBuf, 0, 9);
			grid[0] = rotFlipBuf[2];
			grid[1] = rotFlipBuf[5];
			grid[2] = rotFlipBuf[8];
			grid[5] = rotFlipBuf[7];
			grid[8] = rotFlipBuf[6];
			grid[7] = rotFlipBuf[3];
			grid[6] = rotFlipBuf[0];
			grid[3] = rotFlipBuf[1];
		}

		// 012 -> 210
		// 345    543
		// 678    876
		private void flipH() {
			char[] rotFlipBuf = new char[9];
			System.arraycopy(grid, 0, rotFlipBuf, 0, 9);
			grid[0] = rotFlipBuf[2];
			grid[3] = rotFlipBuf[5];
			grid[6] = rotFlipBuf[8];
			grid[2] = rotFlipBuf[0];
			grid[5] = rotFlipBuf[3];
			grid[8] = rotFlipBuf[6];
		}

		public String toHumanReadable() {
			String nl = System.getProperty("line.separator");
			StringBuffer sb = new StringBuffer();
			for (int r = 0; r < 3; r++) {
				if (r > 0) {
					sb.append("-+-+-");
					sb.append(nl);
				}
				sb.append(grid[(r*3)]);
				sb.append('|');
				sb.append(grid[(r*3)+1]);
				sb.append('|');
				sb.append(grid[(r*3)+2]);
				sb.append(nl);
			}
			return sb.toString();
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < 9; i++) sb.append(grid[i]);
			return sb.toString();
		}
	}

	public static Board[] findAllBoards() {
		List allBoards = new ArrayList();
		char[] grid = new char[9];

		Board testBoard = new Board(grid);

		// Each element = 0 for space, 1 for X, 2 for O.
		int[] cellVal = new int[9];
		for (boolean done = false; !done;) {
			for (int i = 0; i < 9; i++) {
				switch (cellVal[i]) {
				case 0:
					grid[i] = ' ';
					break;
				case 1:
					grid[i] = 'X';
					break;
				case 2:
					grid[i] = 'O';
					break;
				}
			}
			for (int i = 0; i < 9; i++) {
				cellVal[i]++;
				if (cellVal[i] < 3) break;
				cellVal[i] = 0;
				if (i == 8) done = true;
			}

			// Don't save any winning boards.
			boolean isWin = false;
			for (int rc = 0; rc < 3; rc++) {
				if (   (grid[(rc*3)] != ' ')
					&& (grid[(rc*3)] == grid[(rc*3)+1])
					&& (grid[(rc*3)] == grid[(rc*3)+2])   ) {
					isWin = true;
					break;
				}
				if (   (grid[rc] != ' ')
					&& (grid[rc] == grid[3+rc])
					&& (grid[rc] == grid[6+rc])   ) {
					isWin = true;
					break;
				}
			}
			if (isWin) {
				continue;
			}
			if (   (grid[0] != ' ')
				&& (grid[0] == grid[4])
				&& (grid[0] == grid[8])   ) {
				isWin = true;
			} else {
				if (   (grid[2] != ' ')
					&& (grid[2] == grid[4])
					&& (grid[2] == grid[6])   ) {
					isWin = true;
				}
			}
			if (isWin) {
				continue;
			}

			// Don't save any full boards, or any boards where one player is more than
			// one move ahead of the other player.
			boolean isFull = true;
			int xMoves = 0, oMoves = 0;
			for (int i = 0; i < 9; i++) {
				switch (grid[i]) {
				case 'X':
					xMoves++;
					break;
				case 'O':
					oMoves++;
					break;
				default:
					isFull = false;
					break;
				}
			}
			if ( (isFull) || (Math.abs(xMoves-oMoves) > 1) ) {
				continue;
			}

			// Don't save any boards which are duplicates of other boards,
			// including rotated and/or flipped duplicates.
			Board board = new Board(grid);
			board.copyTo(testBoard);
			boolean isDup = false;
			for (int flipPass = 1; ((flipPass <= 2) && (!isDup)); flipPass++) {
				for (int rotPass = 1; ((rotPass <= 4) && (!isDup)); rotPass++) {
					for (int i = 0, n = allBoards.size(); i < n; i++) {
						if (testBoard.equals((Board)allBoards.get(i))) {
							isDup = true;
							break;
						}
					}
					testBoard.rotate90CCW();
				}
				testBoard.flipH();
			}
			if (isDup) {
				continue;
			}

			allBoards.add(board);
/*
System.err.print(allBoards.size());
System.err.print(": ");
System.err.print(startPlayer);
for (int i = 0; i < playCount; i++) {
	System.err.print(",");
	System.err.print(playPos[i]);
}
System.err.println();
*/
		}


/*
		for (int playCount = 1; playCount < 9; playCount++) {
			for (boolean done = false; !done;) {
				boolean anyDupPlays = false;
				for (int i = 1; ( (i < playCount) && (!anyDupPlays) ); i++) {
					for (int j = 0; j < i; j++) {
						if (playPos[i] == playPos[j]) {
							anyDupPlays = true;
							break;
						}
					}
				}
				if (!anyDupPlays) {
					for (int startPass = 1; startPass <= 2; startPass++) {
						char startPlayer = (startPass == 1) ? 'X' : 'O';
						for (int i = 0; i < 9; i++) grid[i] = ' ';
						char player = startPlayer;
						for (int plays = 0;
							 plays < playCount;
							 plays++, player = (player == 'X') ? 'O' : 'X') {
							grid[playPos[plays]] = player;
							// Don't save any winning boards.
							boolean isWin = false;
							for (int rc = 0; rc < 3; rc++) {
								if (   (grid[(rc*3)+0] != ' ')
									&& (grid[(rc*3)+0] == grid[(rc*3)+1])
									&& (grid[(rc*3)+0] == grid[(rc*3)+2])   ) {
									isWin = true;
									break;
								}
								if (   (grid[(0*3)+rc] != ' ')
									&& (grid[(0*3)+rc] == grid[(1*3)+rc])
									&& (grid[(0*3)+rc] == grid[(2*3)+rc])   ) {
									isWin = true;
									break;
								}
							}
							if (!isWin) {
								if (   (grid[(0*3)+0] != ' ')
									&& (grid[(0*3)+0] == grid[(1*3)+1])
									&& (grid[(0*3)+0] == grid[(2*3)+2])   ) {
									isWin = true;
								} else {
									if (   (grid[(0*3)+2] != ' ')
										&& (grid[(0*3)+2] == grid[(1*3)+1])
										&& (grid[(0*3)+2] == grid[(2*3)+0])   ) {
										isWin = true;
									}
								}
							}
							if (!isWin) {
								// Don't save any boards which are duplicates of other boards,
								// including rotated and/or flipped duplicates.
								Board board = new Board(grid);
								boolean isDup = false;
								for (int flipPass = 1; ((flipPass <= 2) && (!isDup)); flipPass++) {
									for (int rotPass = 4; ((rotPass <= 4) && (!isDup)); rotPass++) {
										for (int i = 0, n = allBoards.size(); i < n; i++) {
											if (board.equals((Board)allBoards.get(i))) {
												isDup = true;
												break;
											}
										}
										board.rotate90CCW();
									}
									board.flipH();
								}
								if (!isDup) {
									allBoards.add(board);
System.err.print(allBoards.size());
System.err.print(": ");
System.err.print(startPlayer);
for (int i = 0; i < playCount; i++) {
	System.err.print(",");
	System.err.print(playPos[i]);
}
System.err.println();
								}
							}
						}
					}
				}	// if (!anyDupPlays)
				for (int i = playCount-1; ; i--) {
					if (i < 0) {
						done = true;
						break;
					}
					playPos[i]++;
					if (playPos[i] < 9) break;
					playPos[i] = 0;
				}
			}	// for (boolean done = false; !done;)
		}	// for (int playCount = 1; playCount < 9; playCount++)
*/
		return (Board[])allBoards.toArray(new Board[allBoards.size()]);
	}

	public static void main(String[] args) {
		Board[] boards = findAllBoards();
		for (int i = 0; i < boards.length; i++) {
			System.out.println(boards[i]);
		}
///
System.err.println("Total unique boards: "+boards.length);
	}
}
