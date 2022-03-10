package net.whydah.admin.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import net.whydah.admin.email.msgraph.MsGraphMailSender;

public class MSGraphMailSenderTest {

	private static final Logger log = LoggerFactory.getLogger(MSGraphMailSenderTest.class);
	
	@Test
	private void testSending() {
		
		MsGraphMailSender sender = new MsGraphMailSender("sso@1881.no", 
				"cceabdd8-db03-48f7-9c5e-741a7b80a825",
				"d7502a07-6b95-4a34-81f4-67d75021c5f4", 
				"MtF7Q~FwgLVmh-.hAg9MHVDoyxfd6.XPmoDtx"
				);
		sender.send("misterhuydo@gmail.com", "test from Azure", "test test test");
//		
//		MsGraphMailSender sender = new MsGraphMailSender("misterhuydo@gmail.com", 
//				"a1882812-cbbf-4c7f-8e19-caa5f80a26cb", 
//				"8ee9f573-4e80-443a-87bc-74df80157fec", 
//				"8rB7Q~-dd3sB5DPSHcUNeYxd17q8KqP11jf.8"
//				);
//		sender.send("goodemailtokeep@gmail.com", "test from Azure", "test test test");
	
	}
	
}
