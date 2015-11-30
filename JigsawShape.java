package edu.umb.cs443.sudokubasic;

import java.util.Random;
import java.util.Scanner;

/** http://www.sudokuwiki.org/Jigsaw_Shape_List */

//only 3x3 jigsaw shape definitions for now.
//JigsawShapes commented out seem to take too long to generate, on a few trials.

/** note all 9s are replaced by 0s */

public enum JigsawShape {
	//MoonLotus(81, "1 2 2 2 2 3 4 4 4 1 1 2 2 2 3 3 4 4 1 1 1 2 2 3 4 4 4 1 5 1 1 3 3 3 8 4 5 5 5 6 6 3 3 8 8 5 5 5 6 6 7 7 7 8 5 5 0 6 6 7 7 7 8 0 0 0 0 6 6 7 8 8 0 0 0 0 6 7 7 8 8",
	//		1, 0, 1, 2, 3, 2, 3, 0, 1),
	Onion(81, "1 1 2 2 2 2 2 3 3 1 1 1 2 2 2 3 3 3 4 1 1 1 2 3 3 3 6 4 4 1 5 5 5 3 6 6 4 4 4 5 5 5 6 6 6 4 4 7 5 5 5 0 6 6 4 7 7 7 8 0 0 0 6 7 7 7 8 8 8 0 0 0 7 7 8 8 8 8 8 0 0",
			0, 0, 1, 0, 2, 3, 2, 0, 1),
	Double_Mirror(81, "1 1 1 1 1 2 2 2 2 1 1 3 3 4 5 5 2 2 1 3 3 4 4 4 5 5 2 1 3 4 4 4 4 4 5 2 6 3 7 7 7 7 7 5 2 6 3 3 7 7 7 5 5 0 6 3 8 8 7 8 8 5 0 6 6 8 8 8 8 8 0 0 6 6 6 6 0 0 0 0 0",
			0, 0, 1, 2, 3, 2, 1, 1, 0, 3),
	Cyndie(81, "1 1 1 1 5 2 2 2 2 1 1 1 5 5 5 2 2 2 1 7 5 5 5 5 5 8 2 1 7 7 0 0 0 8 8 2 7 7 7 0 0 0 8 8 8 4 7 7 0 0 0 8 8 3 4 7 6 6 6 6 6 8 3 4 4 4 6 6 6 3 3 3 4 4 4 4 6 3 3 3 3",
			0, 0, 1, 0, 1, 2, 2, 3, 3),
	Tornado(81, "1 1 1 2 2 2 3 3 3 1 1 1 1 2 3 3 3 3 4 1 1 2 2 2 3 3 6 4 4 5 5 2 5 5 6 6 4 4 4 5 2 5 6 6 6 4 4 4 5 5 5 6 6 6 7 7 7 7 8 0 0 0 0 7 7 7 8 8 8 0 0 0 7 7 8 8 8 8 8 0 0",
			2, 0, 1, 2, 2, 3, 0, 0, 1),
	Cross(81, "1 1 1 1 2 3 3 3 3 1 1 1 2 2 4 3 3 3 1 2 2 2 4 4 4 4 3 1 2 5 5 4 4 4 4 3 2 2 5 5 5 5 5 6 6 7 8 8 8 8 5 5 6 0 7 8 8 8 8 6 6 6 0 7 7 7 8 6 6 0 0 0 7 7 7 7 6 0 0 0 0",
			2, 0, 1, 2, 3, 2, 1, 0, 3),
	Worm(81, "1 8 8 8 8 8 0 0 6 1 1 8 8 8 0 0 6 6 4 1 1 8 0 0 6 6 6 4 4 1 1 0 0 6 6 7 4 4 4 1 1 0 6 7 7 4 4 3 3 2 2 7 7 7 4 3 3 3 5 2 2 7 7 3 3 3 5 5 5 2 2 7 3 5 5 5 5 5 2 2 2",
			2, 0, 1, 2, 3, 3, 3, 0, 1),
	Cabbage(81, "1 1 2 2 2 2 3 3 3 1 1 1 2 2 5 3 3 3 1 1 2 2 2 5 6 3 3 1 1 4 4 5 5 6 6 3 4 4 4 4 5 6 6 6 6 7 4 4 5 5 6 6 0 0 7 7 4 5 8 8 8 0 0 7 7 7 5 8 8 0 0 0 7 7 7 8 8 8 8 0 0",
			3, 0, 1, 2, 3, 0, 1, 1, 2),
	//Stripes(81, "4 4 4 4 5 5 5 5 5 4 4 4 4 3 5 5 5 6 0 0 4 2 3 3 5 6 6 0 0 1 2 2 3 3 3 6 0 1 1 2 2 2 3 3 6 0 1 1 1 2 2 3 6 6 0 0 8 1 1 2 7 6 6 0 8 8 8 1 7 7 7 7 8 8 8 8 8 7 7 7 7",
	//		2, 0, 1, 2, 3, 0, 1, 3, 1),
	H(81, "1 1 1 2 2 2 3 3 3 1 1 1 2 2 2 3 3 3 4 1 1 5 2 2 6 3 3 4 4 1 5 5 2 6 6 3 4 4 4 5 5 5 6 6 6 7 4 4 8 5 5 0 6 6 7 7 4 8 8 5 0 0 6 7 7 7 8 8 8 0 0 0 7 7 7 8 8 8 0 0 0",
			3, 0, 1, 2, 3, 2, 0, 0, 1),
	//Wednesday(81, "1 1 1 2 2 3 3 3 3 1 1 2 2 2 3 6 3 3 1 1 2 2 2 5 6 3 3 4 1 1 5 2 5 6 6 6 4 4 4 5 5 5 6 6 6 4 4 4 5 8 5 0 0 6 7 7 4 5 8 8 8 0 0 7 7 4 7 8 8 8 0 0 7 7 7 7 8 8 0 0 0",
	//		1, 0, 1, 2, 2, 3, 0, 0, 2),
	//ZigZag(81, "1 1 1 2 2 2 2 2 3 1 1 1 1 2 2 2 3 3 4 4 1 1 5 2 3 3 3 4 4 4 5 5 5 6 3 3 7 4 4 4 5 6 6 6 3 7 7 4 5 5 5 6 6 6 7 7 7 8 5 0 0 6 6 7 7 8 8 8 0 0 0 0 7 8 8 8 8 8 0 0 0",
	//		1, 0, 1, 2, 2, 3, 0, 0, 2)
			;
	
	public final int[] REGIONS, COLORS;
	
	JigsawShape(int size, String text, int ... colorAssignments) {
		REGIONS = new int[size];
		COLORS = colorAssignments;
		
		Scanner scan = new Scanner(text); int i = 0;
		while(scan.hasNextInt())
			REGIONS[i++] = scan.nextInt();
		scan.close();
	}
	
	public static JigsawShape random() {
		JigsawShape[] allShapes = JigsawShape.values();
		return allShapes[new Random().nextInt(allShapes.length)];
	}
}
