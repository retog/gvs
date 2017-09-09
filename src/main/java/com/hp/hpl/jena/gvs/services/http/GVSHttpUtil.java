/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: GVSHttpUtil.java,v 1.1 2007/05/28 16:00:43 rebach Exp $
*/
package com.hp.hpl.jena.gvs.services.http;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.Request;

/**
 * @author reto
 *
 */
class GVSHttpUtil {

	private static final TimeZone utcTZ = new SimpleTimeZone(0, "UTC"); // tries

	// to
	// access
	// time-zone
	// file:
	// TimeZone.getTimeZone("UTC");
	/**
	 * @param request a request
	 * @return the date specified in the request or else the current date
	 * @throws HandlerException 
	 */
	static Date getMoment(Request request) throws HandlerException {
		Date moment = null;
		String[] momentParameterStrings = request.getRequestURI()
				.getParameterValues("moment");
		if (momentParameterStrings != null) {
			if (momentParameterStrings.length != 1) {
				throw new HandlerException(
						"only one get parameter \"moment\" supported");
			}
			String momentString = momentParameterStrings[0];
			String datePattern = "yyyyMMddHHmmssSSS";
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern
						.substring(0, momentString.length()));
				dateFormat.setTimeZone(utcTZ);
				moment = dateFormat.parse(momentString);
			} catch (ParseException e) {
				throw new HandlerException(e);
			}
			;
		}
		if (moment == null) {
			moment = new Date();
		}
		return moment;
	}

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

