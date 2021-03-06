package com.mick88.dittimetable.settings;

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
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.mick88.dittimetable.BuildConfig;
import com.mick88.dittimetable.DatabaseHelper;
import com.mick88.dittimetable.R;
import com.mick88.dittimetable.RobotoArrayAdapter;
import com.mick88.dittimetable.TimetableApp;
import com.mick88.dittimetable.downloader.TimetableDownloader;
import com.mick88.dittimetable.notifications.EventNotificationService;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableStub;
import com.mick88.dittimetable.timetable_activity.TimetableActivity;
import com.mick88.dittimetable.utils.FontApplicator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SettingsActivity extends ActionBarActivity implements OnClickListener, OnItemSelectedListener
{
    public static final String EXTRA_ALLOW_CANCEL = "allow_cancel";
	
	Spinner
        yearSelector,
		semesterSelector;
	CheckBox
        weekCheckBox,
        eventNotificationsCheckbox;
	EditText editWeeks,
		editPassword,
		editUsername;
	TextView
            editCourse;
	
	private final PresetWeeks[] presetWeekses = new PresetWeeks[] {
            new PresetWeeks("Semester 1", Timetable.SEMESTER_1),
            new PresetWeeks("Semester 2", Timetable.SEMESTER_2),
            new PresetWeeks("This week", String.valueOf(Timetable.getCurrentWeek())),
            new PresetWeeks("Custom...", null),
    };
	int currentWeek = Timetable.getCurrentWeek();
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
        FontApplicator fontApplicator = new FontApplicator(getAssets(), TimetableApp.FONT_NAME);
		fontApplicator.applyFont(getWindow().getDecorView());

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		yearSelector = (Spinner) findViewById(R.id.spinner_year_selector);
		semesterSelector = (Spinner) findViewById(R.id.spinner_semester_selector);
		editWeeks = (EditText) findViewById(R.id.edit_weeks);
		editCourse = (TextView) findViewById(R.id.editCourseCode);
		editUsername =  (EditText) findViewById(R.id.editUsername);
		editPassword = (EditText) findViewById(R.id.editPassword);
		weekCheckBox = (CheckBox) findViewById(R.id.checkBoxSetCurrentWeekOnly);
		eventNotificationsCheckbox = (CheckBox) findViewById(R.id.checkboxNotifyUpcomingEvents);
		TextView tvInfo = (TextView) findViewById(R.id.textDatasetInfo);

        if (editCourse instanceof AutoCompleteTextView)
        {
            RobotoArrayAdapter<String> courseAdapter = new RobotoArrayAdapter<String>(this, R.layout.dropdown_autocomplete, android.R.id.text1, getCourseCodes());
            ((AutoCompleteTextView)editCourse).setAdapter(courseAdapter);
        }

		String [] years = getResources().getStringArray(R.array.year_values);
		yearSelector.setAdapter(new RobotoArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1, years));
		
		semesterSelector.setAdapter(new RobotoArrayAdapter<>(this, android.R.layout.simple_spinner_item, android.R.id.text1, presetWeekses));

		tvInfo.setText(String.format(Locale.ENGLISH, "Dataset: %s, week %d", TimetableDownloader.getDataset(), currentWeek));
		
		loadSettings();
		
		semesterSelector.setOnItemSelectedListener(this);
        findViewById(R.id.btnClearTimetables).setOnClickListener(this);
        findViewById(R.id.btnDeleteSelectedTimetables).setOnClickListener(this);
        findViewById(R.id.btnGetPassword).setOnClickListener(this);
	}
	
	boolean saveSettings()
	{
        AppSettings appSettings = ((TimetableApp) getApplication()).getSettings();
		appSettings.setUsername(editUsername.getText().toString().trim());
		appSettings.setPassword(editPassword.getText().toString().trim());
		
		appSettings.setCourse(editCourse.getText().toString().toUpperCase(Locale.ENGLISH));
		appSettings.setWeekRange(editWeeks.getText().toString());
		appSettings.setYear((int) (yearSelector.getSelectedItemId() + 1));
		appSettings.setOnlyCurrentWeek(weekCheckBox.isChecked());
        if (eventNotificationsCheckbox.isChecked())
        {
            appSettings.setEventNotifications(true);
            EventNotificationService.scheduleUpdates(getApplicationContext(), appSettings);
        }
        else
        {
            appSettings.setEventNotifications(false);
            EventNotificationService.clearNotification(getApplicationContext());
            EventNotificationService.cancelScheduledUpdates(getApplicationContext());
        }

		return true;
	}
	
	void loadSettings()
	{
        AppSettings appSettings = ((TimetableApp) getApplication()).getSettings();

		String courseCode = appSettings.getCourse();
		String weeks = appSettings.getWeekRange();
		int year = appSettings.getYear();
		
		String username = appSettings.getUsername();
		String password = appSettings.getPassword();
		
		if (TextUtils.isEmpty(weeks))
            weeks = Timetable.getCurrentSemester()==1?Timetable.SEMESTER_1:Timetable.SEMESTER_2;

        for (int i=0; i < presetWeekses.length; i++)
        {
            // select relevant semester or last item ("Custom...")
            if (weeks.equals(presetWeekses[i].weekRange) || i == presetWeekses.length - 1)
            {
                semesterSelector.setSelection(i);
                break;
            }
        }
		
		editCourse.setText(courseCode);
		editWeeks.setText(weeks);
		yearSelector.setSelection(year - 1);
		editUsername.setText(username);
		editPassword.setText(password);
		weekCheckBox.setChecked(appSettings.getOnlyCurrentWeek());
		eventNotificationsCheckbox.setChecked(appSettings.getEventNotifications());

	}
	
	@Override
	public boolean onOptionsItemSelected(
			MenuItem item)
	{
		switch(item.getItemId())
		{
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.settings_save:
                saveAndQuit();
                return true;

            case R.id.settings_cancel:
                cancelAndQuit();
                return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_settings, menu);
		if (getIntent().getBooleanExtra(EXTRA_ALLOW_CANCEL, true) == false)
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

        final String course = editCourse.getText().toString().toUpperCase(Locale.ENGLISH);
		if (course.matches("(DT|B)[0-9]{3,4}[A-Z]?") == false)
		{
			if (course.matches("[0-9]{3,4}[A-Z]?") == true)
			{
				// autocorrect
				editCourse.setText(new StringBuilder("DT").append(course));
			}
			else
			{
				editCourse.requestFocus();
				editCourse.setError("Invalid course code");
				valid = false;

                if (BuildConfig.DEBUG == false)
                {
                    Map<String, String> stringMap = new HashMap<String, String>(1);
                    stringMap.put("course", course);
                    FlurryAgent.onEvent("Invalid course code", stringMap);
                }
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
                for (int i = checked.length - 1; i >= 0; i--)
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

            case R.id.btnGetPassword:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.dit.ie/registration/studentclasstimetables/"));
                startActivity(intent);
                break;
		}		
	}
	
	public static String [] getCourseCodes()
	{
		String [] codes = new String [] {
				"DT001",
				"DT002",
				"DT003",
				"DT003A",
				"DT004",
				"DT005",
				"DT006",
				"DT007",
				"DT008",
				"DT009",
				"DT010",
				"DT011",
				"DT011P",
				"DT012",
				"DT020",
				"DT021",
				"DT022",
				"DT023",
				"DT024",
				"DT025",
				"DT026",
				"DT027",
				"DT028",
				"DT032",
				"DT033",
				"DT036A",
				"DT039",
				"DT040",
				"DT041",
				"DT080",
				"DT080A",
				"DT081",
				"DT086",
				"DT087",
				"DT088",
				"DT089",
				"DT089A",
				"DT090",
				"DT090A",
				"DT090E",
				"DT091",
				"DT092",
				"DT093",
				"DT097A",
				"DT101",
				"DT104",
				"DT105",
				"DT106",
				"DT107",
				"DT110",
				"DT111",
				"DT112",
				"DT113",
				"DT117",
				"DT118",
				"DT118C",
				"DT121",
				"DT121A",
				"DT122",
				"DT123",
				"DT124",
				"DT133",
				"DT134",
				"DT149A",
				"DT155",
				"DT159",
				"DT164",
				"DT168",
				"DT169",
				"DT170",
				"DT175",
				"DT175A",
				"DT201",
				"DT203",
				"DT204",
				"DT205",
				"DT206",
				"DT207",
				"DT208",
				"DT209",
				"DT211",
				"DT212",
				"DT217",
				"DT217A",
				"DT218",
				"DT220",
				"DT221",
				"DT222",
				"DT223",
				"DT224",
				"DT226",
				"DT226A",
				"DT227",
				"DT228",
				"DT228A",
				"DT228B",
				"DT229",
				"DT230",
				"DT230B",
				"DT231A",
				"DT233",
				"DT234",
				"DT235",
				"DT236P",
				"DT237",
				"DT238",
				"DT247",
				"DT248",
				"DT249",
				"DT259",
				"DT260",
				"DT261",
				"DT265",
				"DT276",
				"DT285",
				"DT286",
				"DT299",
				"DT299T",
				"DT303",
				"DT310",
				"DT314A",
				"DT315",
				"DT321",
				"DT324",
				"DT329",
				"DT331",
				"DT332",
				"DT340A",
				"DT341",
				"DT343",
				"DT344",
				"DT346",
				"DT347",
				"DT348",
				"DT349",
				"DT350",
				"DT351",
				"DT352",
				"DT353",
				"DT354",
				"DT355",
				"DT357",
				"DT358",
				"DT360",
				"DT364",
				"DT365",
				"DT366",
				"DT370",
				"DT374",
				"DT375",
				"DT388",
				"DT389",
				"DT390",
				"DT395",
				"DT398",
				"DT398A",
				"DT399",
				"DT401",
				"DT401T",
				"DT406",
				"DT406A",
				"DT406H",
				"DT406T",
				"DT407",
				"DT408A",
				"DT408H",
				"DT408T",
				"DT411",
				"DT411H",
				"DT412",
				"DT413",
				"DT414",
				"DT415",
				"DT416",
				"DT417",
				"DT418",
				"DT418P",
				"DT420",
				"DT421",
				"DT422",
				"DT423",
				"DT424",
				"DT425",
				"DT432",
				"DT432A",
				"DT434",
				"DT435",
				"DT436",
				"DT436T",
				"DT437",
				"DT438",
				"DT441",
				"DT442T",
				"DT443",
				"DT444",
				"DT444T",
				"DT451",
				"DT460E",
				"DT477",
				"DT480",
				"DT481",
				"DT491",
				"DT501",
				"DT502A",
				"DT504",
				"DT505",
				"DT506",
				"DT518",
				"DT519",
				"DT520",
				"DT522A",
				"DT522F",
				"DT527",
				"DT528",
				"DT529",
				"DT530",
				"DT531",
				"DT532",
				"DT533",
				"DT533M",
				"DT533N",
				"DT534",
				"DT539B",
				"DT540",
				"DT540A",
				"DT540B",
				"DT542",
				"DT543",
				"DT544",
				"DT545",
				"DT546",
				"DT547",
				"DT547A",
				"DT548",
				"DT549",
				"DT550",
				"DT552",
				"DT553",
				"DT555",
				"DT556",
				"DT557",
				"DT558",
				"DT559",
				"DT561",
				"DT564",
				"DT565",
				"DT567",
				"DT568",
				"DT569",
				"DT571",
				"DT572",
				"DT572A",
				"DT572B",
				"DT572H",
				"DT574",
				"DT576",
				"DT577",
				"DT582",
				"DT584",
				"DT586",
				"DT587",
				"DT589",
				"DT592",
				"DT596",
				"DT597",
				"DT598",
				"DT624",
				"DT704",
				"DT710",
				"DT711",
				"DT712",
				"DT715",
				"DT761",
				"DT764",
				"DT768",
				"DT789",
		};
		
		String [] result = new String[codes.length + codes.length];
		
		for (int i=0; i < result.length; i++)
		{
			if (i < codes.length)
				result[i] = codes[i];
			else result[i] = codes[i - codes.length].substring(2);
		}
		
		return result;
	}

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View itemView, int position, long id)
    {
        final Object item = adapterView.getItemAtPosition(position);
        if (item instanceof PresetWeeks)
        {
            final String weekRange = ((PresetWeeks) item).weekRange;
            if (weekRange == null)
            {
                editWeeks.requestFocus();
                editWeeks.selectAll();
            }
            else
            {
                editWeeks.setText(weekRange);
                editWeeks.setError(null);
            }
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {
    }
}
