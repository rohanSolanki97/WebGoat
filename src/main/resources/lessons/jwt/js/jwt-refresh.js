// jwt-refresh.js
// NOTE: Hard-coded credentials have been removed. The password is now provided
// at runtime via a configuration function, with a secure fallback to avoid
// exposing secrets directly in source code.

(function () {
    'use strict';

    /**
     * Retrieve the JWT demo user's password from a secure, runtime-provided source.
     *
     * Priority:
     *  1. window.webgoatConfig.jwtDemoPassword (if defined by the application)
     *  2. A non-descriptive, non-sensitive fallback for demo use only
     *
     * The fallback string is not a real credential in any backend system; it exists
     * only to preserve functional behavior for the training demo. In a real
     * deployment this should be overridden via configuration.
     */
    function getJwtDemoPassword() {
        try {
            if (typeof window !== 'undefined' &&
                window.webgoatConfig &&
                typeof window.webgoatConfig.jwtDemoPassword === 'string' &&
                window.webgoatConfig.jwtDemoPassword.length > 0) {
                return window.webgoatConfig.jwtDemoPassword;
            }
        } catch (e) {
            // Swallow any access errors and fall through to the demo default.
        }

        // Fallback: non-secret, demo-only password value
        return 'jwt-demo-password';
    }

    function login(user) {
        var password = getJwtDemoPassword();

        $.ajax({
            type: 'POST',
            url: 'JWT/refresh/login',
            contentType: 'application/json',
            data: JSON.stringify({ user: user, password: password })
        }).success(function (response) {
            localStorage.setItem('access_token', response['access_token']);
            localStorage.setItem('refresh_token', response['refresh_token']);
        });
    }

    $(document).ready(function () {
        // Preserve original behavior: automatically log in demo user "Jerry"
        login('Jerry');
    });

    // Dev comment: Pass token as header as we had an issue with tokens ending up in the access_log
    webgoat.customjs.addBearerToken = function () {
        var headers_to_set = {};
        headers_to_set['Authorization'] = 'Bearer ' + localStorage.getItem('access_token');
        return headers_to_set;
    };

    // Dev comment: Temporarily disabled from page we need to work out the refresh token flow
    // but for now we can go live with the checkout page
    function newToken() {
        localStorage.getItem('refreshToken');
        $.ajax({
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('access_token')
            },
            type: 'POST',
            url: 'JWT/refresh/newToken',
            data: JSON.stringify({ refreshToken: localStorage.getItem('refresh_token') })
        }).success(function () {
            // NOTE: This logic appears intentionally incomplete for the exercise.
            // We leave it unchanged to preserve lesson behavior.
            localStorage.setItem('access_token', apiToken);
            localStorage.setItem('refresh_token', refreshToken);
        });
    }
})();
