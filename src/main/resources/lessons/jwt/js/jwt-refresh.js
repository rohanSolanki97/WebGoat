$(document).ready(function () {
    // NOTE:
    // In the original vulnerable version, a hard-coded password value was embedded here.
    // To avoid shipping secrets in client-side code, the password is now provided by a
    // configuration function that can be overridden in a secure, non-version-controlled
    // script or environment-specific bundle.
    login('Jerry');
});

// Configuration provider for authentication settings.
// Default implementation returns a benign placeholder and is expected to be
// overridden in a secure environment-specific script that is NOT committed to VCS.
window.webgoat = window.webgoat || {};
window.webgoat.config = window.webgoat.config || {};

/**
 * Returns credentials (or token parameters) for the JWT refresh login.
 * The default implementation intentionally does NOT contain a secret.
 *
 * Production / training environments should override this function at runtime, for example:
 *
 *   window.webgoat.config.getJwtRefreshCredentials = function(user) {
 *       return { user: user, password: window.SECURE_CONFIG.JWT_REFRESH_PASSWORD };
 *   };
 *
 * where `window.SECURE_CONFIG` is injected by a secure, environment-specific mechanism.
 */
window.webgoat.config.getJwtRefreshCredentials =
    window.webgoat.config.getJwtRefreshCredentials ||
    function (user) {
        // Placeholder password for local/demo use; not a real secret.
        // In real deployments, this must be replaced via a secure configuration channel.
        return { user: user, password: 'CHANGE_ME_IN_SECURE_CONFIG' };
    };

function login(user) {
    var credentials = window.webgoat.config.getJwtRefreshCredentials(user);

    $.ajax({
        type: 'POST',
        url: 'JWT/refresh/login',
        contentType: "application/json",
        data: JSON.stringify(credentials)
    }).success(
        function (response) {
            localStorage.setItem('access_token', response['access_token']);
            localStorage.setItem('refresh_token', response['refresh_token']);
        }
    );
}

//Dev comment: Pass token as header as we had an issue with tokens ending up in the access_log
webgoat.customjs = window.webgoat.customjs || {};
webgoat.customjs.addBearerToken = function () {
    var headers_to_set = {};
    headers_to_set['Authorization'] = 'Bearer ' + localStorage.getItem('access_token');
    return headers_to_set;
};

//Dev comment: Temporarily disabled from page we need to work out the refresh token flow
// but for now we can go live with the checkout page
function newToken() {
    // NOTE: refresh token is taken from localStorage; no hard-coded secrets here.
    $.ajax({
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('access_token')
        },
        type: 'POST',
        url: 'JWT/refresh/newToken',
        data: JSON.stringify({refreshToken: localStorage.getItem('refresh_token')})
    }).success(
        function (response) {
            // Assuming backend responds with new tokens; update from response rather than
            // undeclared variables apiToken/refreshToken to avoid confusion.
            if (response && response.access_token && response.refresh_token) {
                localStorage.setItem('access_token', response.access_token);
                localStorage.setItem('refresh_token', response.refresh_token);
            }
        }
    );
}
