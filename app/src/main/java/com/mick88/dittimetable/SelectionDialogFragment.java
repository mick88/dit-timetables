package com.mick88.dittimetable;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

public abstract class SelectionDialogFragment extends DialogFragment implements OnMultiChoiceClickListener, OnClickListener
{
	
	public interface SelectionResultListener
	{
		public void onGroupsSelected(Map<String, Boolean> items, SelectionDialogFragment dialogFragment);
		public Map<String, Boolean> getDialogListItems(SelectionDialogFragment selectionDialogFragment);
	}	

	Map<String, Boolean> items=null;
	SelectionResultListener listener = null;
	private String[] itemNames=null;
	
	protected abstract int getDialogTitle();

	@Override
	public void onAttach(Activity activity)
	{
		try
		{
			listener = (SelectionResultListener) activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(String.format(Locale.getDefault(), "%s must implement SelectionResultListener", activity.toString()));
		}
		super.onAttach(activity);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.containsKey("items"))
		{
			Object object = savedInstanceState.getSerializable("items");
			if (object instanceof Map)
				setItems((Map<String, Boolean>) object);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle bundle)
	{
		super.onSaveInstanceState(bundle);
		bundle.putSerializable("items", (Serializable) this.items);
	}
	
	@Override
	public void onDetach()
	{
		listener = null;
		super.onDetach();
	}
	
	protected void setItems(Map<String, Boolean> items)
	{
		this.items = items;
		itemNames = items.keySet().toArray(new String[items.size()]);
		Arrays.sort(itemNames);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{	
		if (listener == null)
			throw new NullPointerException();
		if (this.items == null)
			setItems(listener.getDialogListItems(this));
		boolean [] selected = new boolean[itemNames.length];
		
		for (int i=0; i < itemNames.length; i++)
		{
			selected[i] = items.get(itemNames[i]);
		}		
		
		return new AlertDialog.Builder(getActivity()).setTitle(getDialogTitle())
			.setMultiChoiceItems(itemNames, selected, this)
			.setPositiveButton(android.R.string.ok, this)
			.setNegativeButton(android.R.string.cancel, null)
			.setIcon(R.drawable.ic_launcher)
			.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which, boolean isChecked)
	{
		if (itemNames == null)
			throw new NullPointerException();
		items.put(itemNames[which], isChecked);
	}

	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		if (items == null)
			throw new NullPointerException();
		listener.onGroupsSelected(items, this);		
	}
	
}