// NeuralTic.java
// Copyright (c) 2016 Eli Joseph Bradley
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.
//
// NeuralTic.java
// Copyright (c) 2007-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.ethertyper.tic;

import java.io.*;
import java.util.*;

public class NeuralTic {
	public static void main(String[] args) {
		NeuralPlayer neuralPlayer = new NeuralPlayer();
		HumanPlayer human= new HumanPlayer();

		// Do training.
		System.out.println("Learning...");
		for (int pass = 0; ; pass++) {
			neuralPlayer.trainAllBoardsOnePass();
			int npass = neuralPlayer.getNumPassedMoves();
			int nfail = neuralPlayer.getNumFailedMoves();
			System.out.println("train pass="+pass+" npass="+npass+" nfail="+nfail);
			if (nfail == 0) break;
			if (pass%200==0) {
				Tic tic = new Tic(neuralPlayer, human);
				tic.playGame(null, false, false);
				tic.showGrid(System.out);
				switch (tic.getWinner()) {
					case 'X':
						System.out.println("It won.");
						break;
					case 'O':
						System.out.println("You won.");
						break;
					default:
						System.out.println("Nobody won.");
						break;
				}
				new Scanner(System.in).nextLine();
			}
		}

		System.out.println("\n\n\n");
		System.out.print("Show encoded network? ");
		if (Character.toLowerCase(new Scanner(System.in).next().charAt(0))=='y')
			System.out.println("\n\n\n"+neuralPlayer.encodeNN());
	}
}
