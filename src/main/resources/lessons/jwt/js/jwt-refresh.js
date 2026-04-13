$(document).ready(function () {
    // Obtain the user in a safer, non-hardcoded way if available.
    // For backwards compatibility, default to "Jerry" when no user
    // can be resolved from the page context.
    var user =
        (typeof webgoat !== 'undefined' &&
            webgoat.currentUser &&
            typeof webgoat.currentUser.username === 'string' &&
            webgoat.currentUser.username.trim().length > 0)
            ? webgoat.currentUser.username.trim()
            : 'Jerry';

    login(user);
});

function login(user) {
    // Remediation: removed hard-coded password from client-side JavaScript.
    // Instead, delegate credential handling to the backend via an opaque,
    // non-sensitive payload. The backend is responsible for authenticating
    // the user using its own secure configuration and credential store.
    $.ajax({
        type: 'POST',
        url: 'JWT/refresh/login',
        contentType: "application/json",
        data: JSON.stringify({
            user: user,
            // The backend should derive or validate credentials without
            // exposing secrets to the client. This is an opaque flag only.
            credentialHint: "jwt-refresh-flow"
        })
    }).success(
        function (response) {
            // Store only the tokens provided by the server. Do not log or
            // otherwise expose these values.
            localStorage.setItem('access_token', response['access_token']);
            localStorage.setItem('refresh_token', response['refresh_token']);
        }
    );
}

// Dev comment: Pass token as header as we had an issue with tokens ending up in the access_log
webgoat.customjs.addBearerToken = function () {
    var headers_to_set = {};
    headers_to_set['Authorization'] = 'Bearer ' + localStorage.getItem('access_token');
    return headers_to_set;
};

// Dev comment: Temporarily disabled from page we need to work out the refresh token flow but for now we can go live with the checkout page
function newToken() {
    // Use the stored refresh token; do not expose any hard-coded secrets here.
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
        contentType: "application/json",
        data: JSON.stringify({ refreshToken: refreshToken })
    }).success(
        function (response) {
            // Remediation: use new tokens provided by the server response
            // instead of undefined local variables.
            if (response && response.access_token && response.refresh_token) {
                localStorage.setItem('access_token', response.access_token);
                localStorage.setItem('refresh_token', response.refresh_token);
            }
        }
    );
}
