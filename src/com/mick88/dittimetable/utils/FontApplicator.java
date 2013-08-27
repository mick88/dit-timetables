package com.mick88.dittimetable.utils;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.v4.util.LruCache;
import android.view.View;
import android.widget.TextView;

import com.mick88.dittimetable.utils.ViewTraverser.ForeachAction;

/**
 * Applies selected font to the view and all views
 * @author Michal
 *
 */
public class FontApplicator
{
	private static LruCache<String, Typeface> fontCache = new LruCache<String, Typeface>(1);
	private Typeface font;
	
	public FontApplicator(Typeface font)
	{
		this.font = font;
	}
	
	public FontApplicator(AssetManager assets, String assetFontName)
	{
		font = fontCache.get(assetFontName); 
		if (font == null) 
		{
			font = Typeface.createFromAsset(assets, assetFontName);
			fontCache.put(assetFontName, font);
		}
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
