package net.whydah.admin.email.msgraph;

import com.exoreaction.notification.SlackNotificationFacade;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import net.whydah.admin.email.IMailSender;
import net.whydah.admin.util.HttpConnectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.internet.InternetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


//ref
//https://adamtheautomator.com/azure-send-email/#Using_Microsoft_Graph_API_to_Send_Azure_Email
//https://docs.microsoft.com/en-us/graph/api/user-sendmail?view=graph-rest-1.0&tabs=http
	
public class MsGraphMailSender implements IMailSender {

	private static final Logger log = LoggerFactory.getLogger(MsGraphMailSender.class);
	
	private String clientId;
	private String tenantId;
	private String fromAddress;
	
	public MsGraphMailSender(String fromAddress, String clientId, String tenantId, String secret) {
		this.clientId = clientId;
		this.tenantId = tenantId;
		this.fromAddress = fromAddress;
		ClientCredentialGrant.initialize(clientId, tenantId, secret);
	}

	private void notifyMailFailure(String failureType, String failureDetail, String subject, String recipients, Throwable cause) {
		try {
			if (!SlackNotificationFacade.isAvailable()) {
				return;
			}
			Map<String, Object> context = new HashMap<>();
			context.put("operation", describeOperation(subject));
			context.put("subject", subject);
			context.put("recipients", maskRecipients(recipients));
			context.put("fromAddress", fromAddress);
			context.put("tenantId", tenantId);
			context.put("clientId", clientId);
			context.put("failureType", failureType);
			context.put("failureDetail", failureDetail);
			context.put("timestamp", Instant.now().toString());
			String message = "MsGraphMailSender failed to send '" + describeOperation(subject) + "' (" + failureType + ")";
			if (cause != null) {
				SlackNotificationFacade.handleExceptionAsWarning(cause, "MsGraphMailSender", message, context);
			} else {
				SlackNotificationFacade.sendAlarm(message, context);
			}
		} catch (Exception notifyEx) {
			log.warn("Failed to send Slack notification for MS Graph mail failure: {}", notifyEx.getMessage());
		}
	}

	private String describeOperation(String subject) {
		if (subject == null) {
			return "email";
		}
		String lc = subject.toLowerCase();
		if (lc.contains("reset")) {
			return "password reset email";
		}
		if (lc.contains("welcome")) {
			return "welcome email";
		}
		if (lc.contains("verify") || lc.contains("verification")) {
			return "verification email";
		}
		return "email (" + subject + ")";
	}

	private String maskRecipients(String recipients) {
		if (recipients == null || recipients.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (String raw : recipients.split(",")) {
			String addr = raw == null ? "" : raw.trim();
			if (addr.isEmpty()) {
				continue;
			}
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(maskOne(addr));
		}
		return sb.toString();
	}

	private String maskOne(String address) {
		int at = address.indexOf('@');
		if (at <= 0) {
			return "***";
		}
		String local = address.substring(0, at);
		String domain = address.substring(at);
		if (local.length() <= 2) {
			return local.charAt(0) + "*" + domain;
		}
		return local.charAt(0) + "***" + local.charAt(local.length() - 1) + domain;
	}

	@Override
	public void send(String recipients, String subject, String content) {
		log.info("sentinel-d59ac840 sentinel-auto-fix [safe to remove after verification]");
		try {
			IAuthenticationResult result = ClientCredentialGrant.getAccessTokenByClientCredentialGrant();
			ObjectMapper mapper = new ObjectMapper();
			MailPayload payload = new MailPayload(new Supplier<MailPayload.Message>() {
				@Override
				public MailPayload.Message get() {
					String contentLc = content.toLowerCase();
					boolean isHtmlContent = contentLc.contains("<!doctype html>"); // TODO provide stronger content-detection
					String contentType = isHtmlContent ? "html" : "text";
					MailPayload.Message msg = new MailPayload.Message(subject,
							new MailPayload.MessageBody(contentType, content), new Supplier<List<MailPayload.Recipient>>() {

						@Override
						public List<MailPayload.Recipient> get() {
							LinkedList<MailPayload.Recipient> toRecipientsList = new LinkedList<MailPayload.Recipient>();
							InternetAddress[] addresses;
							try {
								addresses = InternetAddress.parse(recipients);
								for(InternetAddress address : addresses) {
									MailPayload.Recipient toRecipients = new MailPayload.Recipient();
									toRecipients.emailAddress = new MailPayload.Email(address.getAddress());
									toRecipientsList.add(toRecipients);
								}
							} catch(Exception ex) {
								ex.printStackTrace();
							}
							return toRecipientsList;
						}
					}.get(), new ArrayList<>());
					return msg;
				}
			}.get(), false);
			
			log.debug("Send payload {} with access token {}, scopes {}", mapper.writeValueAsString(payload), result.accessToken(), result.scopes());
			
			HttpConnectionHelper.Response response = HttpConnectionHelper.post("https://graph.microsoft.com/v1.0/users/" + fromAddress + "/sendMail", result.accessToken(), mapper.writeValueAsBytes(payload));
			if(response.getResponseCode() == 200 || response.getResponseCode() == 202) {
				log.info("Sent OK - result {}", response.getContent());
			} else {
				log.error("MS Graph sendMail failed - HTTP {} message: {} subject: {} recipients: {}",
						response.getResponseCode(), response.getContent(), subject, maskRecipients(recipients));
				notifyMailFailure("HTTP " + response.getResponseCode(), response.getContent(), subject, recipients, null);
			}

		} catch (Exception e) {
			String smtpInfo = "Error sending email via Azure SMTP. clientId=" + clientId + ", tenantId=" + tenantId + ", fromAddress=" + fromAddress;
			log.error("MS Graph sendMail threw exception - subject: {} recipients: {} reason: {}",
					subject, maskRecipients(recipients), e.getMessage(), e);
			notifyMailFailure("exception", e.getMessage(), subject, recipients, e);
			throw new RuntimeException(smtpInfo, e);
		}
	}
	


}
