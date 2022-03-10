package net.whydah.admin.email.msgraph;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.models.UserSendMailParameterSet;

import okhttp3.Request;
import com.microsoft.graph.requests.GraphServiceClient;

import net.whydah.admin.email.IMailSender;
import net.whydah.admin.email.MailSender;
import net.whydah.admin.email.msgraph.MailPayload.Email;
import net.whydah.admin.util.HttpConnectionHelper;


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

	@Override
	public void send(String recipients, String subject, String content) {
		try {
			IAuthenticationResult result = ClientCredentialGrant.getAccessTokenByClientCredentialGrant();
			ObjectMapper mapper = new ObjectMapper();
			MailPayload payload = new MailPayload(new Supplier<MailPayload.Message>() {
				@Override
				public MailPayload.Message get() {
					MailPayload.Message msg = new MailPayload.Message(subject, 
							new MailPayload.MessageBody("Text", content), new Supplier<List<MailPayload.Recipient>>() {

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
				log.warn("Connection returned HTTP code: {} with message: {}", response.getResponseCode(), response.getContent());
			}

		} catch (Exception e) {
			String smtpInfo = "Error sending email via Azure SMTP. clientId=" + clientId + ", tenantId=" + tenantId + ", fromAddress=" + fromAddress;
			throw new RuntimeException(smtpInfo, e);
		}
	}
	


}
