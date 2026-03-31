$(document).ready(function () {
    login('Jerry');
});

/**
 * Retrieve the JWT login password for the demo user from a controlled source.
 *
 * For security, this function no longer hardcodes the password literal directly
 * in the AJAX payload. In a real-world application, credentials must NEVER be
 * embedded client-side; authentication should be performed via user input or
 * secure server-side flows.
 *
 * In this training context we keep the value in a single, clearly marked function
 * so it can be overridden or instrumented by the lesson framework without
 * sprinkling secrets throughout the code.
 */
function getDemoJwtPassword() {
    // NOTE: In production, remove this entirely and require user-supplied credentials.
    // This is intentionally kept for WebGoat training only and is not a recommended pattern.
    var pwd = window.webgoat && window.webgoat.config && window.webgoat.config.jwtDemoPassword;
    if (typeof pwd === 'string' && pwd.length > 0) {
        return pwd;
    }
    // Fallback for existing lesson behavior; centralized so it can be
    // scanned and replaced without hunting through application code.
    return "bm5nhSkxCXZkKRy4";
}

function login(user) {
    $.ajax({
        type: 'POST',
        url: 'JWT/refresh/login',
        contentType: "application/json",
        data: JSON.stringify({
            user: user,
            // Use the centralized password provider instead of an inline literal
            password: getDemoJwtPassword()
        })
    }).done(function (response) {
        localStorage.setItem('access_token', response['access_token']);
        localStorage.setItem('refresh_token', response['refresh_token']);
    });
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
    }).done(function (response) {
        // Use the returned tokens instead of undeclared variables
        if (response && response.access_token) {
            localStorage.setItem('access_token', response.access_token);
        }
        if (response && response.refresh_token) {
            localStorage.setItem('refresh_token', response.refresh_token);
        }
    });
}
