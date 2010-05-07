// GeneticTic2.java
// Copyright (c) 2007-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.tic;

import java.io.*;

public class GeneticTic2 {
	public static void main(String[] args) {
		GeneticPlayer2 geneticPlayer = new GeneticPlayer2();

		// Do training.
		System.out.println("Learning...");
		for (int freeCount = 1; freeCount <= 9; freeCount++) {
			geneticPlayer.train(freeCount);
			System.out.println("Trained free count "+freeCount);
		}

		// Do competition.
		int numTestPasses = 100000;
		for (int whichOpponent = 1; whichOpponent <= 2; whichOpponent++) {
			Player opponent = (whichOpponent == 1) ? new IdealPlayer() : new RandomPlayer();
			System.out.println("Competing against "+opponent.getClass().getName()+"...");
			Tic tic = new Tic(geneticPlayer, opponent);
			int xWinCount = 0, oWinCount = 0, nWinCount = 0;
			for (int pass = 0; pass < numTestPasses; pass++) {
				tic.playGame(null, false, false);
				switch (tic.getWinner()) {
				case 'X':
					xWinCount++;
					break;
				case 'O':
					oWinCount++;
					break;
				default:
					nWinCount++;
					break;
				}
			}
			// Output test result counts.
			System.out.println("X won: "+xWinCount);
			System.out.println("O won: "+oWinCount);
			System.out.println("Nobody won: "+nWinCount);
		}
	}
}
