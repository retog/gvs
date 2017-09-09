/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: LoginLinkSender.java,v 1.1 2007/05/12 07:40:38 rebach Exp $
 */
package com.hp.hpl.jena.gvs.services.http.accountmanager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.gvs.services.http.accountmanager.AccountManager.Configuration;

/**
 * @author reto
 * 
 */
public class LoginLinkSender {
	private static final Log log = LogFactory.getLog(LoginLinkSender.class);

	private Session session;


	private Configuration configuration;


	/**
	 * @param configuration
	 */
	public LoginLinkSender(Configuration configuration) {
		this.configuration = configuration;
		if (System.getProperty("mail.transport.protocol") == null) {
			System.setProperty("mail.transport.protocol", "smtp");
		}
		if (System.getProperty("mail.smtp.host") == null) {
			System.setProperty("mail.smtp.host", configuration.getSmtpHost());
		}
		session = Session.getDefaultInstance(System.getProperties(), null);
	}

	public void sendLoginLink(String email, String loginLink) {


		DataSource htmlDataSource = getHtmlDataSource(loginLink);
		DataSource textDataSource = getTextDataSource(loginLink);

		Transport transport;
		try {
			transport = session.getTransport();
			transport.connect();
			log.debug("connected");
		} catch (MessagingException e) {
			log.error("Error Opening transport for sending "+loginLink+" ("+e.toString()+")");
			throw new RuntimeException("Opening Transport", e);
		}
		try {
				InternetAddress address = new InternetAddress(email);

				Message msg = new MimeMessage(session);

				msg.setFrom(new InternetAddress(configuration.getFromAddress()));

				msg.setRecipient(Message.RecipientType.TO, address);
				String title = "GVS Login Link";
				try {
					msg.setSubject(MimeUtility.encodeText(title, "UTF-8", null));
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);

				}
				msg.setSentDate(new java.util.Date());
				// create and fill the first message part

				MimeBodyPart plainTextVersion = new MimeBodyPart();
				plainTextVersion.setDataHandler(new DataHandler(textDataSource));

				MimeBodyPart htmlPart = new MimeBodyPart();
				htmlPart.setDataHandler(new DataHandler(htmlDataSource));
				// create the Multipart
				// and its parts to it
				Multipart bodyAlternatives = new MimeMultipart("alternative");
				bodyAlternatives.addBodyPart(plainTextVersion);
				htmlPart.setHeader("Content-Type", "text/html; charset=UTF-8");
				bodyAlternatives.addBodyPart(htmlPart);



				// mbp1.setHeader("Content-Language", "fr");
				// add the Multipart to the message
				Multipart mainMultipart = new MimeMultipart();
				BodyPart body = new MimeBodyPart();
				body.setContent(bodyAlternatives);
				mainMultipart.addBodyPart(body);

				// mainMultipart.addBodyPart(getSerialializerRDFPart(mailModel));

				msg.setContent(mainMultipart);
				log.debug("mesage ready, sending");
				Transport.send(msg);
				/*
				 * Reusing conncection: (problem:isp limits) Address[] recipients = new
				 * Address[1]; recipients[0] = recipient; transport.sendMessage(msg,
				 * recipients);
				 */
				log.info("message sent to " + address.getAddress() + " ("
						+ Thread.activeCount() + ")");
			} catch (Exception ex) {
				log.error("sending email: ", ex);
			}
		try {
			transport.close();
		} catch (MessagingException e) {
			log.error("closing transport: ", e);
		}
	}

	/**
	 * @param loginLink
	 * @return
	 */
	private static DataSource getTextDataSource(final String loginLink) {
		return new DataSource() {

			public String getContentType() {
				return "text/plain; charset=UTF-8";
			}

			public InputStream getInputStream() throws IOException {
				return new ByteArrayInputStream(
						("With the following link you can log in to GVS: "+loginLink).getBytes("utf-8"));
			}

			public String getName() {
				return "text version";
			}

			public OutputStream getOutputStream() throws IOException {
				return null;
			}
			
		};
	}

	/**
	 * @param loginLink
	 * @return
	 */
	private static DataSource getHtmlDataSource(final String loginLink) {
		return new DataSource() {

			public String getContentType() {
				return "text/html";
			}

			public InputStream getInputStream() throws IOException {
				return new ByteArrayInputStream(
						("With the following link you can log in to GVS: <a href=\""+loginLink+"\">"+loginLink+"</a>").getBytes("utf-8"));

			}

			public String getName() {
				// TODO Auto-generated method stub
				return null;
			}

			public OutputStream getOutputStream() throws IOException {
				return null;
			}

		};
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

