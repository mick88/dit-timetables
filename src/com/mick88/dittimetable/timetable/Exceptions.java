package com.mick88.dittimetable.timetable;

public abstract class Exceptions
{
	private Exceptions(){};
	
	public static class TimetableException extends RuntimeException
	{
		public TimetableException(String message)
		{
			super(message);
		}
	}
	
	
	public static class InvalidDataException extends TimetableException
	{
		public InvalidDataException()
		{
			super("Server returned invalid data.");
		}		
	}
	
	public static class SettingsEmptyException extends TimetableException
	{
		public SettingsEmptyException()
		{
			super("Undefined settings");
		}		
	}
	
	public static class NoLocalCopyException extends TimetableException
	{

		public NoLocalCopyException()
		{
			super("Local copy not present");
		}
		
	}
	
	public static class ServerConnectionException extends TimetableException
	{
		public ServerConnectionException()
		{
			super("Server connection failed");
		}		
	}
	
	public static class SessionExpiredException extends TimetableException
	{
		public SessionExpiredException()
		{
			super("Sesstion Expired.");
		}
	}
	
	public static class NoEventsException extends TimetableException
	{
		public NoEventsException()
		{
			super("No events to display.");
		}
	}
	
	public static class ServerLoadingException extends TimetableException
	{
		public ServerLoadingException()
		{
			super("The system is loading commonly used data at the moment.");
		}
	}
	
	public static class IncorrectCredentialsException extends TimetableException
	{
		public IncorrectCredentialsException()
		{
			super("Incorrect login credentials.");
		}
	}
	
	public static class EmptyTimetableException extends TimetableException
	{
		public EmptyTimetableException()
		{
			super("Selected timetable is empty.");
		}
	}
	
	public static class WrongCourseException extends TimetableException
	{
		public WrongCourseException()
		{
			super("Cannot find course with this code.");
		}
	}
}
