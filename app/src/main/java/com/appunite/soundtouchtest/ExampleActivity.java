/////////////////////////////////////////////////////////////////////////////
///
/// Example Android Application/Activity that allows processing WAV 
/// audio files with SoundTouch library
///
/// Copyright (c) Olli Parviainen
///
////////////////////////////////////////////////////////////////////////////////
//
// $Id: SoundTouch.java 210 2015-05-14 20:03:56Z oparviai $
//
////////////////////////////////////////////////////////////////////////////////


package com.appunite.soundtouchtest;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.appunite.soundtouchtest.soundtouch.BPMDetect;
import com.appunite.soundtouchtest.soundtouch.SoundTouch;

import java.io.File;
import java.io.IOException;

public class ExampleActivity extends Activity implements OnClickListener 
{
	TextView textViewConsole = null;
	EditText editSourceFile = null;
	EditText editOutputFile = null;
	EditText editTempo = null;
	EditText editPitch = null;
	CheckBox checkBoxPlay = null;
	
	StringBuilder consoleText = new StringBuilder();

	MediaPlayer mediaPlayer = new MediaPlayer();

	
	/// Called when the activity is created
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_example);
		
		textViewConsole = (TextView)findViewById(R.id.textViewResult);
		editSourceFile = (EditText)findViewById(R.id.editTextSrcFileName);
		editOutputFile = (EditText)findViewById(R.id.editTextOutFileName);

		editTempo = (EditText)findViewById(R.id.editTextTempo);
		editPitch = (EditText)findViewById(R.id.editTextPitch);
		
		Button buttonFileSrc = (Button)findViewById(R.id.buttonSelectSrcFile);
		Button buttonFileOutput = (Button)findViewById(R.id.buttonSelectOutFile);
		Button buttonProcess = (Button)findViewById(R.id.buttonProcess);
		buttonFileSrc.setOnClickListener(this);
		buttonFileOutput.setOnClickListener(this);
		buttonProcess.setOnClickListener(this);

		checkBoxPlay = (CheckBox)findViewById(R.id.checkBoxPlay);

		// Check soundtouch library presence & version
		checkLibVersion();

		final String inputPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/test.wav";
		final String outputPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/testOutput.wav";

		editSourceFile.setText(inputPath);
		editOutputFile.setText(outputPath);

		ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

		//final float bpm = getBpm(outputPath);
		new BpmTask().execute(outputPath);
	}

	public float getBpm(final String inputFilePath) {
		AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
		String sampleRate = am.getParameters(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
		if (sampleRate == null || sampleRate.equals("")) sampleRate = "44100";

		final BPMDetect bpmDetect = new BPMDetect(2, Integer.parseInt(sampleRate));
		return bpmDetect.getBPM(inputFilePath);
	}
	
		
	/// Function to append status text onto "console box" on the Activity
	public void appendToConsole(final String text)
	{
		// run on UI thread to avoid conflicts
		runOnUiThread(new Runnable() 
		{
		    public void run() 
		    {
				consoleText.append(text);
				consoleText.append("\n");
				textViewConsole.setText(consoleText);
		    }
		});
	}
	

	
	/// print SoundTouch native library version onto console
	protected void checkLibVersion()
	{
		String ver = SoundTouch.getVersionString();
		appendToConsole("SoundTouch native library version = " + ver);
	}



	/// Button click handler
	@Override
	public void onClick(View arg0) 
	{
		switch (arg0.getId())
		{
			case R.id.buttonSelectSrcFile:
			case R.id.buttonSelectOutFile:
				// one of the file select buttons clicked ... we've not just implemented them ;-)
				Toast.makeText(this, "File selector not implemented, sorry! Enter the file path manually ;-)", Toast.LENGTH_LONG).show();
				break;
				
			case R.id.buttonProcess:
				// button "process" pushed
				process();
				break;						
		}
		
	}
	
	
	/// Play audio file
	protected void playWavFile(String fileName)
	{

		try {
			mediaPlayer.reset();
			mediaPlayer.setDataSource(this, Uri.parse(fileName));
			mediaPlayer.prepare();
			mediaPlayer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	protected class BpmTask extends AsyncTask<String, Integer, Float>
	{

		@Override
		protected Float doInBackground(final String... params) {
			return getBpm(params[0]);
		}

		@Override
		protected void onPostExecute(final Float bpm) {
			super.onPostExecute(bpm);
			Toast.makeText(ExampleActivity.this, "Bpm is: " + bpm, Toast.LENGTH_LONG).show();
		}
	}
	
				

	/// Helper class that will execute the SoundTouch processing. As the processing may take
	/// some time, run it in background thread to avoid hanging of the UI.
	protected class ProcessTask extends AsyncTask<ProcessTask.Parameters, Integer, Long>
	{
		/// Helper class to store the SoundTouch file processing parameters
		public final class Parameters
		{
			String inFileName;
			String outFileName;
			float tempo;
			float pitch;
		}

		
		
		/// Function that does the SoundTouch processing
		public final long doSoundTouchProcessing(Parameters params) 
		{
			
			SoundTouch st = new SoundTouch();
			st.setTempo(params.tempo);
			st.setPitchSemiTones(params.pitch);
			Log.i("SoundTouch", "process file " + params.inFileName);
			long startTime = System.currentTimeMillis();
			int res = st.processFile(params.inFileName, params.outFileName);
			long endTime = System.currentTimeMillis();
			float duration = (endTime - startTime) * 0.001f;
			
			Log.i("SoundTouch", "process file done, duration = " + duration);
			appendToConsole("Processing done, duration " + duration + " sec.");
			if (res != 0)
			{
				String err = SoundTouch.getErrorString();
				appendToConsole("Failure: " + err);
				return -1L;
			}
			
			// Play file if so is desirable
			if (checkBoxPlay.isChecked())
			{
				playWavFile(params.outFileName);
			}
			return 0L;
		}


		
		/// Overloaded function that get called by the system to perform the background processing
		@Override	
		protected Long doInBackground(Parameters... aparams) 
		{
			return doSoundTouchProcessing(aparams[0]);
		}
		
	}


	/// process a file with SoundTouch. Do the processing using a background processing
	/// task to avoid hanging of the UI
	protected void process()
	{
		try 
		{
			ProcessTask task = new ProcessTask();
			ProcessTask.Parameters params = task.new Parameters();
			// parse processing parameters
			params.inFileName = editSourceFile.getText().toString();
			params.outFileName = editOutputFile.getText().toString();
			params.tempo = 0.01f * Float.parseFloat(editTempo.getText().toString());
			params.pitch = Float.parseFloat(editPitch.getText().toString());

			// update UI about status
			appendToConsole("Process audio file :" + params.inFileName +" => " + params.outFileName);
			appendToConsole("Tempo = " + params.tempo);
			appendToConsole("Pitch adjust = " + params.pitch);
			
			Toast.makeText(this, "Starting to process file " + params.inFileName + "...", Toast.LENGTH_SHORT).show();

			// start SoundTouch processing in a background thread
			task.execute(params);
//			task.doSoundTouchProcessing(params);	// this would run processing in main thread
			
		}
		catch (Exception exp)
		{
			exp.printStackTrace();
		}
	
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mediaPlayer.release();
	}
}