$(document).ready(function () {
    login('Jerry');
});

function login(user) {
    // Read the password from a secure runtime configuration instead of hard-coding it
    var password = window.WEBGOAT_JWT_PASSWORD;
    if (typeof password !== 'string' || !password.length) {
        // Fail closed if password is not securely configured
        console.error('JWT login password is not configured securely.');
        return;
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

//Dev comment: Pass token as header as we had an issue with tokens ending up in the access_log
webgoat.customjs.addBearerToken = function () {
    var headers_to_set = {};
    var token = localStorage.getItem('access_token');
    if (typeof token === 'string' && token.length > 0) {
        headers_to_set['Authorization'] = 'Bearer ' + token;
    }
    return headers_to_set;
};

//Dev comment: Temporarily disabled from page we need to work out the refresh token flow but for now we can go live with the checkout page
function newToken() {
    var refreshToken = localStorage.getItem('refresh_token');
    if (typeof refreshToken !== 'string' || !refreshToken.length) {
        console.error('No refresh token present; cannot request new token.');
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
            if (response && typeof response.access_token === 'string') {
                localStorage.setItem('access_token', response.access_token);
            }
            if (response && typeof response.refresh_token === 'string') {
                localStorage.setItem('refresh_token', response.refresh_token);
            }
        }
    );
}
