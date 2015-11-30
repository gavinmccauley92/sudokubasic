package edu.umb.cs443.sudokubasic;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Chronometer;

public class SudokuOptionDialogFragment extends DialogFragment {
	private Chronometer timer;
	private long timerTime;
	
	public SudokuOptionDialogFragment(Chronometer ch) {
		super();
		this.timer = ch;
	}
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	timer.stop();
    	String[] timeStringSplit = timer.getText().toString().split(":");	//mm:ss
    	timerTime = 1000*(Integer.parseInt(timeStringSplit[0])*60 + Integer.parseInt(timeStringSplit[1]));
    	
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Options")
		.setView(getActivity().getLayoutInflater().inflate(R.layout.sudoku_option_dialog, null))
		.setPositiveButton("Close", new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int id) {
            	   
               }
           })
           ;
       
        return builder.create();
    }
    
    @Override
    public void onDismiss(final DialogInterface dialog) {
    	timer.setBase(SystemClock.elapsedRealtime() - timerTime);	//reset to time on pause
    	timer.start();
    }
}
