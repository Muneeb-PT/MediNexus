package com.medinexus.service.identity;

import javafx.application.Platform;

import java.util.function.Consumer;

public class IdentityOAuthManager {

    private static final int CALLBACK_PORT = 8765;

    private static final String CALLBACK_PATH =
            "/oauth/callback";

    private final OAuthCallbackServer callbackServer;

    private final DigiLockerOAuthService oauthService;

    private final DigiLockerIdentityService identityService;

    private Consumer<DigiLockerIdentityService.IdentityResult>
            successHandler;

    private Consumer<Exception> errorHandler;

    private boolean started;

    public IdentityOAuthManager() {

        this.callbackServer =
                new OAuthCallbackServer(
                        CALLBACK_PORT,
                        CALLBACK_PATH
                );

        this.oauthService =
                new DigiLockerOAuthService();

        this.identityService =
                new DigiLockerIdentityService();

        configureCallback();
    }

    private void configureCallback() {

        callbackServer.setCallbackHandler(
                (code, state) -> {

                    try {

                        DigiLockerIdentityService.IdentityResult
                                result =
                                identityService.processCallback(
                                        code,
                                        state
                                );

                        if (successHandler != null) {

                            Platform.runLater(() ->
                                    successHandler.accept(result)
                            );
                        }

                    } catch (Exception e) {

                        if (errorHandler != null) {

                            Platform.runLater(() ->
                                    errorHandler.accept(e)
                            );
                        }
                    }
                }
        );
    }

    public void start() {

        if (started) {
            return;
        }

        callbackServer.start();

        started = true;
    }

    public void stop() {

        if (!started) {
            return;
        }

        callbackServer.stop();

        started = false;
    }

    public DigiLockerOAuthService.AuthorizationRequest
    createAuthorizationRequest(int patientId) {

        if (!started) {
            start();
        }

        return oauthService.createAuthorizationRequest(
                patientId
        );
    }

    public void setSuccessHandler(
            Consumer<DigiLockerIdentityService.IdentityResult>
                    successHandler) {

        this.successHandler = successHandler;
    }

    public void setErrorHandler(
            Consumer<Exception> errorHandler) {

        this.errorHandler = errorHandler;
    }

    public int getCallbackPort() {
        return CALLBACK_PORT;
    }

    public String getCallbackPath() {
        return CALLBACK_PATH;
    }

    public boolean isStarted() {
        return started;
    }
}