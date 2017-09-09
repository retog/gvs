/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: GVSSetClockPermission.java,v 1.1 2007/05/30 07:13:28 rebach Exp $
*/
package com.hp.hpl.jena.gvs.security;

import java.security.Permission;

import com.hp.hpl.jena.gvs.Source;

/** Represent the permission to impersonate a Source, i.e. to assert and revoke statements on behalf
 * of a Source.
 * 
 * @author reto
 *
 */
public class GVSSetClockPermission extends Permission {

	
	public GVSSetClockPermission() {
		super("Allows setting the clock");
	}
	@Override
	public boolean equals(Object other) {
		if (!other.getClass().equals(GVSSetClockPermission.class)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.security.Permission#getActions()
	 */
	@Override
	public String getActions() {
		return "";
	}

	/* (non-Javadoc)
	 * @see java.security.Permission#hashCode()
	 */
	@Override
	public int hashCode() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see java.security.Permission#implies(java.security.Permission)
	 */
	@Override
	public boolean implies(Permission other) {
		if (!other.getClass().equals(GVSSetClockPermission.class)) {
			return false;
		} 
		return true;
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

