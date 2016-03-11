package utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtils 
{

	private static SimpleDateFormat ymdhmsFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
	private static SimpleDateFormat ymdhmsWithDashesFormatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	
	public static String getDateTimeStringFormattedForSQL(String yyyymmddhhmmss)
	{
		String result = yyyymmddhhmmss.substring(0, 4) + "-" + yyyymmddhhmmss.substring(4, 6) + "-" +
				yyyymmddhhmmss.substring(6, 8) + " " + yyyymmddhhmmss.substring(8, 10) + ":" + 
				yyyymmddhhmmss.substring(10, 12) + ":" + yyyymmddhhmmss.substring(12);
		
		return result;
	}
	
	
	public static String getCurrentTimeAsYmdhms()
	{
		Calendar now = Calendar.getInstance();
		return ymdhmsFormatter.format(now.getTime());
	}
	
	
	public static String getCurrentTimeAsYmdhmsWithDashes()
	{
		Calendar now = Calendar.getInstance();
		return ymdhmsWithDashesFormatter.format(now.getTime());
	}
	
}
