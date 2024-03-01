// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.

package net.whydah.admin.email.msgraph;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
//import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
//import com.microsoft.graph.models.User;
//import com.microsoft.graph.requests.GraphServiceClient;

/**
 * Graph
 */
public class ClientCredentialGrant {

	private static ConfidentialClientApplication app;
	private static boolean initialized= false;
	private static String clientId, tenantId, clientSecret;


	public static void initialize(String clientId, String tenantId, String clientSecret) {
		ClientCredentialGrant.clientId  = clientId;
		ClientCredentialGrant.tenantId = tenantId;
		ClientCredentialGrant.clientSecret = clientSecret;
		buildConfidentialClientObject();

		//    	if(graphClient==null) {
		//    		final ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
		//    				.clientId(clientId)
		//    				.clientSecret(clientSecret)
		//    				.tenantId(tenantId)
		//    				.build();
		//
		//    		final TokenCredentialAuthProvider tokenCredentialAuthProvider = new TokenCredentialAuthProvider(scopes, clientSecretCredential);
		//
		//    		graphClient =
		//    				GraphServiceClient
		//    				.builder()
		//    				.authenticationProvider(tokenCredentialAuthProvider)
		//    				.buildClient();
		//
		//
		//    		//me = graphClient.me().buildRequest().get();
		//    	}
		//        return graphClient;



	}

	private static void buildConfidentialClientObject() {
		if(!initialized) {
			try {
				// Load properties file and set properties used throughout the sample
				app = ConfidentialClientApplication.builder(
						clientId,
						ClientCredentialFactory.createFromSecret(clientSecret))
						.authority("https://login.microsoftonline.com/" + tenantId + "/")
						.build();
				initialized = true;
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static IAuthenticationResult getAccessTokenByClientCredentialGrant() throws Exception {
		if(!initialized) {
			buildConfidentialClientObject();
		}
		// With client credentials flows the scope is ALWAYS of the shape "resource/.default", as the
		// application permissions need to be set statically (in the portal), and then granted by a tenant administrator
		ClientCredentialParameters clientCredentialParam = ClientCredentialParameters.builder(
				Collections.singleton("https://graph.microsoft.com/.default"))
				.build();

		CompletableFuture<IAuthenticationResult> future = app.acquireToken(clientCredentialParam);
		return future.get();
	}


}