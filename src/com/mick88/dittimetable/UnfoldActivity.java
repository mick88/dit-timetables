package com.mick88.dittimetable;

import com.mick88.dittimetable.utils.FontApplicator;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class UnfoldActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		setContentView(R.layout.activity_unfold);
		
		LinearLayout container = (LinearLayout) findViewById(R.id.container);
		
		FontApplicator fontApplicator = new FontApplicator(getAssets(), "Roboto-Light.ttf");
		fontApplicator.applyFont(container);
	}

}
