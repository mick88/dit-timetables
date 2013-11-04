package com.mick88.dittimetable.timetable_activity;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.mick88.dittimetable.R;

public class GroupSelectionDialog extends DialogFragment implements OnMultiChoiceClickListener, OnClickListener
{
	public interface GroupSelectionListener
	{
		public void onGroupsSelected(ArrayList<String> selected, ArrayList<String> unselected);
	}
	
	String [] allGroups=null;
	String [] hiddenGroups=null;
	
	ArrayList<String> selectedItems = new ArrayList<String>();
	ArrayList<String> unselectedItems = new ArrayList<String>();
	GroupSelectionListener listener=null;
	
	private boolean isHidden(String group)
	{
		for(String g : hiddenGroups)
		{
			if (g.equals(group)) return true;
		}
		return false;
	}
	
	public void setGroups(String [] all, String [] hidden)
	{
		this.allGroups = all;
		this.hiddenGroups = hidden;
	}
	
	@Override
	public void onAttach(Activity activity)
	{
		try
		{
			listener = (GroupSelectionListener) activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(String.format(Locale.getDefault(), "%s must implement GroupSelectionListener", activity.toString()));
		}
		super.onAttach(activity);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		boolean [] selected = new boolean[allGroups.length];
		for (int i=0; i < allGroups.length; i++)
		{
			selected[i] = isHidden(allGroups[i]) == false;
		}		
		
		builder.setTitle("Select your group(s):")
			.setMultiChoiceItems(allGroups, selected, this)
			.setPositiveButton("OK", this)
			.setNegativeButton("Cancel", null).setIcon(R.drawable.ic_launcher);
		
		return builder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which, boolean isChecked)
	{
		//TODO: Test this fix
		if (isChecked) 
		{
			selectedItems.add(allGroups[which]);
			unselectedItems.remove(allGroups[which]);
		}
		else 
		{
			unselectedItems.add(allGroups[which]);
			selectedItems.remove(allGroups[which]);
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		listener.onGroupsSelected(selectedItems, unselectedItems);
		
	}
}
