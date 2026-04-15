// File path: src/test/resources/lessons/jwt/js/jwt-refresh.test.js
// Focus: delta on removal of hard-coded password and safer token handling in newToken().

const fs = require('fs');
const path = require('path');

// Provide a minimal jQuery ajax mock
global.$ = {
  ajax: jest.fn(() => ({
    success: function (cb) {
      // Record the callback and allow tests to invoke it
      global.__lastJwtAjaxSuccess = cb;
      return this;
    },
  })),
};

// Provide webgoat.customjs namespace
global.webgoat = { customjs: {} };

// Load the updated script
const scriptPath = path.resolve(
  __dirname,
  '../../../../../main/resources/lessons/jwt/js/jwt-refresh.js'
);
const scriptSource = fs.readFileSync(scriptPath, 'utf8');
// eslint-disable-next-line no-new-func
new Function('window', 'document', '$', 'webgoat', scriptSource)(
  global,
  { readyState: 'complete', addEventListener: () => {} },
  global.$,
  global.webgoat
);

describe('jwt-refresh delta tests (no hard-coded password, safer token handling)', () => {
  beforeEach(() => {
    global.localStorage = (function () {
      let store = {};
      return {
        getItem: (k) => store[k] || null,
        setItem: (k, v) => {
          store[k] = String(v);
        },
        clear: () => {
          store = {};
        },
      };
    })();
    global.console = global.console || {};
    console.error = jest.fn();
    $.ajax.mockClear();
    global.__lastJwtAjaxSuccess = undefined;
  });

  test('login should use runtime-configured password and not a hard-coded value', () => {
    // Arrange: configure runtime password
    global.WEBGOAT_JWT_PASSWORD = 'runtime-secret';

    // Call login directly (it was defined globally by the script)
    // eslint-disable-next-line no-undef
    login('Jerry');

    expect($.ajax).toHaveBeenCalledTimes(1);
    const ajaxArgs = $.ajax.mock.calls[0][0];
    const body = JSON.parse(ajaxArgs.data);

    expect(body.user).toBe('Jerry');
    expect(body.password).toBe('runtime-secret');
    // Ensure the old hard-coded password is not accidentally used
    expect(body.password).not.toBe('bm5nhSkxCXZkKRy4');
  });

  test('login should fail closed and not call ajax when password is not configured', () => {
    // Arrange: ensure password is undefined
    delete global.WEBGOAT_JWT_PASSWORD;

    // eslint-disable-next-line no-undef
    login('Jerry');

    expect($.ajax).not.toHaveBeenCalled();
    expect(console.error).toHaveBeenCalled();
  });

  test('newToken should read from and update localStorage tokens based on response', () => {
    // Arrange
    localStorage.setItem('access_token', 'oldAccess');
    localStorage.setItem('refresh_token', 'oldRefresh');

    // eslint-disable-next-line no-undef
    newToken();

    expect($.ajax).toHaveBeenCalledTimes(1);
    const ajaxArgs = $.ajax.mock.calls[0][0];
    expect(ajaxArgs.headers.Authorization).toBe('Bearer oldAccess');

    const body = JSON.parse(ajaxArgs.data);
    expect(body.refreshToken).toBe('oldRefresh');

    // Simulate successful response callback invoking with new tokens
    const response = {
      access_token: 'newAccess',
      refresh_token: 'newRefresh',
    };
    if (typeof global.__lastJwtAjaxSuccess === 'function') {
      global.__lastJwtAjaxSuccess(response);
    }

    expect(localStorage.getItem('access_token')).toBe('newAccess');
    expect(localStorage.getItem('refresh_token')).toBe('newRefresh');
  });

  test('newToken should not call ajax when refresh token is missing', () => {
    // Arrange
    localStorage.clear();

    // eslint-disable-next-line no-undef
    newToken();

    expect($.ajax).not.toHaveBeenCalled();
    expect(console.error).toHaveBeenCalled();
  });
});
