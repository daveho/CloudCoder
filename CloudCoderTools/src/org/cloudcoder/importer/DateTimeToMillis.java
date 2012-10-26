package org.cloudcoder.importer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class DateTimeToMillis {
	public static final String FORMAT = "dd MM yyyy HH:mm:ss Z";

	public static void main(String[] args) throws ParseException {
		Scanner keyboard = new Scanner(System.in);
		
		System.out.println("Enter date/time in format " + FORMAT);
		String dateTime = keyboard.nextLine();
		
		long time = convert(dateTime);
		System.out.println(time);
	}

	public static long convert(String dateTime) throws ParseException {
		DateFormat formatter = new SimpleDateFormat(FORMAT);
		Date date = (Date) formatter.parse(dateTime);
		long time = date.getTime();
		return time;
	}
}
