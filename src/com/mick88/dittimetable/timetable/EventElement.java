package com.mick88.dittimetable.timetable;

import org.jsoup.nodes.Element;

/**
 * Used for parsing event elements in timetable
 * @author Michal
 *
 */
class EventElement
{
	private final String colorStartText ="color:";
			
	String text="";
	String color="";
	
	public EventElement(Element element)
	{
		String style = element.attr("style");
		int colStart = style.indexOf(colorStartText)+colorStartText.length(),
				colEnd = style.indexOf(';', colStart);

		try
		{
			color = style.substring(colStart, colEnd);			
			text = element.text();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public String getText()
	{
		return text;
	}
	
	public String getColor()
	{
		return color;
	}		
}