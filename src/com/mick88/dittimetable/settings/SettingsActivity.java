package com.mick88.dittimetable.settings;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mick88.dittimetable.DatabaseHelper;
import com.mick88.dittimetable.R;
import com.mick88.dittimetable.RobotoArrayAdapter;
import com.mick88.dittimetable.TimetableApp;
import com.mick88.dittimetable.downloader.TimetableDownloader;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableStub;
import com.mick88.dittimetable.timetable_activity.TimetableActivity;
import com.mick88.dittimetable.utils.FontApplicator;

public class SettingsActivity extends ActionBarActivity implements OnClickListener
{	

	public static final String EXTRA_ALLOW_CANCEL = "allow_cancel";
	
	FontApplicator fontApplicator;
	Spinner yearSelector, 
		semesterSelector;
	CheckBox weekCheckBox;
	EditText editWeeks,
		editCourse,
		editPassword,
		editUsername;
	AppSettings appSettings;
	boolean allowCancel = true;
	
	final int SEM_1_ID=0,
			SEM_2_ID=1,
			SEM_ALL_ID=2,
			CUSTOM_ID=4;
	int currentWeek = Timetable.getCurrentWeek();
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		this.allowCancel = getIntent().getBooleanExtra(EXTRA_ALLOW_CANCEL, true);
		
		this.fontApplicator = new FontApplicator(getAssets(), TimetableApp.FONT_NAME);
		fontApplicator.applyFont(getWindow().getDecorView());
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
		
		findViewById(R.id.btnClearTimetables).setOnClickListener(this);
		findViewById(R.id.btnDeleteSelectedTimetables).setOnClickListener(this);
		
		String [] years = getResources().getStringArray(R.array.year_values);
		yearSelector.setAdapter(new RobotoArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1, years));
		
		String [] presetWeeks = getResources().getStringArray(R.array.semester_predefines);
		semesterSelector.setAdapter(new RobotoArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1, presetWeeks));
		
		findViewById(R.id.btn_get_password).setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.dit.ie/registration/studentclasstimetables/"));				
				startActivity(intent);
			}
		});
		tvInfo.setText(String.format(Locale.ENGLISH, "Dataset: %s, week %d", TimetableDownloader.getDataset(), currentWeek));
		
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
		appSettings.setUsername(editUsername.getText().toString().trim());
		appSettings.setPassword(editPassword.getText().toString().trim());
		
		appSettings.setCourse(editCourse.getText().toString().toUpperCase(Locale.ENGLISH));
		appSettings.setWeekRange(editWeeks.getText().toString());
		appSettings.setYear((int) (yearSelector.getSelectedItemId()+1));
		appSettings.setOnlyCurrentWeek(weekCheckBox.isChecked());
		
		appSettings.saveSettings(this);
		
		return true;
	}
	
	void loadSettings()
	{
		String courseCode="",
			weeks=Timetable.getCurrentSemester()==1?Timetable.SEMESTER_1:Timetable.SEMESTER_2,
					username="", 
					password="";
		int year=1;
		
		courseCode = appSettings.getCourse();
		weeks = appSettings.getWeekRange();
		year = appSettings.getYear();
		
		username = appSettings.getUsername();
		password = appSettings.getPassword();
		
		if (TextUtils.isEmpty(weeks)) weeks = Timetable.getCurrentSemester()==1?Timetable.SEMESTER_1:Timetable.SEMESTER_2;
		
		if (weeks.equals(Timetable.SEMESTER_1)) semesterSelector.setSelection(SEM_1_ID);
		else if (weeks.equals(Timetable.SEMESTER_2)) semesterSelector.setSelection(SEM_2_ID);
		else if (weeks.equals(Timetable.ALL_WEEKS)) semesterSelector.setSelection(SEM_ALL_ID);
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
		if (allowCancel == false)
		{
			menu.findItem(R.id.settings_cancel).setVisible(false);
		}
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
		
		if (editCourse.getText().toString().toUpperCase(Locale.ENGLISH).matches("DT[0-9]{3}[A-Z]?") == false)
		{
			if (editCourse.getText().toString().matches("[0-9]{3}[A-Z]?") == true)
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
			goBack();
		}
	}
	
	void cancelAndQuit()
	{
		this.setResult(RESULT_CANCELED);
		goBack();
	}
	
	private void goBack()
	{
		Intent intent = new Intent(getApplicationContext(), TimetableActivity.class);
		startActivity(intent);
		finish();
	}

	@Override
	public void onBackPressed()
	{
		saveAndQuit();
	}

	/**
	 * Show dialog allowing to pick and delete timetables
	 */
	void showTimetableDeleteDialog()
	{
		final DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
		final List<TimetableStub> timetables = databaseHelper.getSavedTimetables();
		Collections.sort(timetables);
		
		if (timetables.isEmpty())
		{
			new AlertDialog.Builder(this).setMessage(R.string.there_are_not_saved_timetables)
				.setPositiveButton(android.R.string.ok, null)
				.show();
			return;
		}
		
		CharSequence [] titles = new CharSequence[timetables.size()];
		for (int i=0; i < timetables.size(); i++)
			titles[i] = String.format(Locale.ENGLISH, "%s (%s)", timetables.get(i).describe(), timetables.get(i).describeWeeks());
		final boolean [] checked = new boolean[timetables.size()];
		
		new AlertDialog.Builder(this).setMultiChoiceItems(titles, checked, new OnMultiChoiceClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked)
			{
				checked[which] = isChecked;				
			}
		})
		.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// strip list of unchecked items
				for (int i=checked.length-1; i >= 0; i--)
					if (checked[i] == false) 
						timetables.remove(i);
				
				if (timetables.isEmpty())
				{
					Toast.makeText(getApplicationContext(), R.string.no_items_deleted, Toast.LENGTH_SHORT).show();
					return;
				}
				
				// delete
				int n = databaseHelper.delete(TimetableStub.class, timetables);
				Toast.makeText(getApplicationContext(), getString(R.string._d_items_deleted, n), Toast.LENGTH_SHORT).show();
			}
		})
		.setNegativeButton(android.R.string.cancel, null)
		.setTitle(R.string.delete_timetables_)
		.show();
	}
	
	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.btnClearTimetables:
				new AlertDialog.Builder(this)
				.setMessage(R.string.all_cached_timetables_will_be_deleted_if_you_wish_to_use_them_again_they_will_be_re_downloaded_continue_)
				.setTitle(R.string.clear_cache)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
				{
					
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						new DatabaseHelper(getApplicationContext()).deleteAllTimetables();
						Toast.makeText(getApplicationContext(), "Timetable cache cleared", Toast.LENGTH_SHORT).show();
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
				break;
			case R.id.btnDeleteSelectedTimetables:
				showTimetableDeleteDialog();
				break;
		}		
	}
}
