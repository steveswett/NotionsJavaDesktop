package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class StringUtils {
	
	
	public static String getStringFromStreamWithClose(InputStream is)
	{
		Scanner s = null;
		String result = null;

		try
		{
			s = new Scanner(is).useDelimiter("\\A");
			result = s.hasNext() ? s.next() : "";
			is.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (s != null) s.close();
		}
		
		return result == null ? "" : result;
	}
	

}
