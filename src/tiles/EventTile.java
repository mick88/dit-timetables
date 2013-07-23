package tiles;

import java.util.Locale;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.timetable.TimetableEvent;
import com.mick88.dittimetable.timetable.TimetableEvent.ClassType;

@Deprecated
public class EventTile extends View
{
	enum CurrentState
	{
		Normal,
		Upcoming,
		Current,
	}
	
	TimetableEvent timetableEvent = null;
	View tile;
	Context context;
	CurrentState currentState = CurrentState.Normal;
	
	public EventTile(Context context) {
		super(context);
		init(context);
	}
	
	public EventTile(Context context, TimetableEvent timetableEvent)
	{
		this(context);
		setTimetableEvent(timetableEvent);
	}
	
	public EventTile(Context context, TimetableEvent event, CurrentState currentState)
	{
		this(context, event);
		setCurrentState(currentState);
	}
	
	private void init(Context context)
	{
		tile = inflate(context, R.layout.timetable_event_small, null);
	}
	
	private void setText()
	{
		if (timetableEvent != null)
		{
			((TextView) tile.findViewById(R.id.eventTitle)).setText(timetableEvent.getName());
			((TextView) tile.findViewById(R.id.eventTime)).setText(String.format(Locale.getDefault(), "%s - %s", timetableEvent.getStartTime(), timetableEvent.getEndTime()));
			((TextView) tile.findViewById(R.id.eventLocation)).setText(timetableEvent.getRoomShort());
			
			TextView textGroup = (TextView) tile.findViewById(R.id.eventGroup);
			if (TextUtils.isEmpty(timetableEvent.getGroupStr()))
			{
				textGroup.setVisibility(View.GONE);
			}
			else
			{
				textGroup.setText(timetableEvent.getGroupStr());
				textGroup.setVisibility(View.VISIBLE);
			}
			
			TextView textLecturer = (TextView) tile.findViewById(R.id.eventLecturer);
			if (textLecturer != null)
			{
				textLecturer.setText(timetableEvent.getLecturer());
			}
			
			TextView textEventType = (TextView) tile.findViewById(R.id.eventType);
			ClassType type = timetableEvent.getClassType();
			int color = 0x000000;				
			switch (type)
			{
				case Lecture:
					color = getResources().getColor(R.color.color_lecture);
					break;
				case Laboratory:
					color = getResources().getColor(R.color.color_laboratory);
					break;
				case Tutorial:
					color = getResources().getColor(R.color.color_tutorial);
					break;
				default:
					color = 0xFFFFFF;
					break;
			}
			textEventType.setTextColor(color);
			textEventType.setText(type.toString());
			
			invalidate();
			requestLayout();
		}
	}
	
	public void setTimetableEvent(TimetableEvent timetableEvent)
	{
		this.timetableEvent = timetableEvent;
		setText();
	}
	
	public void setCurrentState(CurrentState currentState)
	{
		this.currentState = currentState;
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		tile.draw(canvas);
		ViewGroup group = (ViewGroup) tile;
		int max = group.getChildCount();
		for (int i=0; i < max; i++)
		{
			group.getChildAt(i).draw(canvas);
		}
	}
	
	@Override
	public void setOnClickListener(OnClickListener l)
	{
		tile.setOnClickListener(l);
		super.setOnClickListener(l);
	}

}
