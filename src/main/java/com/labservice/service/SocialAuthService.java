package com.labservice.service;

import com.labservice.exception.BadRequestException;
import com.labservice.exception.UnauthorizedException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;
import java.util.Map;

// ─────────────────────────────────────────────────────────────
// WHY THIS CLASS EXISTS
// ─────────────────────────────────────────────────────────────
// Each social provider has a DIFFERENT way to verify tokens.
// This class knows how to talk to each one and extract user info.
//
// Think of it like a translator who speaks multiple languages:
//   - Google speaks "GoogleIdToken"
//   - Facebook speaks "Graph API"
//   - GitHub speaks "GitHub API"
//
// But they all answer the same question:
//   "Who is this user?" → email + name
//
// RestTemplate is Spring's built-in HTTP client.
// It's how our server makes HTTP requests to OTHER servers
// (Facebook's API, GitHub's API, etc.)
// ─────────────────────────────────────────────────────────────

@Service
public class SocialAuthService {

    @Value("${google.client-id}")
    private String googleClientId;

    // RestTemplate = Spring's tool for making HTTP requests to other servers
    // Our server → HTTP GET → Facebook's server → response with user info
    private final RestTemplate restTemplate = new RestTemplate();

    // ─────────────────────────────────────────────
    // ROUTE TO THE RIGHT PROVIDER
    // ─────────────────────────────────────────────
    // Takes the provider name ("google", "facebook", "github")
    // and calls the right verification method.
    public SocialUserInfo verifyAndGetUserInfo(String provider, String accessToken) {
        return switch (provider.toLowerCase()) {
            case "google"   -> verifyGoogle(accessToken);
            case "facebook" -> verifyFacebook(accessToken);
            case "github"   -> verifyGitHub(accessToken);
            default -> throw new BadRequestException("Unsupported social provider: " + provider);
        };
    }

    // ─────────────────────────────────────────────
    // GOOGLE VERIFICATION
    // ─────────────────────────────────────────────
    // Google gives us an ID Token (a signed JWT).
    // We verify it using Google's official library.
    // This is the same code that was in AuthService.googleLogin() before.
    private SocialUserInfo verifyGoogle(String token) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(token);
        } catch (Exception e) {
            throw new UnauthorizedException("Failed to verify Google token");
        }

        if (idToken == null) {
            throw new UnauthorizedException("Invalid Google token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        return new SocialUserInfo(
                payload.getEmail(),
                (String) payload.get("given_name"),
                (String) payload.get("family_name")
        );
    }

    // ─────────────────────────────────────────────
    // FACEBOOK VERIFICATION
    // ─────────────────────────────────────────────
    // Facebook gives us an Access Token (not a JWT — just a string).
    // To verify it, we call Facebook's Graph API:
    //   GET https://graph.facebook.com/me?fields=email,first_name,last_name&access_token=XXX
    //
    // If the token is valid, Facebook returns:
    //   { "email": "john@facebook.com", "first_name": "John", "last_name": "Smith" }
    //
    // If invalid, Facebook returns an error.
    //
    // To set up Facebook login:
    //   1. Go to https://developers.facebook.com
    //   2. Create an app
    //   3. Add "Facebook Login" product
    //   4. Frontend uses Facebook's SDK to get the access token
    @SuppressWarnings("unchecked")
    private SocialUserInfo verifyFacebook(String accessToken) {
        String url = "https://graph.facebook.com/me?fields=email,first_name,last_name&access_token="
                + accessToken;

        try {
            // Our server calls Facebook's API
            // RestTemplate sends: GET https://graph.facebook.com/me?...
            // Facebook responds with JSON → Spring converts to Map
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("email")) {
                throw new UnauthorizedException("Facebook did not return an email");
            }

            return new SocialUserInfo(
                    (String) response.get("email"),
                    (String) response.get("first_name"),
                    (String) response.get("last_name")
            );
        } catch (Exception e) {
            throw new UnauthorizedException("Failed to verify Facebook token: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // GITHUB VERIFICATION
    // ─────────────────────────────────────────────
    // GitHub gives us an Access Token.
    // To verify it, we call GitHub's API:
    //   GET https://api.github.com/user
    //   Header: Authorization: Bearer gho_xxxx
    //
    // GitHub returns:
    //   { "email": "john@github.com", "name": "John Smith", "login": "johnsmith" }
    //
    // Note: GitHub's "name" is the full name (not split into first/last).
    // We split it ourselves.
    //
    // Also: GitHub email can be private. If email is null, we fetch it from:
    //   GET https://api.github.com/user/emails
    //
    // To set up GitHub login:
    //   1. Go to https://github.com/settings/developers
    //   2. Create an OAuth App
    //   3. Frontend uses GitHub's OAuth flow to get the access token
    @SuppressWarnings("unchecked")
    private SocialUserInfo verifyGitHub(String accessToken) {
        try {
            // GitHub requires the token in the Authorization header (not the URL)
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Call GitHub's user API
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.github.com/user",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new UnauthorizedException("GitHub returned no data");
            }

            // Get email — may be null if user has private email
            String email = (String) body.get("email");

            // If email is private, fetch from the emails endpoint
            if (email == null) {
                ResponseEntity<Object[]> emailResponse = restTemplate.exchange(
                        "https://api.github.com/user/emails",
                        HttpMethod.GET,
                        entity,
                        Object[].class
                );

                Object[] emails = emailResponse.getBody();
                if (emails != null && emails.length > 0) {
                    // Find the primary email
                    for (Object emailObj : emails) {
                        Map<String, Object> emailMap = (Map<String, Object>) emailObj;
                        if (Boolean.TRUE.equals(emailMap.get("primary"))) {
                            email = (String) emailMap.get("email");
                            break;
                        }
                    }
                }
            }

            if (email == null) {
                throw new UnauthorizedException("Could not get email from GitHub");
            }

            // Split full name into first + last
            // "John Smith" → firstName="John", lastName="Smith"
            String fullName = (String) body.get("name");
            String firstName = "";
            String lastName = "";
            if (fullName != null && fullName.contains(" ")) {
                firstName = fullName.substring(0, fullName.indexOf(" "));
                lastName = fullName.substring(fullName.indexOf(" ") + 1);
            } else if (fullName != null) {
                firstName = fullName;
            }

            return new SocialUserInfo(email, firstName, lastName);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException("Failed to verify GitHub token: " + e.getMessage());
        }
    }
}
