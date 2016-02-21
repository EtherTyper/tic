// HumanPlayer.java
// Copyright (c) 2016 Eli Joseph Bradley
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.
//
// NeuralPlayer.java
// Copyright (c) 2007-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.ethertyper.tic;

import com.ethertyper.neural.*;
import java.io.*;
import java.util.*;

public class HumanPlayer extends Player {

	public HumanPlayer() { }

	public String encodeNN() {
		return "(^_^) -<\"I play Tic tac toe!\">";
	}

	private char[][] tmpGrid = new char[3][3];
	private int[] tmpFreeRow = new int[9], tmpFreeCol = new int[9];

	public synchronized int chooseNextMove(char[][] grid, char whichPlayer) {
        String visual="";
        // 1: +-+-+-+
        // 2: | | | |
        // 3: +-+-+-+
        // 4: | | | |
        // 5: +-+-+-+
        for (int i=0; i<grid.length; i++){
            visual+=oddLine(grid[i]);
            visual+=evenLine(grid[i]);
            if (i==grid[i].length-1)
                visual+=oddLine(grid[i]);
        }
        int choice=0;
        do {
            System.out.println(visual+"\nPlant "+whichPlayer+" in... ");
            Scanner scan=new Scanner(System.in);
            choice=(scan.nextInt()%3)*3+(scan.nextInt()%3);
        } while (grid[choice/3][choice%3]!=' ');
        return choice;
		// throw new InternalError("Unexpected grid");
	}
    
    public synchronized String oddLine(char[] grid){
        String visual="";
        for (int j=0; j<grid.length; j++){
            visual+="+-";
            if (j==grid.length-1)
                visual+="+\n";
        }
        return visual;
    }
    
    public synchronized String evenLine(char[] grid){
        String visual="";
        for (int j=0; j<grid.length; j++){
            visual+="|"+grid[j];
            if (j==grid.length-1)
                visual+="|\n";
        }
        return visual;
    }
}
