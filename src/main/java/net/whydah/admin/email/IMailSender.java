package net.whydah.admin.email;

public interface IMailSender {
	public void send(String recipients, String subject, String body) ;
}
