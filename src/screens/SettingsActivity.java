package screens;

import java.io.IOException;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.mick88.dittimetable.AppSettings;
import com.mick88.dittimetable.R;
import com.mick88.dittimetable.TimetableApp;
import com.mick88.dittimetable.timetable.Timetable;

public class SettingsActivity extends SherlockActivity
{	
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
		
		appSettings = ((TimetableApp)getApplication()).getSettings();
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		yearSelector = (Spinner) findViewById(R.id.spinner_year_selector);
		semesterSelector = (Spinner) findViewById(R.id.spinner_semester_selector);
		editWeeks = (EditText) findViewById(R.id.edit_weeks);
		editCourse = (EditText) findViewById(R.id.editCourseCode);
		editUsername =  (EditText) findViewById(R.id.editUsername);
		editPassword = (EditText) findViewById(R.id.editPassword);
		weekCheckBox = (CheckBox) findViewById(R.id.checkBoxSetCurrentWeekOnly);
		TextView tvInfo = (TextView) findViewById(R.id.textDatasetInfo);
		
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
					break;
				case SEM_2_ID:
					editWeeks.setText(Timetable.SEMESTER_2);
					break;
				case 2:
					editWeeks.setText(Timetable.ALL_WEEKS);
					break;
				case 3:
					editWeeks.setText(String.valueOf(currentWeek));
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
		
		appSettings.setCourse(editCourse.getText().toString());
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
		
		try
		{
			//String [] settings = Timetable.readSettings(getApplicationContext());
			/*courseCode = settings[Timetable.SETTINGS_ID_COURSE];
			year = Integer.parseInt(settings[Timetable.SETTINGS_ID_YEAR]);
			weeks = settings[Timetable.SETTINGS_ID_WEEKS];
			username = settings[Timetable.SETTING_ID_USERNAME].trim();
			password = settings[Timetable.SETTING_ID_PASSWORD].trim();*/					
		} 
		catch (Exception e)
		{
//			Log.w("timetable", e.getMessage());
//			weeks = Timetable.getSemester()==1?Timetable.SEMESTER_1:Timetable.SEMESTER_2;
		}
		
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
			com.actionbarsherlock.view.MenuItem item)
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
		getSupportMenuInflater().inflate(R.menu.activity_settings, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	boolean validate()
	{
		if (editCourse.getText().toString().matches("DT[0-9]{3}") == false)
		{
			editCourse.requestFocus();
			Toast.makeText(getApplicationContext(), "Please enter correct course code (like DT211)", Toast.LENGTH_LONG).show();
			return false;
		}
		
		if (editWeeks.getText().toString().matches("[0-9-,]+") == false) //any digit, space or ,. One or more times
		{
			editWeeks.requestFocus();
			Toast.makeText(getApplicationContext(), "Please enter correct week range (use dropdown)", Toast.LENGTH_LONG).show();
			return false;
		}
		
		if (TextUtils.isEmpty(editUsername.getText()))
		{
			editUsername.requestFocus();
			Toast.makeText(getApplicationContext(), "Please enter user name", Toast.LENGTH_LONG).show();
			return false;
		}
		
		if (TextUtils.isEmpty(editPassword.getText()))
		{
			editPassword.requestFocus();
			Toast.makeText(getApplicationContext(), "Please enter password", Toast.LENGTH_LONG).show();
			return false;
		}
		
		return true;
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
