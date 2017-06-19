package net.whydah.admin;

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


public class ApplicationModelFacade {

	private static final Logger log = LoggerFactory.getLogger(ApplicationModelFacade.class);
	public List<Application> apps = new ArrayList<Application>();
	UibApplicationsConnection uibApplicationsConnection;
	CredentialStore credentialStore;
	private static final int SESSION_CHECK_INTERVAL = 120; 
	Map<String, String> apptopkenId_appId_Map = new HashMap<>(); //TODO: find a way to validate this map later

	public ApplicationModelFacade(CredentialStore credentialStore, UibApplicationsConnection uibApplicationsConnection){
		this.uibApplicationsConnection = uibApplicationsConnection;
		this.credentialStore = credentialStore;
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		ScheduledFuture<?> sf = scheduler.scheduleAtFixedRate(
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

	public Application getApplication(String applicationID) {
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
			apps = ApplicationMapper.fromJsonList(appsJson);
			log.info("Aplication list is updated");
		} catch (AppException e) {
			e.printStackTrace();
		}


	}

}
