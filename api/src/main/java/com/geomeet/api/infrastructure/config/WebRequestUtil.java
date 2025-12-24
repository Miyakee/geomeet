package com.geomeet.api.infrastructure.config;

import org.springframework.web.context.request.WebRequest;

/**
 * Utility class for extracting information from WebRequest.
 * Reduces boilerplate code in exception handlers.
 */
public final class WebRequestUtil {

    private static final String URI_PREFIX = "uri=";

    private WebRequestUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Extract the request path from WebRequest.
     * 
     * @param request the WebRequest object
     * @return the request path
     */
    public static String getPath(WebRequest request) {
        String description = request.getDescription(false);
        return description.replace(URI_PREFIX, "");
    }
}

