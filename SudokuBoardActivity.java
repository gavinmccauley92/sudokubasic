package edu.umb.cs443.sudokubasic;

import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SudokuBoardActivity extends ActionBarActivity {
	private Sudoku currentSudoku;
	private Map<Character, Integer> wordokuCIMapping;
	private SudokuOptionDialogFragment currentOptionDialog;
	
	private Map<TextView, Cell> hintCellMapping;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_menu);
	}
	
	public void showOptions(View v) {
		currentOptionDialog = new SudokuOptionDialogFragment((Chronometer) findViewById(R.id.sudoku_timer));
		currentOptionDialog.show(getFragmentManager(), "Options");
	}
	
	public void selectDifficulty(View v) {
		RadioButton rb = (RadioButton) findViewById(((RadioGroup) findViewById(R.id.difficulty_radio_group)).getCheckedRadioButtonId());
		for(int rbid : Arrays.asList(R.id.radio_super_easy, R.id.radio_easy, R.id.radio_medium, R.id.radio_hard)) {
			RadioButton rb2 = (RadioButton) findViewById(rbid);
			rb2.setTypeface(rb.equals(rb2) ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
		}
	}
	
	public void startSudoku(View v) {
		Button b = (Button) findViewById(R.id.start_button);
		b.setEnabled(false);
		b.setText("Loading...");
		ProgressBar pb = (ProgressBar) findViewById(R.id.sudokuGenerateBar);
		pb.setVisibility(View.VISIBLE);
		
		int givens = 81;
		Random r = new Random();
		
		switch(((RadioGroup) findViewById(R.id.difficulty_radio_group)).getCheckedRadioButtonId()) {
			case R.id.radio_super_easy:
				givens = r.nextInt(05) + 51;	//51-55
				break;
			case R.id.radio_easy:
				givens = r.nextInt(10) + 41;	//41-50
				break;
			case R.id.radio_medium:
				givens = r.nextInt(10) + 31;	//31-40
				break;
			case R.id.radio_hard:
				givens = r.nextInt(05) + 26;	//26-30
		}
		
		CheckBox wordoku = (CheckBox) findViewById(R.id.wordoku_check), jigsaw = (CheckBox) findViewById(R.id.jigsaw_check);
		new SudokuGenerationTask().execute(givens, wordoku.isChecked() ? 0 : null, jigsaw.isChecked() ? 0 : null);
	}
	
	public void returnToStart(View v) {
		setContentView(R.layout.start_menu);
		currentOptionDialog.dismiss();
	}
	
	private Deque<Sudoku> savedSudokus = new LinkedList<>();
	private boolean workingInSudokuStateMethod = false;
	
	public void saveSudokuState(View v) {
		savedSudokus.push(currentSudoku instanceof Wordoku ? new Wordoku((Wordoku) currentSudoku) : new Sudoku(currentSudoku));
	}
	
	public void revertSudokuState(View v) {
		if(savedSudokus.peek() == null || (v.getId() == R.id.revert_state && savedSudokus.size() <= 1))
			return;
		
		workingInSudokuStateMethod = true;
		
		Sudoku savedSudoku = v.getId() == R.id.revert_state ? savedSudokus.pop() : savedSudokus.removeFirst();
		
		GridLayout sudokuGrid = (GridLayout) findViewById(R.id.board_grid);
		
		for(Cell c : savedSudoku.getPuzzle()) {
			View cellView = sudokuGrid.getChildAt(c.ROW*currentSudoku.size() + c.COLUMN);
			
			if(cellView instanceof EditText) {
				currentSudoku.getCellAt(c.ROW, c.COLUMN).setValue(c.getValue());
				((EditText) cellView).setText(currentSudoku.getStringOfValue(c.getValue()));
			}
		}
		
		//reset hints
		for(Cell c : currentSudoku.getPuzzle())
			if(c.getValue() == 0)
				c.resetPossibilities();
		for(Cell c : currentSudoku.getPuzzle())
			currentSudoku.updatePossibilities(c);
		
		updateHints();
		
		if(v.getId() == R.id.reset)
			savedSudokus.clear();
		
		currentOptionDialog.dismiss();
		workingInSudokuStateMethod = false;
	}
	
	public void toggleHints(View v) {
		final GridLayout sudokuGrid = (GridLayout) findViewById(R.id.board_grid), hintGrid = (GridLayout) findViewById(R.id.hint_grid);
		ToggleButton tb = (ToggleButton) v;
		
		if(tb.isChecked())
			hintGrid.bringToFront();
		else
			sudokuGrid.bringToFront();
	}
	
	private class SudokuGenerationTask extends AsyncTask<Integer, Void, Sudoku> {
		protected Sudoku doInBackground(Integer ... givens) {
			Log.i("doInBackground", "Starting");
			if(givens[0] > 17 && givens[0] < 81) {
				JigsawShape js = givens[2] != null ? JigsawShape.random() : null;
				return givens[1] != null ? Wordoku.random(3, givens[0], js) : Sudoku.random(3, givens[0], js);
			} else
				return null;
		}
		
		protected void onPostExecute(Sudoku su) {
			Log.i("postExecute", "Starting");
			if(su == null)
				throw new IllegalArgumentException("Inappropriate number of givens");
			
			CheckBox wordokuBox = (CheckBox) findViewById(R.id.wordoku_check);
			CheckBox jigsawBox = (CheckBox) findViewById(R.id.jigsaw_check);
			
			setContentView(R.layout.activity_sudoku_board);
			
			currentSudoku = su;
			savedSudokus.clear();
			saveSudokuState(null);	//set up first item w/ empty
			
			wordokuCIMapping = wordokuBox.isChecked() ? ((Wordoku) currentSudoku).getCIMapping() : null;
			hintCellMapping = new HashMap<>();
			
			//set up hints
			for(Cell c : currentSudoku.getPuzzle())
				if(c.getValue() == 0)
					c.resetPossibilities();
			for(Cell c : currentSudoku.getPuzzle())
				currentSudoku.updatePossibilities(c);
			
			final GridLayout sudokuGrid = (GridLayout) findViewById(R.id.board_grid);
			sudokuGrid.removeAllViews();
			final GridLayout hintGrid = (GridLayout) findViewById(R.id.hint_grid);
			hintGrid.removeAllViews();
			
			//for some reason I couldn't center either grid in xml, doing it programmatically.
			Display display = getWindowManager().getDefaultDisplay();
			Point screenSize = new Point(); display.getSize(screenSize);
			int screenWidth = screenSize.x, screenHeight = screenSize.y;
			
			final int suSize = su.size(), boxSize = (int) Math.sqrt(suSize), CELL_SIZE = (int) (Math.min(screenWidth*0.95, screenHeight*0.85)/(suSize+1));
			
			//finish centering grids.
			int horizontalPadding = (screenWidth - suSize*CELL_SIZE) / 2;
			sudokuGrid.setPaddingRelative(horizontalPadding, 2, 0, 0);
			hintGrid.setPaddingRelative(horizontalPadding, 2, 0, 0);
			
			sudokuGrid.setRowCount(suSize);
			sudokuGrid.setColumnCount(suSize);
			hintGrid.setRowCount(suSize);
			hintGrid.setColumnCount(suSize);
			
			//for Jigsaw, if applicable
			JigsawShape js = currentSudoku.getJigsawShape();
			
			List<Integer> jigsawBackgroundIDs = Arrays.asList(R.drawable.jigsaw_cell_border_0, 
					R.drawable.jigsaw_cell_border_1, R.drawable.jigsaw_cell_border_2, R.drawable.jigsaw_cell_border_3);
			Collections.shuffle(jigsawBackgroundIDs);
			
			for(final Cell c : currentSudoku.getPuzzle()) {
				//Log.i("postExecute", "At cell " + c.ROW + "," + c.COLUMN);
				View v;
				
				if(c.getValue() > 0) {
					TextView tv = new TextView(getApplicationContext());
					tv.setText(String.valueOf(currentSudoku.getCellValue(c.ROW, c.COLUMN)));
					tv.setGravity(Gravity.CENTER);
					tv.setTextColor(getResources().getColor(R.color.given_cell_text));
					tv.setWidth(CELL_SIZE);
					tv.setHeight(CELL_SIZE);
					tv.setTextSize(CELL_SIZE*0.7f);
					
					v = tv;
				} else {
					final EditText et = new EditText(getApplicationContext());
					et.setGravity(Gravity.CENTER);
					et.setTextColor(getResources().getColor(R.color.not_given_cell_text));
					et.setRawInputType(InputType.TYPE_CLASS_NUMBER);
					et.setWidth(CELL_SIZE);
					et.setHeight(CELL_SIZE);
					et.setTextSize(CELL_SIZE*0.7f);
					
					//length and character limitations
					
					InputFilter[] etFilters = new InputFilter[2];
					etFilters[0] = new InputFilter.LengthFilter(1 + (suSize / 10));
					etFilters[1] = DigitsKeyListener.getInstance(currentSudoku.symbolString());
					et.setFilters(etFilters);
					
					//use text change as a time to check if the puzzle is solved.
					
					et.addTextChangedListener(new TextWatcher() {
						/* check if the puzzle is fully solved.
						   Only need to check after a text change and when the text change leads to a filled in cell. */
				        public void afterTextChanged(Editable s) {
				        	if(workingInSudokuStateMethod)
				        		return;
				        	
				        	int value = s.length() > 0 ? (wordokuCIMapping != null ? wordokuCIMapping.get(s.toString().charAt(0)) : Integer.parseInt(s.toString())) : 0;
				        	c.setValue(value);
				        	
				        	// recalculate hints
							for(Cell c2 : currentSudoku.getPuzzle())
								if(c2.getValue() == 0)
									c2.resetPossibilities();
							for(Cell c2 : currentSudoku.getPuzzle())
								currentSudoku.updatePossibilities(c2);
							
				        	updateHints();
				        	
				        	if(value > 0 && currentSudoku.isSolved()) {
					        	//some success method
					        	Chronometer ch = (Chronometer) findViewById(R.id.sudoku_timer);
					        	ch.stop();
					        	
					        	AlertDialog.Builder builder = new AlertDialog.Builder(SudokuBoardActivity.this);
					        	builder
					        		.setTitle("Success!")
					        		.setMessage("Time: " + ch.getText().toString())
					        		.setPositiveButton("Try Another One", new DialogInterface.OnClickListener() {
						                @Override
						                public void onClick(DialogInterface dialog, int id) {
						             	   returnToStart(null);
						                }
						            })
						            .setNegativeButton("Reinspect Puzzle", new DialogInterface.OnClickListener() {
						                @Override
						                public void onClick(DialogInterface dialog, int id) {
						             	   
						                }
						            });
					            
					        	builder.create().show();
				        	}
				        }
				        
				        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
				        public void onTextChanged(CharSequence s, int start, int before, int count) {}
					});
					
					//create an overlay for this cell: a gridlayout of potential candidates (hints)
					
					GridLayout cellHintGrid = new GridLayout(getApplicationContext());
					cellHintGrid.setColumnCount(boxSize);
					cellHintGrid.setRowCount(boxSize);
					
					TextView hinttv = new TextView(getApplicationContext());
					
					Set<Integer> cellHints = c.getPossibilities();
					String hinttext = "";
					
					for(int i = 1; i <= suSize; i++) {
						hinttext += (cellHints.contains(i) ? currentSudoku.getStringOfValue(i) : " ") + (i % boxSize == 0 ? "\n" : "");
					}
					
					hinttv.setText(hinttext);
					
					hinttv.setWidth(CELL_SIZE);
					hinttv.setHeight(CELL_SIZE);
					hinttv.setGravity(Gravity.CENTER);
					hinttv.setTextColor(getResources().getColor(R.color.hint_text));
					hinttv.setTextSize(CELL_SIZE*0.33f);
					hinttv.setTypeface(Typeface.MONOSPACE);
					hinttv.setAlpha(0.9f);
					
					hintGrid.addView(hinttv, new GridLayout.LayoutParams(GridLayout.spec(c.ROW), GridLayout.spec(c.COLUMN)));
					
					hintCellMapping.put(hinttv, c);
					
					v = et;
				}
				
				
				if(!jigsawBox.isChecked()) {
					//determine special borders on certain sides of each cell to separate the boxes better visually
					int borderResource;
					
					if(c.ROW % boxSize == 0 && c.ROW != 0) {
						if(c.COLUMN % boxSize == boxSize - 1 && c.COLUMN != suSize - 1)
							borderResource = R.drawable.cell_border_upper_right;
						else if(c.COLUMN % boxSize == 0 && c.COLUMN != 0)
							borderResource = R.drawable.cell_border_upper_left;
						else
							borderResource = R.drawable.cell_border_upper;
					} else if(c.ROW % boxSize == boxSize - 1 && c.ROW != suSize - 1) {
						if(c.COLUMN % boxSize == boxSize - 1 && c.COLUMN != suSize - 1)
							borderResource = R.drawable.cell_border_lower_right;
						else if(c.COLUMN % boxSize == 0 && c.COLUMN != 0)
							borderResource = R.drawable.cell_border_lower_left;
						else
							borderResource = R.drawable.cell_border_lower;
					} else if(c.COLUMN % boxSize == 0 && c.COLUMN != 0)
						borderResource = R.drawable.cell_border_left;
					else if(c.COLUMN % boxSize == boxSize - 1 && c.COLUMN != suSize - 1)
						borderResource = R.drawable.cell_border_right;
					else
						borderResource = R.drawable.cell_border_center;
					
					v.setBackgroundResource(borderResource);
				} else {
					v.setBackgroundResource(jigsawBackgroundIDs.get(js.COLORS[c.REGION]));
				}
				
				sudokuGrid.addView(v);
			}
			
			Chronometer ch = (Chronometer) findViewById(R.id.sudoku_timer);
			ch.setBase(SystemClock.elapsedRealtime());	//reset
			ch.start();
			
			sudokuGrid.bringToFront();
		}
	}
	
	private void updateHints() {
		int suSize = currentSudoku.size(), boxSize = (int) Math.sqrt(suSize);
		
		for(Map.Entry<TextView, Cell> hintEntry : hintCellMapping.entrySet()) {
			Cell c = hintEntry.getValue(); TextView hinttv = hintEntry.getKey();
			Set<Integer> cellHints = c.getPossibilities();
			String hinttext = "";
			
			for(int i = 1; i <= suSize; i++) {
				hinttext += (cellHints.contains(i) ? currentSudoku.getStringOfValue(i) : " ") + (i % boxSize == 0 ? "\n" : "");
			}
			
			hinttv.setText(hinttext);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sudoku_board, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
