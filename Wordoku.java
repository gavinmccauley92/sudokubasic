package edu.umb.cs443.sudokubasic;

import java.util.*;

public class Wordoku extends Sudoku {
	protected TreeMap<Character, Integer> ciMapping;
	protected TreeMap<Integer, Character> icMapping;
	
	private void initCharMaps(Set<Character> allowedChars) {
		if(allowedChars.size() != size)
			throw new IllegalArgumentException("There must be exactly " + size + " unique characters, there are " + allowedChars.size() + ": " + allowedChars);
		
		ciMapping = new TreeMap<>();
		icMapping = new TreeMap<>();
		
		int i = 1;
		for(char c : allowedChars) {
			ciMapping.put(c, i);
			icMapping.put(i, c);
			i++;
		}
		
		this.cellWidth = 1;
	}
	
	public Wordoku(int n, int[] values, int[] regionNumbers, Set<Character> allowedChars) {
		super(n, values, regionNumbers);
		initCharMaps(allowedChars);
	}
	
	public Wordoku(int n, int[] values, JigsawShape jigsawShape, Set<Character> allowedChars) {
		super(n, values, jigsawShape);
		initCharMaps(allowedChars);
	}
	
	public Wordoku(int n, int[] values, Set<Character> allowedChars) {
		this(n, values, generateBoxRegions(n*n), allowedChars);
	}
	
	//copy constructor
	public Wordoku(Wordoku w) {
		super(w);
		this.ciMapping = new TreeMap<>(w.ciMapping);
		this.icMapping = new TreeMap<>(w.icMapping);
		this.cellWidth = 1;
	}
	
	//same Sudoku but with different chars
	public Wordoku(Wordoku w, Set<Character> allowedChars) {
		super(w);
		initCharMaps(allowedChars);
	}
	
	public static Wordoku random(int n, int numberOfGivens, JigsawShape js) {
		return random(n, new Random(), numberOfGivens, js);
	}
	
	public static Wordoku random(int n, Random seed, int numberOfGivens, JigsawShape js) {
		int size = n*n;
		int[] values = new int[size*size];
		Arrays.fill(values, 0);
		
		Set<Character> allowedChars = new HashSet<>();
		while(allowedChars.size() != size)
			allowedChars.add((char) ('A' + seed.nextInt(26)));
		
		Wordoku randomWordoku = js != null && n == 3 ? new Wordoku(n, values, js, allowedChars) : new Wordoku(n, values, allowedChars);
		
		randomWordoku.smartSolve(0, 0);
		randomWordoku.depopulateTo(numberOfGivens, seed);
		
		return randomWordoku;
	}
	
	public String getCellValue(int row, int column) {
		int value = puzzle[row*size + column].getValue();
		return value > 0 ? String.valueOf(icMapping.get(value)) : null;
	}
	
	public Map<Character, Integer> getCIMapping() {
		return ciMapping;
	}
	
	public String getStringOfValue(int i) {
		return i >= 1 && i <= size ? String.valueOf(icMapping.get(i)) : null;
	}
	
	public String symbolString() {
		String s = "";
		for(char c : ciMapping.keySet())
			s += c;
		return s;
	}
	
	public static void main(String[] args) throws Exception {
		/*Scanner scan = new Scanner(new File("sudoku\\test-16-2.txt"));
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
		*/
		Wordoku w = Wordoku.random(3, new Random(), 36, null);
		System.out.println(w);
		if(w.solve())
			System.out.println("Solved:\n" + w);
		System.out.println(w.icMapping);
	}
}
