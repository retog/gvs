/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: MillisDateFormatTest.java,v 1.3 2007/05/01 09:57:25 rebach Exp $
*/
package com.hp.hpl.jena.gvs.impl.util.java.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

import com.hp.hpl.jena.gvs.impl.util.java.MillisDateFormat;

/**
 * @author reto
 *
 */
public class MillisDateFormatTest extends TestCase {

	/**
	 * Test method for {@link java.text.DateFormat#format(java.util.Date)}.
	 */
	public void testParseFormatDateConstistency() {
		Date date = new Date();
		for (int i = 0; i < 2000; i++) {
			String formatedDate = MillisDateFormat.instance.format(date);
			Date parsedDate;
			try {
				parsedDate = MillisDateFormat.instance.parse(formatedDate);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
			assertEquals(date, parsedDate);
			date = new Date(date.getTime()+(i < 1000 ? 1 : 1000000));
		}
	}
	
	public void testZDate() throws ParseException {
		Date date = new Date();
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.setTime(date);
		StringBuffer formattedBuffer = new StringBuffer();
		formattedBuffer.append(calendar.get(Calendar.YEAR));
		formattedBuffer.append('-');
		formattedBuffer.append(formatFixedDigitsNumber(calendar.get(Calendar.MONTH)+1, 2));
		formattedBuffer.append('-');
		formattedBuffer.append(formatFixedDigitsNumber(calendar.get(Calendar.DAY_OF_MONTH),2));
		formattedBuffer.append('T');
		formattedBuffer.append(formatFixedDigitsNumber(calendar.get(Calendar.HOUR_OF_DAY),2));
		formattedBuffer.append(':');
		formattedBuffer.append(formatFixedDigitsNumber(calendar.get(Calendar.MINUTE),2));
		formattedBuffer.append(':');
		formattedBuffer.append(formatFixedDigitsNumber(calendar.get(Calendar.SECOND), 2));
		formattedBuffer.append('.');
		formattedBuffer.append(formatFixedDigitsNumber(calendar.get(Calendar.MILLISECOND), 3));
		formattedBuffer.append('Z');
		/*String string = dateFormat.format(date);
		StringBuffer result = new StringBuffer(string);
		result.insert(string.length() - 2, ':');*/
		String formatted = formattedBuffer.toString();
		Date parsed = MillisDateFormat.instance.parse(formatted);
		assertEquals(date, parsed);
	}
	
	/**
	 * @param number
	 * @param digits
	 * @return
	 */
	private StringBuffer formatFixedDigitsNumber(int number, int digits) {
		StringBuffer result = new StringBuffer(digits);
		int minNumberOfRightSize = 1;
		for (int i = 0; i < digits -1 ; i++) {
			minNumberOfRightSize *= 10;
			if (number >= minNumberOfRightSize) {
				continue;
			}
			result.append('0');
		}
		result.append(number);
		return result;
	}

	public void testZoneDate() throws ParseException {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat(
		"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		
		String noColumnYet = dateFormat.format(date);
		StringBuffer result = new StringBuffer(noColumnYet);
		result.insert(noColumnYet.length() - 2, ':');
		Date parsed = MillisDateFormat.instance.parse(result.toString());
		assertEquals(date, parsed);
	}
	
	/*public void testTime() throws ParseException {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 300000; i++) {
			MillisDateFormat.instance.parse("1997-07-16T19:20:30.450+01:00");
			//MillisDateFormat.instance.parse("1997-07-16T19:20:30.450Z");
		}
		long endTime = System.currentTimeMillis();
		System.out.println("It took "+(endTime-startTime)+"ms");
	}*/

}


/*
    (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

