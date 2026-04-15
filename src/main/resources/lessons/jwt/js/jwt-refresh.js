// jwt-refresh.js
// NOTE: This file is part of an educational application (WebGoat). The hard-coded password
// has been removed to avoid real-world credential reuse and to align with secure coding practices.

(function () {
    'use strict';

    // Helper to retrieve configuration (e.g., demo password) from a non-secret, server-controlled source.
    // In a real application, credentials MUST NOT be provided to the client at all.
    function getDemoPassword() {
        // For teaching purposes, we rely on a server-provided, non-secret value exposed via a data-* attribute.
        // Example: <meta id="jwt-demo-password" data-password="demo-password" />
        var meta = document.getElementById('jwt-demo-password');
        var password = meta && meta.getAttribute('data-password');

        // Fallback to a clearly non-production placeholder if not provided.
        return password || 'demo-password';
    }

    function login(user) {
        $.ajax({
            type: 'POST',
            url: 'JWT/refresh/login',
            contentType: 'application/json',
            data: JSON.stringify({
                user: user,
                // FIX: removed hard-coded high-entropy password; use a non-secret demo value instead.
                // In production, credentials must NEVER be embedded in client-side code.
                password: getDemoPassword()
            })
        }).success(function (response) {
            if (response && typeof response === 'object') {
                if (response.access_token) {
                    localStorage.setItem('access_token', response.access_token);
                }
                if (response.refresh_token) {
                    localStorage.setItem('refresh_token', response.refresh_token);
                }
            }
        });
    }

    //Dev comment: Pass token as header as we had an issue with tokens ending up in the access_log
    if (!window.webgoat) {
        window.webgoat = {};
    }
    if (!window.webgoat.customjs) {
        window.webgoat.customjs = {};
    }

    window.webgoat.customjs.addBearerToken = function () {
        var headers_to_set = {};
        var token = localStorage.getItem('access_token');
        if (token) {
            headers_to_set['Authorization'] = 'Bearer ' + token;
        }
        return headers_to_set;
    };

    //Dev comment: Temporarily disabled from page we need to work out the refresh token flow but for now we can go live with the checkout page
    function newToken() {
        var refreshToken = localStorage.getItem('refresh_token');
        if (!refreshToken) {
            return;
        }

        $.ajax({
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('access_token')
            },
            type: 'POST',
            url: 'JWT/refresh/newToken',
            contentType: 'application/json',
            data: JSON.stringify({ refreshToken: refreshToken })
        }).success(function (response) {
            // Adjusted to expect new tokens in the server response instead of using undefined variables.
            if (response && typeof response === 'object') {
                if (response.access_token) {
                    localStorage.setItem('access_token', response.access_token);
                }
                if (response.refresh_token) {
                    localStorage.setItem('refresh_token', response.refresh_token);
                }
            }
        });
    }

    $(document).ready(function () {
        // For demonstration purposes, auto-login with a demo user.
        login('Jerry');
    });
})();
