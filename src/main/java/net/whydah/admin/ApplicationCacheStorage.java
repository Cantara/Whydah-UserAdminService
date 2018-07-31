package net.whydah.admin;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.whydah.admin.applications.uib.UibApplicationsConnection;
import net.whydah.admin.errorhandling.AppException;
import net.whydah.sso.application.mappers.ApplicationMapper;
import net.whydah.sso.application.types.Application;
import net.whydah.sso.commands.appauth.CommandGetApplicationIdFromApplicationTokenId;


public class ApplicationCacheStorage {

	private static final Logger log = LoggerFactory.getLogger(ApplicationCacheStorage.class);
	public static List<Application> apps = new ArrayList<Application>();
	UibApplicationsConnection uibApplicationsConnection;
	CredentialStore credentialStore;
	private static final int SESSION_CHECK_INTERVAL = 120; 
	String stsUrl;
	public static Map<String, String> apptopkenId_appId_Map = new HashMap<>(); //TODO: find a way to validate this map later
	static ScheduledExecutorService scheduler;
	
	public ApplicationCacheStorage(CredentialStore credentialStore, UibApplicationsConnection uibApplicationsConnection, String stsUrl){
		this.uibApplicationsConnection = uibApplicationsConnection;
		this.credentialStore = credentialStore;
		this.stsUrl = stsUrl;
		if(scheduler==null) {
			scheduler = Executors.newScheduledThreadPool(1);
			scheduler.scheduleAtFixedRate(
					new Runnable() {
						public void run() {
							try{
								updateApplicationList();
							}catch(Exception ex){
								ex.printStackTrace();
								log.error("Unexpected error " + ex.getMessage());
							}
						}
					},
					1, SESSION_CHECK_INTERVAL, TimeUnit.SECONDS);
		}
	}
	
	public Application getApplicationByAppTokenId(String applicationTokenId) {
		String appId=null;
		if (apptopkenId_appId_Map.containsKey(applicationTokenId)) {
            appId = apptopkenId_appId_Map.get(applicationTokenId);
        } else {
            appId = new CommandGetApplicationIdFromApplicationTokenId(URI.create(stsUrl), applicationTokenId).execute();     
        }

		if(appId!=null) {
			apptopkenId_appId_Map.put(applicationTokenId, appId);
			return getApplication(appId);
		}
		return null;
		
	}
	
	public String getApplicationIdByAppTokenId(String applicationTokenId) {
		String appId=null;
		if (apptopkenId_appId_Map.containsKey(applicationTokenId)) {
            appId = apptopkenId_appId_Map.get(applicationTokenId);
        } else {
            appId = new CommandGetApplicationIdFromApplicationTokenId(URI.create(stsUrl), applicationTokenId).execute();     
        }

		if(appId!=null) {
			apptopkenId_appId_Map.put(applicationTokenId, appId);
			return appId;
		}
		return null;
		
	}

	public Application getApplication(String applicationID) {
		if(applicationID==null||applicationID.equals("")) {
			return null;
		}
		if(apps.size()==0){
			updateApplicationList();
		}
		for(Application app : apps){
			if(app.getId().equals(applicationID)){
				return app;
			}
		}
		return null;
	}


	public void updateApplicationList() {
		try {
			String appsJson;
			appsJson = uibApplicationsConnection.listAll(credentialStore.getUserAdminServiceTokenId());
			if(appsJson!=null) {
				apps = ApplicationMapper.fromJsonList(appsJson);
				log.info("Aplication list is updated");
			}
		} catch (AppException e) {
			e.printStackTrace();
		}


	}

}
