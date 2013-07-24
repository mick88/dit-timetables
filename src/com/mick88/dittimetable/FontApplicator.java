package com.mick88.dittimetable;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import com.mick88.dittimetable.ViewTraverser.ForeachAction;

/**
 * Applies selected font to the view and all views
 * @author Michal
 *
 */
public class FontApplicator
{
	final private Typeface font;
	
	public FontApplicator(Typeface font)
	{
		this.font = font;
	}
	
	public FontApplicator(AssetManager assets, String assetFontName)
	{
		this.font = Typeface.createFromAsset(assets, assetFontName);
	}
	
	/**
	 * Applies font to the view and/or its children
	 * @param root
	 * @return
	 */
	public FontApplicator applyFont(View root)
	{
		if (root == null) return this;
		new ViewTraverser(root).traverse(new ForeachAction<View>()
		{
			
			@Override
			public void onElement(View element)
			{
				if (element instanceof TextView)
				{
					((TextView) element).setTypeface(font);
				}
			}
		});
		
		return this;
	}
}
