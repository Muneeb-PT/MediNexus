package com.medinexus.service.identity;

import java.awt.Desktop;
import java.net.URI;

public class IdentityBrowserService {

    public void openAuthorizationPage(String url) {

        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException(
                    "Authorization URL cannot be empty."
            );
        }

        if (!Desktop.isDesktopSupported()) {
            throw new IllegalStateException(
                    "Browser access is not supported on this system."
            );
        }

        try {
            Desktop.getDesktop().browse(
                    URI.create(url)
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to open the authorization page.",
                    e
            );
        }
    }
}