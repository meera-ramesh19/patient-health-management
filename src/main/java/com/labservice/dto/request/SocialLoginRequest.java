package com.labservice.dto.request;

// Generic DTO for ALL social logins (Google, Facebook, GitHub, etc.)
//
// JSON shape:
//   { "provider": "google", "accessToken": "eyJ...", "portalType": "patient" }
//   { "provider": "facebook", "accessToken": "EAA...", "portalType": "doctor" }
//   { "provider": "github", "accessToken": "gho_...", "portalType": "patient" }
//
// provider    = which social login ("google", "facebook", "github")
// accessToken = the token from the social provider's login flow
// portalType  = "patient" or "doctor" (only needed on first login)

public class SocialLoginRequest {

    private String provider;       // "google", "facebook", "github"
    private String accessToken;    // The token from the provider
    private String portalType;     // "patient" or "doctor" — for first-time registration

    public SocialLoginRequest() {}

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getPortalType() { return portalType; }
    public void setPortalType(String portalType) { this.portalType = portalType; }
}
