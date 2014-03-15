package org.keycloak.testsuite;

import org.keycloak.OAuth2Constants;
import org.keycloak.social.AuthCallback;
import org.keycloak.social.AuthRequest;
import org.keycloak.social.SocialAccessDeniedException;
import org.keycloak.social.SocialProvider;
import org.keycloak.social.SocialProviderConfig;
import org.keycloak.social.SocialProviderException;
import org.keycloak.social.SocialUser;

import java.util.UUID;

public class DummySocial implements SocialProvider {

    private static final String AUTH_PATH = "http://localhost:8081/dummy-social/auth";

    @Override
    public String getId() {
        return "dummy";
    }

    @Override
    public AuthRequest getAuthUrl(SocialProviderConfig config) throws SocialProviderException {
        String state = UUID.randomUUID().toString();

        return AuthRequest.create(state, AUTH_PATH).setQueryParam(OAuth2Constants.RESPONSE_TYPE, "token")
                .setQueryParam(OAuth2Constants.REDIRECT_URI, config.getCallbackUrl()).setQueryParam(OAuth2Constants.STATE, state).setAttribute(OAuth2Constants.STATE, state).build();
    }

    @Override
    public String getName() {
        return "Dummy Provider";
    }

    @Override
    public SocialUser processCallback(SocialProviderConfig config, AuthCallback callback) throws SocialProviderException {
        String error = callback.getQueryParam(OAuth2Constants.ERROR);
        if (error != null) {
            throw new SocialAccessDeniedException();
        }

        if (!callback.getQueryParam(OAuth2Constants.STATE).equals(callback.getAttribute(OAuth2Constants.STATE))) {
            throw new SocialProviderException("Invalid state");
        }

        String id = callback.getQueryParam("id");
        String username = callback.getQueryParam("username");
        SocialUser user = new SocialUser(id, username);
        user.setName(callback.getQueryParam("firstname"), callback.getQueryParam("lastname"));
        user.setEmail(callback.getQueryParam("email"));
        return user;
    }

}
