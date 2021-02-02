package org.kodluyoruz.mybank.models;

public class CreateCustomerRequest {

    private String personalIdNo;
    private String fullName;
    private String address;
    private String phoneNumber;
    private String email;

    public String getPersonalIdNo() {
        return personalIdNo;
    }

    public void setPersonalIdNo(String personalIdNo) {
        this.personalIdNo = personalIdNo;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
