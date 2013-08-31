package com.mick88.dittimetable.screens;

import java.io.IOException;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mick88.dittimetable.AppSettings;
import com.mick88.dittimetable.R;
import com.mick88.dittimetable.RobotoArrayAdapter;
import com.mick88.dittimetable.TimetableApp;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.utils.FontApplicator;

public class SettingsActivity extends ActionBarActivity
{	
	/**
	 * TODO: Don't allow back if data is incorrect
	 */
	
	FontApplicator fontApplicator;
	Spinner yearSelector, 
		semesterSelector;
	CheckBox weekCheckBox;
	EditText editWeeks,
		editCourse,
		editPassword,
		editUsername;
	AppSettings appSettings;
	
	final int SEM_1_ID=0,
			SEM_2_ID=1,
			CUSTOM_ID=4;
	int currentWeek = Timetable.getCurrentWeek();
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		this.fontApplicator = new FontApplicator(getAssets(), "Roboto-Light.ttf");
		fontApplicator.applyFont(getWindow().getDecorView());
		appSettings = ((TimetableApp)getApplication()).getSettings();
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		yearSelector = (Spinner) findViewById(R.id.spinner_year_selector);
		semesterSelector = (Spinner) findViewById(R.id.spinner_semester_selector);
		editWeeks = (EditText) findViewById(R.id.edit_weeks);
		editCourse = (EditText) findViewById(R.id.editCourseCode);
		editCourse.setRawInputType(Configuration.KEYBOARD_QWERTY);
		editUsername =  (EditText) findViewById(R.id.editUsername);
		editPassword = (EditText) findViewById(R.id.editPassword);
		weekCheckBox = (CheckBox) findViewById(R.id.checkBoxSetCurrentWeekOnly);
		TextView tvInfo = (TextView) findViewById(R.id.textDatasetInfo);
		
		String [] years = getResources().getStringArray(R.array.year_values);
		yearSelector.setAdapter(new RobotoArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, years));
		
		String [] presetWeeks = getResources().getStringArray(R.array.semester_predefines);
		semesterSelector.setAdapter(new RobotoArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, presetWeeks));
		
		findViewById(R.id.btn_get_password).setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.dit.ie/registration/studentclasstimetables/"));				
				startActivity(intent);
			}
		});
		tvInfo.setText(String.format(Locale.ENGLISH, "Dataset: %s, week %d", Timetable.getDataset(), currentWeek));
		
		loadSettings();
		
		semesterSelector.setOnItemSelectedListener(new OnItemSelectedListener()
		{

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3)
			{
				switch ((int)arg3)
				{
				case SEM_1_ID:
					editWeeks.setText(Timetable.SEMESTER_1);
					editWeeks.setError(null);
					break;
				case SEM_2_ID:
					editWeeks.setText(Timetable.SEMESTER_2);
					editWeeks.setError(null);
					break;
				case 2:
					editWeeks.setText(Timetable.ALL_WEEKS);
					editWeeks.setError(null);
					break;
				case 3:
					editWeeks.setText(String.valueOf(currentWeek));
					editWeeks.setError(null);
					break;
				case 4:
					editWeeks.requestFocus();
					editWeeks.selectAll();
					break;
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
			}
		});
	}
	
	boolean saveSettings()
	{
		String [] settings = new String[] {
				editCourse.getText().toString(), 
				String.valueOf(yearSelector.getSelectedItemId()+1),
				editWeeks.getText().toString(),
				editUsername.getText().toString().trim(),
				editPassword.getText().toString().trim()
				};
		
		appSettings.setUsername(editUsername.getText().toString().trim());
		appSettings.setPassword(editPassword.getText().toString().trim());
		
		appSettings.setCourse(editCourse.getText().toString().toUpperCase());
		appSettings.setWeeks(editWeeks.getText().toString());
		appSettings.setYear((int) (yearSelector.getSelectedItemId()+1));
		appSettings.setOnlyCurrentWeek(weekCheckBox.isChecked());
		
		appSettings.saveSettings();
		try
		{
			Timetable.writeSettings(getApplicationContext(), settings);
			return true;
		} catch (IOException e)
		{
			Toast.makeText(getApplicationContext(), "Settings could not be saved", Toast.LENGTH_LONG).show();
		}
		return false;
	}
	
	void loadSettings()
	{
		String courseCode="",
			weeks=Timetable.getSemester()==1?Timetable.SEMESTER_1:Timetable.SEMESTER_2,
					username="", 
					password="";
		int year=1;
		
		courseCode = appSettings.getCourse();
		weeks = appSettings.getWeeks();
		year = appSettings.getYear();
		
		username = appSettings.getUsername();
		password = appSettings.getPassword();
		
		if (TextUtils.isEmpty(weeks)) weeks = Timetable.getSemester()==1?Timetable.SEMESTER_1:Timetable.SEMESTER_2;
		
		if (weeks.equals(Timetable.SEMESTER_1)) semesterSelector.setSelection(SEM_1_ID);
		else if (weeks.equals(Timetable.SEMESTER_2)) semesterSelector.setSelection(SEM_2_ID);
		else semesterSelector.setSelection(CUSTOM_ID);
		
		editCourse.setText(courseCode);
		editWeeks.setText(weeks);
		yearSelector.setSelection(year-1);
		editUsername.setText(username);
		editPassword.setText(password);
		weekCheckBox.setChecked(appSettings.getOnlyCurrentWeek());
		
	}
	
	@Override
	public boolean onOptionsItemSelected(
			MenuItem item)
	{
		switch(item.getItemId())
		{
		case android.R.id.home:
			onBackPressed();
			break;
			
		case R.id.settings_save:
			saveAndQuit();
			break;
		case R.id.settings_cancel:
			cancelAndQuit();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_settings, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	private boolean validate()
	{
		boolean valid = true;
		
		if (TextUtils.isEmpty(editPassword.getText()))
		{
			editPassword.requestFocus();
			editPassword.setError("Password cannot be empty");
			valid = false;
		}
		
		if (TextUtils.isEmpty(editUsername.getText()))
		{
			editUsername.requestFocus();
			editUsername.setError("Username cannot be empty");
			valid = false;
		}
		
		if (editWeeks.getText().toString().matches("[0-9-,]+") == false) //any digit, space or ,. One or more times
		{
			editWeeks.requestFocus();
			editWeeks.setError("Incorrect week range");
			valid = false;
		}
		
		if (editCourse.getText().toString().toUpperCase().matches("DT[0-9]{3}") == false)
		{
			if (editCourse.getText().toString().matches("[0-9]{3}") == true)
			{
				// autocorrect
				CharSequence text = editCourse.getText();
				editCourse.setText(new StringBuilder("DT").append(text));
			}
			else
			{
				editCourse.requestFocus();
				editCourse.setError("Invalid course code");
				valid = false;
			}
		}
		
		return valid;
	}
	
	void saveAndQuit()
	{
		if (validate())
		{
			saveSettings();
			this.setResult(RESULT_OK);
			finish();
		}
	}
	
	void cancelAndQuit()
	{
		this.setResult(RESULT_CANCELED);
		this.finish();
	}

	@Override
	public void onBackPressed()
	{
		AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle(R.string.app_name)
				.setMessage("Would you like to save changes?")
				.setPositiveButton("Yes", new OnClickListener()
				{
					
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						saveAndQuit();
					}
				})
				.setNegativeButton("No", new OnClickListener()
				{
					
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						cancelAndQuit();
						
					}
				})
				.setCancelable(false)
				.setIcon(R.drawable.ic_launcher)
				.create();
		dialog.show();
	}

}
