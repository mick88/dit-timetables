package com.mick88.dittimetable.about;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.RobotoArrayAdapter;
import com.mick88.dittimetable.about.SocialLinkAdapter.SocialLinkItem;

public class SocialLinkAdapter extends RobotoArrayAdapter<SocialLinkItem>
{
	public static interface SocialLinkItem
	{
		void setText(TextView textView);
		void setImage(ImageView imageView);
	}
	
	private static class ViewHolder
	{
		TextView textView;
		ImageView imageView;
	}
	
	public SocialLinkAdapter(Context context, SocialLinkItem[] objects)
	{
		super(context, 0, 0, objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final ViewHolder holder;
		View view = convertView;
		if (view == null)
		{
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.list_item_img_text, parent, false);
			applyRobotoFont(view);
			
			holder = new ViewHolder();
			holder.imageView = (ImageView) view.findViewById(android.R.id.icon);
			holder.textView = (TextView) view.findViewById(android.R.id.text1);
			view.setTag(holder);
		}
		else holder = (ViewHolder) view.getTag();
		
		SocialLinkItem item = getItem(position);
		
		item.setText(holder.textView);
		item.setImage(holder.imageView);
		
		return view;		
	}
	
}
