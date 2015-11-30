package edu.umb.cs443.sudokubasic;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class Sudoku {
	protected Cell[] puzzle;
	protected int size, n;
	
	public final String ID, OVERRIDE_ID;
	protected int cellWidth;
	protected static int idCounter = 0;
	
	protected Map<Integer, Set<Cell>> regionMapping;
	protected final Set<Integer> RCR_TO_MATCH;
	
	protected JigsawShape jigsawShape = null;
	
	public Sudoku(int n, int[] values) {
		this(n, values, generateBoxRegions(n*n));
	}
	
	protected static int[] generateBoxRegions(int size) {
		int[] regionNumbers = new int[size*size];
		int n = (int) Math.sqrt(size);
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				regionNumbers[i*size + j] = n*(i / n) + (j / n);
		return regionNumbers;
	}
	
	public Sudoku(int n, int[] values, JigsawShape jigsawShape) {
		this(n, values, jigsawShape.REGIONS);
		this.jigsawShape = jigsawShape;
		System.out.println(jigsawShape.name());
	}
	
	public Sudoku(int n, int[] values, int[] regionNumbers) {
		this.n = n;
		this.size = n*n;
		if(values.length != size*size)
			throw new IllegalArgumentException("Values must be a " + (size*size) + " length array");
		if(regionNumbers.length != size*size)
			throw new IllegalArgumentException("Region mappings must be a " + (size*size) + " length array");
		
		this.ID = "su_id_" + idCounter;
		this.OVERRIDE_ID = "su_override" + idCounter;
		idCounter++;
		
		this.cellWidth = 1 + (int) Math.log10((double) size);
		
		this.regionMapping = new HashMap<>(size*2);
		
		Set<Integer> si = new HashSet<>(size*2);
		for(int i = 1; i <= size; i++) {
			regionMapping.put(i-1, new LinkedHashSet<Cell>(size*2));
			si.add(i);
		}
		
		RCR_TO_MATCH = Collections.unmodifiableSet(si);
		
		puzzle = new Cell[size*size];
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++) {
				int regionNumber = regionNumbers[i*size + j];
				
				if(regionNumber < 0 || regionNumber >= size)
					throw new IllegalArgumentException("Region values for each cell must be labeled from 0 to " + (size-1));
				if(values[i*size + j] < 0 || values[i*size + j] > size)
					throw new IllegalArgumentException("Values for each cell must be labeled as 0 (not known yet) or 1 to " + size);
				
				Cell c = new Cell(this, values[i*size + j], i, j, regionNumber);
				puzzle[i*size + j] = c;
				regionMapping.get(regionNumber).add(c);
			}
	}
	
	//copy constructor
	public Sudoku(Sudoku s) {
		this.puzzle = new Cell[s.puzzle.length];
		this.size = s.size;
		this.n = s.n;
		this.cellWidth = s.cellWidth;
		this.regionMapping = new HashMap<>(s.regionMapping);
		this.RCR_TO_MATCH = Collections.unmodifiableSet(s.RCR_TO_MATCH);
		this.ID = "su_id_" + idCounter++;	//still unique ID
		this.OVERRIDE_ID = "su_override_id" + idCounter++;	//still unique override ID
		
		for(int i = 0; i < s.puzzle.length; i++)
			this.puzzle[i] = new Cell(this, s.puzzle[i]);
	}
	
	public Cell[] getRow(int row) {
		if(row < 0 || row >= size)
			return new Cell[0];
		
		return Arrays.copyOfRange(puzzle, row*size, (row+1)*size);
	}
	
	public Cell[] getRowOf(Cell c) {
		if(!c.getID().equals(this.ID))
			return new Cell[0];
		
		return getRow(c.ROW);
	}
	
	public Cell[] getColumn(int column) {
		if(column < 0 || column >= size)
			return new Cell[0];
		
		Cell[] columnA = new Cell[size];
		for(int i = 0; i < size; i++)
			columnA[i] = puzzle[i*size + column];
		
		return columnA;
	}
	public Cell[] getColumnOf(Cell c) {
		if(!c.getID().equals(this.ID))
			return new Cell[0];
		
		return getColumn(c.COLUMN);
	}
	
	public Set<Cell> getRegionOf(Cell c) {
		if(!c.getID().equals(this.ID))
			return Collections.emptySet();
		
		return regionMapping.get(c.REGION);
	}
	
	public Set<Cell> getAffectedCells(Cell c) {
		if(!c.getID().equals(this.ID))
			return Collections.emptySet();
		
		Set<Cell> ac = new LinkedHashSet<>(3*(size-1));
		
		for(Cell c2 : getRowOf(c))
			ac.add(c2);
		for(Cell c2 : getColumnOf(c))
			ac.add(c2);
		ac.addAll(getRegionOf(c));
		
		ac.remove(c);
		return ac;
	}
	
	public void updatePossibilities(Cell c) {
		for(Cell c2 : getAffectedCells(c))
			if(c2.getValue() == 0)
				c2.eliminate(c.getValue());
	}
	
	public boolean isSolved() {
		for(Cell[] nArray : this.puzzleIterator()) {
			Set<Integer> si = new HashSet<>(size*2);
			for(Cell c : nArray)
				si.add(c.getValue());
			if(!si.equals(RCR_TO_MATCH))
				return false;
		}
		
		return true;
	}
	
	//in practice, no way we count 9 mil possibilities
	protected int totalSolutionCount() {
		return countSolutions(0, 0, Integer.MAX_VALUE);
	}
	
	protected int solutionCount(int limit) {
		return countSolutions(0, 0, limit);
	}
	
	//effective for 3x3 solution counting, 4x4 and above not so much unless with a low limit.
	protected int countSolutions(int row, int column, int limit) {
		if(row == size && column == 0)
			return 1;	//a true solution
		Cell c = getCellAt(row, column);
		
		if(c.getValue() != 0) {
			return countSolutions(row + ((column + 1)/size), (column + 1) % size, limit);
		} else {
			Set<Cell> affectedCells = getAffectedCells(c);
			List<Integer> candidates = new LinkedList<>(RCR_TO_MATCH);
			Collections.shuffle(candidates);
			int solutionCount = 0;
			
			long start = System.currentTimeMillis();
			numberLoop: for(int j = 0; j < size && System.currentTimeMillis() - start < 100L; j++) {
				int i = candidates.get(j);
				for(Cell c2 : affectedCells)
					if(c2.getValue() == i)
						continue numberLoop;
				
				c.setValue(i);
				solutionCount += countSolutions(row + ((column + 1)/size), (column + 1) % size, limit);
				c.setValue(0);
				
				if(solutionCount >= limit)
					break;
			}
			
			return Math.min(solutionCount, limit);	//tried all combos including and past this cell, return.
		}
	}
	
	/** same as above, but take first solution as the answer (perhaps assuming above, or we know from outside, that it returns 1)
		and set the puzzle state as fully solved
		effective for any difficulty on 3x3, when partially filled in by techniques below on 4x4, should still be effective. no guarantee on 5x5. */
	protected boolean smartSolve(int row, int column) {
		if(row == size && column == 0)
			return true;
		Cell c = getCellAt(row, column);
		
		if(c.getValue() != 0) {
			return smartSolve(row + ((column + 1)/size), (column + 1) % size);
		} else {
			Set<Cell> affectedCells = getAffectedCells(c);
			
			List<Integer> candidates = new LinkedList<>(RCR_TO_MATCH);
			//in the case of multiple solutions, this roughly randomizes which solution is ultimately set
			Collections.shuffle(candidates);
			
			numberLoop: for(int i : candidates) {
				for(Cell c2 : affectedCells)
					if(c2.getValue() == i)
						continue numberLoop;
				
				c.setValue(i);
				
				if(smartSolve(row + ((column + 1)/size), (column + 1) % size))
					return true;
				
				c.setValue(0);
			}
			return false;
		}
	}
	
	public boolean solve() {
		boolean anyInfo = true;
		
		//set up possibilities
		for(Cell c : puzzle)
			updatePossibilities(c);
		
		do {
			anyInfo = false;
			
			//naked singles
			for(Cell c : puzzle)
				if(c.checkAndSetValue()) {
					updatePossibilities(c);
					anyInfo = true;
				}
			
			for(Cell[] nArray : this.puzzleIterator()) {
				//hidden pairs
				LinkedList<Cell> unknownCells = new LinkedList<>();
				
				for(Cell c : nArray)
					if(c.getValue() == 0)
						unknownCells.add(c);
				
				for(int i = 1; i <= size; i++) {
					int j = 0; LinkedList<Cell> cellsWithI = new LinkedList<>();
					
					for(Cell c : unknownCells)
						if(c.getPossibilities().contains(i)) {
							cellsWithI.add(c);
							j++;
						}
					
					if(j == 1) {
						Cell setC = cellsWithI.pop();
						setC.setValue(i);
						updatePossibilities(setC);
						unknownCells.remove(setC);
						anyInfo = true;
					}
						
				}
				
				/*
				//naked pairs
				if(unknownCells.size() >= 3) {
					Queue<Cell> cc1 = new LinkedList<>(unknownCells);
					while(cc1.peek() != null) {
						Cell c1 = cc1.poll();
						Queue<Cell> cc2 = new LinkedList<>(cc1);
						while(cc2.peek() != null) {
							Cell c2 = cc2.poll();
							Set<Integer> s1 = c1.getPossibilities(), s2 = c2.getPossibilities();
							if(s1.equals(s2) && s1.size() == 2) {
								System.out.println(Arrays.toString(nArray) + "\n\t" + c1 + c2);
								LinkedList<Cell> otherUnknownCells = new LinkedList<>(unknownCells);
								otherUnknownCells.remove(c1);
								otherUnknownCells.remove(c2);
								
								for(Cell c3 : otherUnknownCells)
									for(int i : s1)
										c3.eliminate(i);
								System.out.println("\t" + Arrays.toString(nArray) + "\n");
								anyInfo = true;
							}
						}
					
					}
				}
				*/
			}
		} while(anyInfo);
		
		if(!isSolved())	
			smartSolve(0, 0);	//if needed, resort to more algorithmic techniques at this point
		
		return isSolved();
	}
	
	protected void depopulateTo(int cellsLeft) {
		depopulateTo(cellsLeft, new Random());
	}
	
	protected void depopulateTo(int cellsLeft, Random seed) {
		if(cellsLeft >= puzzle.length)
			return;
		
		List<Cell> populatedCells = new LinkedList<>(Arrays.asList(puzzle)), unpopulatedCells = new LinkedList<>();
		
		for(Cell c : populatedCells)
			if(c.getValue() == 0)
				unpopulatedCells.add(c);
		
		populatedCells.removeAll(unpopulatedCells);
		
		while(populatedCells.size() > cellsLeft) {
			Cell c = populatedCells.get(seed.nextInt(populatedCells.size()));
			int value = c.getValue();
			c.setValue(0);
			if(solutionCount(2) == 1) 
				populatedCells.remove(c);
			else
				c.setValue(value);
		}
	}
	
	public int size() {
		return size;
	}
	
	public JigsawShape getJigsawShape() {
		return jigsawShape;
	}
	
	public Cell[] getPuzzle() {
		return puzzle;
	}
	
	public String symbolString() {
		String s = "";
		for(int i = 1; i <= size; i++)
			s += i;
		return s;
	}
	
	public Cell getCellAt(int row, int column) {
		return puzzle[row*size + column];
	}
	
	public String getCellValue(int row, int column) {
		int value = puzzle[row*size + column].getValue();
		return value > 0 ? String.valueOf(value) : null;
	}
	
	public String getStringOfValue(int i) {
		return i >= 1 && i <= size ? String.valueOf(i) : null;
	}
	
	public String toString() {
		String value = "";
		
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				String i2 = getCellValue(i, j);
				value += i2 != null ? (String.format("%" + cellWidth + "s", i2) + " ") : String.format("%" + (cellWidth+1) + "s", "* ");
				if(j % n == n-1)	//0-index: e.g. for usual 9x9 puzzle, dividing points are indices 2 and 5, not 3 & 6
					value += "  ";
			}
			value += i % n == n-1 ? "\n\n" : "\n";	//same 0-index issue
		}
		return value;
	}
	
	//iterate over sets of cells of size this.size that must each contain exactly 1, 2, ... size to be successfully solved.
	protected class SudokuIterator implements Iterator<Cell[]> {
		private int cursor = 0;
		
		public boolean hasNext() {
			return (cursor < 3*size);
		}
		
		public Cell[] next() {
			if(cursor < size)
				return getRow(cursor++);	//a row
			else if(cursor < 2*size) {	//cursor must be >= size for the method to test this condition
				return getColumn(cursor++ % size);
			} else {
				return regionMapping.get(cursor++ % size).toArray(new Cell[size]);	//why this is an inner, non-static class
			}
		}
		
		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Sudoku puzzles are immutable once loaded.");
		}
	}
	
	protected Iterable<Cell[]> puzzleIterator() {
		return new Iterable<Cell[]>() {
			public Iterator<Cell[]> iterator() {
				return new SudokuIterator();
			}
		};
	}
	
	public static Sudoku of(Scanner scan) {
		int size = Integer.parseInt(scan.next());
		int[] values = new int[size*size];
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++) {
				try {
					values[i*size + j] = Integer.parseInt(scan.next());
				} catch(Exception e) {
					values[i*size + j] = 0;
				}
			}
		
		return new Sudoku((int) Math.sqrt(size), values);
	}
	
	public static Sudoku random(int n, Random seed, int numberOfGivens, JigsawShape js) {
		int size = n*n;
		int[] values = new int[size*size];
		Arrays.fill(values, 0);
		
		Sudoku randomSudoku = js != null && n == 3 ? new Sudoku(n, values, js) : new Sudoku(n, values);
		
		System.out.println("Sudoku random before smartSolve");
		randomSudoku.smartSolve(0, 0);
		System.out.println("Sudoku random after smartSolve");
		randomSudoku.depopulateTo(numberOfGivens, seed);
		
		/** alternate way to do above that ended up being slower, and w/o depopulating
		
		List<Integer> candidates = new LinkedList<>();
		for(int i = 1; i <= size; i++) candidates.add(i);
		
		//heuristic: set first row/column/region to a random ordering of 1-9 while there are no constraints. This speeds up the rest of the randomizing slightly when solving.
		Collections.shuffle(candidates, seed);
		Cell[] firstRow = randomSudoku.getRow(0);
		for(int i = 0; i < size; i++)
			firstRow[i].setValue(candidates.get(i));
		
		populateLoop: while(true) {
			List<Cell> sCells = new LinkedList<>();
			Map<Cell, List<Integer>> candidateMapping = new HashMap<>();
			
			for(Cell c : randomSudoku.puzzle)
				cIf: if(c.getValue() == 0) {
					List<Integer> cellCandidates = new LinkedList<>(candidates);
					for(Cell c2 : randomSudoku.getAffectedCells(c)) {
						//http://stackoverflow.com/questions/4534146/properly-removing-an-integer-from-a-listinteger
						cellCandidates.remove(new Integer(c2.getValue()));
						if(cellCandidates.size() <= 1)
							break cIf;
					}
					sCells.add(c);
					candidateMapping.put(c, cellCandidates);
				}
			
			Cell randomCell = sCells.get(seed.nextInt(sCells.size()));
			List<Integer> rcCandidates = candidateMapping.get(randomCell);
			randomCell.setValue(rcCandidates.get(seed.nextInt(rcCandidates.size())));
			
			switch(randomSudoku.solutionCount(2)) {
				case 0:
					randomCell.setValue(0);
					break;
				case 1:
					randomSudoku.solve();
					break populateLoop;
				case 2:
					//possibly this value works, do nothing and continue.
					break;
				default:
					throw new IllegalArgumentException("Should not be doing more than 2 solutions");
			}
		}
		*/
		
		return randomSudoku;
	}
	
	public static Sudoku randomFull(int n, JigsawShape js) {
		return random(n, new Random(), n*n*n*n, js);
	}
	
	public static Sudoku random(int n, int numberOfGivens, JigsawShape js) {
		return random(n, new Random(), numberOfGivens, js);
	}
	
	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			
			//http://i.imgur.com/t0MBBPr.png
			//http://i.imgur.com/stCF6vC.png
			
			/*int[][] regionNumbers = {
				{1, 1, 1, 2, 3, 3, 3, 3, 3},
				{1, 1, 1, 2, 2, 2, 3, 3, 3},
				{1, 4, 4, 4, 4, 2, 2, 2, 3},
				{1, 1, 4, 5, 5, 5, 5, 2, 2},
				{4, 4, 4, 4, 5, 6, 6, 6, 6},
				{7, 7, 5, 5, 5, 5, 6, 8, 8},
				{0, 7, 7, 7, 6, 6, 6, 6, 8},
				{0, 0, 0, 7, 7, 7, 8, 8, 8},
				{0, 0, 0, 0, 0, 7, 8, 8, 8}
			};*/
			
			for(JigsawShape js : JigsawShape.values()) {
				for(int i = 0; i < 5; i++) {
					long start = System.currentTimeMillis();
					Sudoku.randomFull(3, js);
					System.out.println(js + "\t" + (System.currentTimeMillis() - start));
				}
			}
			
		} else if(args.length == 1) {
			/*LongSummaryStatistics lss = new LongSummaryStatistics();
			int trials = Integer.parseInt(args[0]);
			for(int i = 0; i < trials; i++) {
				long time1 = System.currentTimeMillis();
				Sudoku s = Sudoku.randomFull(3);
				long time2 = System.currentTimeMillis();
				lss.accept(time2-time1);
			}
			System.out.println(lss);
			*/
		}
	}
}
