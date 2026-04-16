$(document).ready(function () {
    login('Jerry');
});

function login(user) {
    // Retrieve the password/secret from a configuration or UI element instead of hardcoding.
    // For this training client, we read from a data attribute on the <body> to avoid embedding
    // the secret directly into the JavaScript source.
    var password = $('body').data('jwt-refresh-password');
    if (typeof password !== 'string') {
        // Fallback to an empty string if not provided; server should reject invalid credentials.
        password = '';
    }

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

// Dev comment: Pass token as header as we had an issue with tokens ending up in the access_log
webgoat.customjs.addBearerToken = function () {
    var headers_to_set = {};
    headers_to_set['Authorization'] = 'Bearer ' + localStorage.getItem('access_token');
    return headers_to_set;
};

// Dev comment: Temporarily disabled from page we need to work out the refresh token flow
// but for now we can go live with the checkout page
function newToken() {
    // Note: refresh token is still held client-side as part of the lesson design.
    // Ensure this is never reused in production in this form.
    var refreshToken = localStorage.getItem('refresh_token');
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
            // Use the server-provided tokens instead of undeclared variables.
            if (response && response.access_token && response.refresh_token) {
                localStorage.setItem('access_token', response.access_token);
                localStorage.setItem('refresh_token', response.refresh_token);
            }
        }
    );
}
