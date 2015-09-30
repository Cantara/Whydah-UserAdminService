package net.whydah.admin.user;

/**
 *
 {"username":"helloMe", "firstName":"hello", "lastName":"me", "personRef":"", "email":"hello.me@example.com", "cellPhone":"+47 90221133"}
 * Created by baardl on 30.09.15.
 */
public class MinimalUser {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String cellPhone;
    private String personRef;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCellPhone() {
        return cellPhone;
    }

    public void setCellPhone(String cellPhone) {
        this.cellPhone = cellPhone;
    }

    public String getPersonRef() {
        return personRef;
    }

    public void setPersonRef(String personRef) {
        this.personRef = personRef;
    }
}
