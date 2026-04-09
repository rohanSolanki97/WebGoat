// Simple, configurable accessor to avoid hardcoded secrets in source code.
// In a real deployment, this should be wired to a secure configuration source
// (environment variables, injected config object, or a secrets manager).
function getJwtDemoPassword() {
    // Prefer an externally provided value; fall back to a non-secret placeholder for demos.
    // NOTE: Do NOT put real secrets here; use environment/config at runtime.
    if (typeof window !== 'undefined' && window.webgoat && window.webgoat.jwtDemoPassword) {
        return window.webgoat.jwtDemoPassword;
    }

    // Fallback placeholder that is NOT a production secret.
    return 'CHANGE_ME_IN_CONFIG';
}

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
            // Previously hard-coded password was here; now retrieved via config accessor.
            password: getJwtDemoPassword()
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
