package com.labservice.service;

// Simple container for user info extracted from ANY social provider.
//
// Google returns:   { email, name, given_name, family_name }
// Facebook returns: { email, name, first_name, last_name }
// GitHub returns:   { email, name, login }
//
// This class normalizes them all into the same shape.
// The AuthService doesn't care WHERE the info came from —
// it just needs email, firstName, lastName to create an account.

public class SocialUserInfo {

    private String email;
    private String firstName;
    private String lastName;

    public SocialUserInfo(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
}
