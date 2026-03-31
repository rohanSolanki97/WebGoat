$(document).ready(function () {
    // For demo purposes the original code auto‑logged in a hard‑coded user.
    // In a real deployment, this should be driven by explicit user action.
    login('Jerry');
});

/**
 * DO NOT hard‑code credentials or secrets in client‑side JavaScript.
 * The original code contained a hard‑coded password literal which could be trivially extracted.
 * Here we keep the function shape but remove the hard‑coded secret and require the caller
 * to supply a password argument (for training/demo you can inject it from the server).
 */
function login(user, password) {
    if (typeof password !== 'string' || !password.length) {
        // In production you might surface a UI error instead of console logging.
        // We avoid sending any default or guessable password.
        console.error('Password must be provided by a trusted source; hard-coded passwords are not allowed.');
        return;
    }

    $.ajax({
        type: 'POST',
        url: 'JWT/refresh/login',
        contentType: "application/json",
        data: JSON.stringify({ user: user, password: password })
    }).success(
        function (response) {
            // Store only the minimal tokens necessary, and never log them.
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
    // Note: refresh token is read from localStorage, not hard‑coded.
    var refreshToken = localStorage.getItem('refresh_token');
    if (!refreshToken) {
        console.error('No refresh token available to request a new access token.');
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
            // Use server‑returned values, do not rely on undefined client globals
            if (response && response.access_token && response.refresh_token) {
                localStorage.setItem('access_token', response.access_token);
                localStorage.setItem('refresh_token', response.refresh_token);
            }
        }
    );
}
