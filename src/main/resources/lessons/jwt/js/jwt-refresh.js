// Ensure a global configuration object exists for providing non-hardcoded secrets.
// In the real system this should be populated from a secure source (env, vault, etc.),
// NOT from inline literals. This stub is here only to avoid breaking existing flows
// when configuration is missing.
window.webgoat = window.webgoat || {};
webgoat.config = webgoat.config || {};
// WARNING: Do not hardcode secrets here in production. Use environment/secret management.
// Example (non-production):
//   webgoat.config.jwtPassword = window.__JWT_PASSWORD_FROM_ENV__;

/**
 * Retrieve the JWT password from a secure configuration source.
 * This function centralizes access so that:
 * - No secrets are inlined in code;
 * - Future migrations to vaults/env vars require changes in only one place.
 */
function getJwtPassword() {
    // Prefer an explicitly provided secure config value, if present.
    if (webgoat.config && typeof webgoat.config.jwtPassword === 'string') {
        return webgoat.config.jwtPassword;
    }

    // Fallback: no password available. In a real deployment, you should fail closed here.
    // We keep a null/empty fallback to preserve lesson wiring without embedding a secret.
    return '';
}

$(document).ready(function () {
    login('Jerry');
});

function login(user) {
    const password = getJwtPassword();

    // It is strongly recommended that 'password' be non-empty and
    // provided from a secure configuration source.
    $.ajax({
        type: 'POST',
        url: 'JWT/refresh/login',
        contentType: "application/json",
        data: JSON.stringify({ user: user, password: password })
    }).success(
        function (response) {
            localStorage.setItem('access_token', response['access_token']);
            localStorage.setItem('refresh_token', response['refresh_token']);
        }
    );
}

//Dev comment: Pass token as header as we had an issue with tokens ending up in the access_log
webgoat.customjs.addBearerToken = function () {
    var headers_to_set = {};
    headers_to_set['Authorization'] = 'Bearer ' + localStorage.getItem('access_token');
    return headers_to_set;
}

//Dev comment: Temporarily disabled from page we need to work out the refresh token flow but for now we can go live with the checkout page
function newToken() {
    localStorage.getItem('refreshToken');
    $.ajax({
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('access_token')
        },
        type: 'POST',
        url: 'JWT/refresh/newToken',
        data: JSON.stringify({refreshToken: localStorage.getItem('refresh_token')})
    }).success(
        function () {
            localStorage.setItem('access_token', apiToken);
            localStorage.setItem('refresh_token', refreshToken);
        }
    );
}
