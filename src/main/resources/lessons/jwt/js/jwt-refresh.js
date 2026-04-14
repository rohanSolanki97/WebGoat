$(document).ready(function () {
  login('Jerry');
});

function getJwtLoginPayload(user) {
  // Load the password from a non-hardcoded, configuration-driven source.
  // In a real deployment this should come from a secure server-side mechanism
  // or environment-derived configuration, not from client-side code.
  if (typeof window.webgoatConfig === 'object' && window.webgoatConfig.jwtPassword) {
    return { user: user, password: String(window.webgoatConfig.jwtPassword) };
  }

  // Fallback: do not embed a real secret client-side; use a placeholder to
  // avoid hardcoded credentials in source. The backend for this exercise
  // should be configured to accept this non-sensitive value in training mode.
  return { user: user, password: 'TRAINING_ONLY_DO_NOT_USE_IN_PROD' };
}

function login(user) {
  $.ajax({
    type: 'POST',
    url: 'JWT/refresh/login',
    contentType: 'application/json',
    data: JSON.stringify(getJwtLoginPayload(user))
  }).success(function (response) {
    localStorage.setItem('access_token', response['access_token']);
    localStorage.setItem('refresh_token', response['refresh_token']);
  });
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
  // NOTE: The original code read but did not use refreshToken local variables correctly.
  // We retain the flow but treat tokens strictly as data, not hardcoded secrets.
  var refreshToken = localStorage.getItem('refresh_token');
  $.ajax({
    headers: {
      Authorization: 'Bearer ' + localStorage.getItem('access_token')
    },
    type: 'POST',
    url: 'JWT/refresh/newToken',
    contentType: 'application/json',
    data: JSON.stringify({ refreshToken: refreshToken })
  }).success(function (response) {
    // Expect the server to return new tokens; avoid referencing undefined globals.
    if (response && response.access_token) {
      localStorage.setItem('access_token', response.access_token);
    }
    if (response && response.refresh_token) {
      localStorage.setItem('refresh_token', response.refresh_token);
    }
  });
}
