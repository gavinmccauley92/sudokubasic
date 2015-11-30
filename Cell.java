package edu.umb.cs443.sudokubasic;

import java.util.*;

//One number of a Sudoku cell

public class Cell {
	public final int ROW, COLUMN, REGION;
	private String suID, overrideID;
	
	private int value, n, size;
	private boolean isGiven;
	private Set<Integer> possibilities;
	
	public Cell(Sudoku su, int value, int row, int column, int region) {
		this.ROW = row;
		this.COLUMN = column;
		this.REGION = region;
		this.suID = su.ID;
		this.overrideID = su.OVERRIDE_ID;
		
		this.size = su.size();
		this.n = (int) Math.sqrt(size);
		
		possibilities = new HashSet<>();
		this.value = value;
		this.isGiven = value > 0;
		if(!isGiven)
			for(int i = 1; i <= size; i++) possibilities.add(i);
	}
	
	//copy constructor to another Sudoku
	public Cell(Sudoku su, Cell c) {
		this(su, c.value, c.ROW, c.COLUMN, c.REGION);
		this.possibilities = c.possibilities;
	}
	
	public void eliminate(int i) {
		if(possibilities.size() > 1)
			possibilities.remove(i);
	}
	
	public void eliminateAllBut(int i1, int i2, int ... iMore) {
		possibilities = new HashSet<>(Arrays.asList(i1, i2));
		for(int i : iMore)
			possibilities.add(i);
	}
	
	public void resetPossibilities() {
		possibilities.clear();
		for(int i = 1; i <= size; i++) possibilities.add(i);
	}
	
	public boolean checkAndSetValue() {
		if(possibilities.size() == 1) {
			value = possibilities.iterator().next();
			possibilities.clear();
			return true;
		}
		
		return false;
	}
	
	public void setValue(int i) {
		value = i;
		if(isGiven = value > 0)
			possibilities.clear();
		else
			resetPossibilities();
	}
	
	public int getValue() {
		return value;
	}
	
	public String getID() {
		return suID;
	}
	
	public void overrideID(String overrideKey, String newOverride, String newID) {
		if(overrideKey.equals(this.overrideID)) {
			this.overrideID = newOverride;
			this.suID = newID;
		}
	}
	
	public boolean equals(Object o) {
		if(!(o instanceof Cell))
			return false;
		
		Cell c = (Cell) o;
		return suID.equals(c.suID) && ROW == c.ROW && COLUMN == c.COLUMN;
	}
	
	public int hashCode() {
		return ROW*n + COLUMN;
	}
	
	public Set<Integer> getPossibilities() {
		return possibilities;
	}
	
	public String toString() {
		return (value != 0 && !isGiven ? String.valueOf(value) : possibilities.toString());
	}
}
