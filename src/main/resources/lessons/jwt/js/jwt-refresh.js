// Configuration for JWT demo credentials.
// In a real deployment, this must come from a secure configuration source
// (e.g., environment variable, secrets manager) and NOT be hard-coded.
var webgoatJwtConfig = (typeof webgoatJwtConfig !== 'undefined' && webgoatJwtConfig) || {
    // Placeholder only; should be overridden in a secure environment
    demoPasswordProvider: function () {
        // Return a non-sensitive value by default. Real secrets must not be committed to source.
        return '';
    }
};

$(document).ready(function () {
    login('Jerry');
});

function login(user) {
    $.ajax({
        type: 'POST',
        url: 'JWT/refresh/login',
        contentType: "application/json",
        data: JSON.stringify({
            user: user,
            // Removed hard-coded password; now resolved via a configuration-driven provider
            password: webgoatJwtConfig.demoPasswordProvider()
        })
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
};

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
