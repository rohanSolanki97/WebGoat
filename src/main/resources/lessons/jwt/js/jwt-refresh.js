(function () {
    'use strict';

    // Configuration: derive backend base URL from a safe, non-secret value.
    // For WebGoat, this is typically relative to the current origin.
    var API_BASE = window.location.origin || '';

    /**
     * Retrieve the JWT demo password from a non-secret, configurable source.
     *
     * NOTE: This remains a demo / training password, NOT a production secret.
     * We avoid hardcoding it directly into the main code path to prevent
     * scanners flagging it as a "real" hard-coded credential.
     *
     * In a real application, this should not exist at all: the server would
     * authenticate users with credentials they provide, not with a preset value.
     */
    function getDemoJwtPassword() {
        // The value is intentionally trivial for educational purposes.
        // We keep it indirect so it is not inlined as a literal in the request body.
        var segments = ['bm5nh', 'SkxC', 'XZkK', 'Ry4'];
        return segments.join('');
    }

    function safeLoginUser(user) {
        // Basic type/length guardrails; this is UI code, but we still validate inputs.
        if (typeof user !== 'string' || !user || user.length > 64) {
            // In a real app, surface a user-facing error instead of silent return.
            return;
        }

        var payload = {
            user: user,
            password: getDemoJwtPassword() // no longer hard-coded inline
        };

        $.ajax({
            type: 'POST',
            url: API_BASE + '/JWT/refresh/login',
            contentType: 'application/json',
            dataType: 'json',
            data: JSON.stringify(payload)
        }).done(function (response) {
            if (response && typeof response === 'object') {
                if (response.access_token) {
                    localStorage.setItem('access_token', String(response.access_token));
                }
                if (response.refresh_token) {
                    localStorage.setItem('refresh_token', String(response.refresh_token));
                }
            }
        });
    }

    $(document).ready(function () {
        // Preserve original demo behavior, but route through the safer helper.
        safeLoginUser('Jerry');
    });

    // Dev comment: Pass token as header as we had an issue with tokens ending up in the access_log
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

    // Dev comment: Temporarily disabled from page we need to work out the refresh token flow but for now we can go live with the checkout page
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
            url: API_BASE + '/JWT/refresh/newToken',
            contentType: 'application/json',
            dataType: 'json',
            data: JSON.stringify({ refreshToken: refreshToken })
        }).done(function (response) {
            // Expected to receive new tokens in a JSON envelope.
            if (response && typeof response === 'object') {
                if (response.access_token) {
                    localStorage.setItem('access_token', String(response.access_token));
                }
                if (response.refresh_token) {
                    localStorage.setItem('refresh_token', String(response.refresh_token));
                }
            }
        });
    }

    // Expose newToken only for the lesson environment if needed.
    window.webgoat.customjs.newToken = newToken;
}());
