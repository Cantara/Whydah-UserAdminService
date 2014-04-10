package net.whydah.identity.data.helper;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: totto
 * Date: Nov 4, 2010
 * Time: 1:46:42 PM
 */
public class ApplicationData implements Serializable{

    private String applicationName;
    private String applicationID;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public Map<String, CompanyRoles> getCompaniesAndRolesMap() {
        return companiesAndRolesMap;
    }

    public void addCompanyWithRoles(String company,CompanyRoles companiesAndRolesMap) {
        this.companiesAndRolesMap.put(company,companiesAndRolesMap);
    }

    private final Map<String, CompanyRoles> companiesAndRolesMap = new HashMap<String, CompanyRoles>();

    public Collection<CompanyRoles> getCompanyRoles() {
        return companiesAndRolesMap.values();
    }

    @Override
    public String toString() {
        return "ApplicationData{" +
                "applicationName='" + applicationName + '\'' +
                ", applicationID='" + applicationID + '\'' +
                ", companiesAndRolesMap=" + companiesAndRolesMap +
                '}';
    }
}
