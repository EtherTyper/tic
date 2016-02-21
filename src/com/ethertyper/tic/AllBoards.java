// AllBoards.java
// Copyright (c) 2007-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.tic;

import java.io.*;
import java.util.*;

public class AllBoards {
	public static char[][][] getAllBoards() {
		List v = new Vector();
		BufferedReader in = null;
		try {
			String resourceName =
				AllBoards.class.getPackage().getName().replace('.', File.separatorChar)+
				"/allboards.txt";
			in = new BufferedReader(new InputStreamReader
				(AllBoards.class.getClassLoader().getResourceAsStream(resourceName)));
			String line;
			while (true) {
				if ((line = in.readLine()) == null) break;
				if (line.length() == 0) continue;
				if (line.length() != 9) {
					throw new Exception("Unexpected line length: "+line.length()+" (should be 9)");
				}

				char[][] grid = new char[3][3];
				grid[0][0] = line.charAt(0);
				grid[0][1] = line.charAt(1);
				grid[0][2] = line.charAt(2);
				grid[1][0] = line.charAt(3);
				grid[1][1] = line.charAt(4);
				grid[1][2] = line.charAt(5);
				grid[2][0] = line.charAt(6);
				grid[2][1] = line.charAt(7);
				grid[2][2] = line.charAt(8);

				v.add(grid);
			}
			return (char[][][])v.toArray(new char[v.size()][][]);
		} catch (Exception ex) {
			throw new Error(ex);
		} finally {
			if (in != null) {
				try { in.close(); } catch (Exception ex) {}
			}
		}
	}

	// [0][0] [0][1] [0][2] -> [0][2] [1][2] [2][2]
	// [1][0] [1][1] [1][2] -> [0][1] [1][1] [2][1]
	// [2][0] [2][1] [2][2] -> [0][0] [1][0] [2][0]
	public static void rotate90CCWGrid(char[][] grid) {
		char tmp = grid[0][0];
		grid[0][0] = grid[0][2];
		grid[0][2] = grid[2][2];
		grid[2][2] = grid[2][0];
		grid[2][0] = tmp;

		tmp = grid[0][1];
		grid[0][1] = grid[1][2];
		grid[1][2] = grid[2][1];
		grid[2][1] = grid[1][0];
		grid[1][0] = tmp;
	}

	// [0][0] [0][1] [0][2] -> [0][2] [1][2] [2][2]
	// [1][0] [1][1] [1][2] -> [0][1] [1][1] [2][1]
	// [2][0] [2][1] [2][2] -> [0][0] [1][0] [2][0]
	public static void rotate90CCWRowColInts(int[] rows, int[] cols, int count) {
		int r, c;
		for (int i = 0; i < count; i++) {
			r = 2-cols[i];
			c = rows[i];
			rows[i] = r;
			cols[i] = c;
		}
	}

	public static int unrotate90CCWMove(int move) {
		switch (move) {
		case 0: return 2;
		case 2: return 8;
		case 8: return 6;
		case 6: return 0;
		case 1: return 5;
		case 5: return 7;
		case 7: return 3;
		case 3: return 1;
		default: return move;
		}
	}

	public static void flipHGrid(char[][] grid) {
		char tmp = grid[0][0];
		grid[0][0] = grid[0][2];
		grid[0][2] = tmp;

		tmp = grid[1][0];
		grid[1][0] = grid[1][2];
		grid[1][2] = tmp;

		tmp = grid[2][0];
		grid[2][0] = grid[2][2];
		grid[2][2] = tmp;
	}

	public static void flipHColInts(int[] cols, int count) {
		for (int i = 0; i < count; i++) {
			if (cols[i] == 0) {
				cols[i] = 2;
			} else if (cols[i] == 2) {
				cols[i] = 0;
			}
		}
	}

	public static int unflipHMove(int move) {
		switch (move) {
		case 0: return 2;
		case 2: return 0;
		case 3: return 5;
		case 5: return 3;
		case 6: return 8;
		case 8: return 6;
		default: return move;
		}
	}

	public static void main(String[] args) {
		char[][][] allBoards = AllBoards.getAllBoards();
		for (int i = 0; i < allBoards.length; i++) {
			for (int r = 0; r < 3; r++) {
				for (int c = 0; c < 3; c++) {
					System.out.print(allBoards[i][r][c]);
				}
				System.out.println();
			}
			System.out.println();
		}
		System.out.println("Total boards: "+allBoards.length);
	}
}
