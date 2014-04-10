package net.whydah.identity.data.helper;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: totto
 * Date: Nov 4, 2010
 * Time: 1:48:25 PM
 */
public class CompanyRoles  implements Serializable {

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public Map<String,String> getRoleMap() {
        return roleMap;
    }

    public void setRoleMap(Map<String,String> roleMap) {
        this.roleMap = roleMap;
    }

    private String companyNumber;
    private String companyName;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    private Map<String,String> roleMap;

    @Override
    public String toString() {
        return "CompanyRoles{" +
                "companyNumber='" + companyNumber + '\'' +
                ", companyName='" + companyName + '\'' +
                ", roleMap=" + roleMap +
                '}';
    }
}
