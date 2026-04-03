package com.labservice.dto.request;

// Shape of Google login JSON:
//   { "googleToken": "eyJhbGciOi...", "portalType": "patient" }
//
// googleToken = the token from Google's "Sign in with Google" button
//               The frontend gets this from Google and sends it to us
// portalType  = "patient" or "doctor" — only used on FIRST login
//               (when we auto-create their account)

public class GoogleLoginRequest {

    private String googleToken;
    private String portalType;    // "patient" or "doctor" — for first-time registration

    public GoogleLoginRequest() {}

    public String getGoogleToken() { return googleToken; }
    public void setGoogleToken(String googleToken) { this.googleToken = googleToken; }

    public String getPortalType() { return portalType; }
    public void setPortalType(String portalType) { this.portalType = portalType; }
}
