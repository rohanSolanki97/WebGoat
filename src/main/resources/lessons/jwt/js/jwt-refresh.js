$(document).ready(function () {
    login('Jerry');
});

// In production, provide the password via a secure configuration mechanism
// (e.g., environment variable injected into the page, secure config endpoint,
// or a test-only account with no real privileges). For WebGoat’s lesson
// purposes, we read it from a well-known, non-secret placeholder that does
// NOT embed real credentials in source code.
function getConfiguredPassword() {
    // Example: read from a data-* attribute on a meta tag injected at build/deploy time.
    var meta = document.querySelector('meta[name="webgoat-jwt-password"]');
    if (meta && meta.getAttribute('data-password')) {
        return meta.getAttribute('data-password');
    }

    // Fallback: use a clearly non-secret placeholder. This MUST be replaced in any real deployment.
    // This avoids hard-coding real credentials in the script while keeping the lesson functional.
    return 'CHANGE_ME_IN_CONFIG';
}

function login(user) {
    var password = getConfiguredPassword();

    $.ajax({
        type: 'POST',
        url: 'JWT/refresh/login',
        contentType: "application/json",
        data: JSON.stringify({user: user, password: password})
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
