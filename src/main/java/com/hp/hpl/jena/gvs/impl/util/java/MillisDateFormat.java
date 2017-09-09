/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: MillisDateFormat.java,v 1.6 2007/05/07 18:45:22 rebach Exp $
 */
package com.hp.hpl.jena.gvs.impl.util.java;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * @author reto implements http://www.w3.org/TR/NOTE-datetime with the
 *         limitation that it expects exactly a three digits decimal fraction of
 *         seconds. if a time zone designator other than 'Z' is presen it must
 *         contain a column
 */
public class MillisDateFormat extends DateFormat {
	/**
	 * An instance of this class
	 */
	public static final MillisDateFormat instance = new MillisDateFormat();

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	private static final long serialVersionUID = 3258407344076372025L;

	private static final TimeZone utcTZ = new SimpleTimeZone(0, "UTC");;

	/**
	 * @see java.text.DateFormat#format(java.util.Date, java.lang.StringBuffer,
	 *      java.text.FieldPosition)
	 */
	public StringBuffer format(Date date, StringBuffer toAppendTo,
			FieldPosition fieldPosition) {

		String string = dateFormat.format(date);
		StringBuffer result = new StringBuffer(string);
		result.insert(string.length() - 2, ':');
		return result;
	}

	/**
	 * @see java.text.DateFormat#parse(java.lang.String,
	 *      java.text.ParsePosition)
	 */
	public Date parse(String dateString, ParsePosition parsePos) {

		int position = parsePos.getIndex();

		int y1 = dateString.charAt(position++) - '0';
		int y2 = dateString.charAt(position++) - '0';
		int y3 = dateString.charAt(position++) - '0';
		int y4 = dateString.charAt(position++) - '0';
		int year = 1000 * y1 + 100 * y2 + 10 * y3 + y4;
		position++; // skip '-'
		int m1 = dateString.charAt(position++) - '0';
		int m2 = dateString.charAt(position++) - '0';
		int month = 10 * m1 + m2;
		position++; // skip '-'
		int d1 = dateString.charAt(position++) - '0';
		int d2 = dateString.charAt(position++) - '0';
		int day = 10 * d1 + d2;
		position++; // skip 'T'
		int h1 = dateString.charAt(position++) - '0';
		int h2 = dateString.charAt(position++) - '0';
		int hour = 10 * h1 + h2;
		position++; // skip ':'
		int min1 = dateString.charAt(position++) - '0';
		int min2 = dateString.charAt(position++) - '0';
		int minutes = 10 * min1 + min2;
		position++; // skip ':'
		int s1 = dateString.charAt(position++) - '0';
		int s2 = dateString.charAt(position++) - '0';
		int secs = 10 * s1 + s2;
		position++; // skip '.'
		int ms1 = dateString.charAt(position++) - '0';
		int ms2 = dateString.charAt(position++) - '0';
		int ms3 = dateString.charAt(position++) - '0';
		int msecs = 100 * ms1 + 10 * ms2 + ms3;
		Calendar resultCalendar = new GregorianCalendar(year, month - 1, day,
				hour, minutes, secs);
		resultCalendar.setTimeZone(utcTZ);
		long timeInMillis = resultCalendar.getTimeInMillis() + msecs;
		char tzd1 = dateString.charAt(position++);
		if (tzd1 != 'Z') {
			int htz1 = dateString.charAt(position++) - '0';
			int htz2 = dateString.charAt(position++) - '0';
			int hourtz = 10 * htz1 + htz2;
			position++; // skip ':'
			int mintz1 = dateString.charAt(position++) - '0';
			int mintz2 = dateString.charAt(position++) - '0';
			int minutestz = 10 * mintz1 + mintz2;
			int offSetInMillis = (hourtz * 60 + minutestz) * 60000;
			if (tzd1 == '+') {
				timeInMillis -= offSetInMillis;
			} else {
				timeInMillis += offSetInMillis;
			}
		}
		parsePos.setIndex(position);
		return new Date(timeInMillis);

	}
}

/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

