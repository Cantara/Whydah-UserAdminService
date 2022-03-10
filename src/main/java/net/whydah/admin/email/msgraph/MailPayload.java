package net.whydah.admin.email.msgraph;

import java.util.List;

public class MailPayload {

	/*
	 * 
	{
	  "message": {
	    "subject": "Meet for lunch?",
	    "body": {
	      "contentType": "Text",
	      "content": "The new cafeteria is open."
	    },
	    "toRecipients": [
	      {
	        "emailAddress": {
	          "address": "fannyd@contoso.onmicrosoft.com"
	        }
	      }
	    ],
	    "ccRecipients": [
	      {
	        "emailAddress": {
	          "address": "danas@contoso.onmicrosoft.com"
	        }
	      }
	    ]
	  },
	  "saveToSentItems": "false"
	}
	 */
	
	public static class Email {
		public Email() {}
		public Email(String add) {this.address = add;}
		public String address;
	}
	public static class Recipient {
		public Recipient() {}
		public Recipient(Email email) {this.emailAddress = email;}
		public Email emailAddress;
	}
	public static class MessageBody {
		public MessageBody() {
			
		}
		public MessageBody(String contentType, String content) {
			this.content = content;
			this.contentType = contentType;
		}
		public String contentType;
		public String content;
	}
	public static class Message {
		public Message() {}
		public Message(String subject, MessageBody body, List<Recipient> toRecipients, List<Recipient> ccRecipients) {
			this.subject = subject;
			this.body = body;
			this.toRecipients = toRecipients;
			this.ccRecipients = ccRecipients;
		}
		public String subject;
		public MessageBody body;
		public List<Recipient> toRecipients;
		public List<Recipient> ccRecipients;
	}
	
	public Message message;
	public boolean saveToSentItems=false;
	
	public MailPayload() {
	}
	
	public MailPayload(Message message, boolean saveToSentItems) {
		this.message = message;
		this.saveToSentItems = saveToSentItems;
	}
	
	
	
	
}
