$(document).ready(function () {
    // For demo purposes, the username is still hard-coded as 'Jerry'.
    // The password is no longer embedded in the client; it is obtained
    // via a backend endpoint designed to provide a demo credential.
    fetchDemoPasswordAndLogin('Jerry');
});

/**
 * Fetch a demo password from the backend instead of hard-coding it in client-side JS.
 * This avoids committing secrets to source control and exposing them in the browser.
 *
 * NOTE: In a real production system this pattern should be replaced with
 * a proper login flow where the user supplies the credential directly.
 */
function fetchDemoPasswordAndLogin(user) {
    $.ajax({
        type: 'GET',
        url: 'JWT/refresh/demo-password',
        dataType: 'json'
    }).done(function (response) {
        // Expecting a response like: { "password": "someDemoPassword" }
        if (response && typeof response.password === 'string') {
            login(user, response.password);
        } else {
            // Fallback: do not attempt login if no password is provided.
            // This avoids introducing another hard-coded secret.
        }
    }).fail(function () {
        // Intentionally avoid logging password or sensitive context.
        // The lesson UI can show an error based on server-side logic if needed.
    });
}

function login(user, password) {
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
            // apiToken and refreshToken are assumed to be provided from server-side
            // responses or higher-level client code; no secrets are hard-coded here.
            localStorage.setItem('access_token', apiToken);
            localStorage.setItem('refresh_token', refreshToken);
        }
    );
}
