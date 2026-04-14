// File: src/test/resources/lessons/jwt/js/jwt-refresh.test.js

// Delta tests for jwt-refresh.js focusing on removal of hardcoded password and correct token refresh.
// We use Jest and mock jQuery.ajax plus a configurable window.webgoatConfig.

global.$ = require('jquery');

require('lessons/jwt/js/jwt-refresh.js');

describe('jwt-refresh delta tests (no hardcoded password, config-driven payload, token handling)', () => {
  let originalAjax;
  let sentRequests;

  beforeEach(() => {
    sentRequests = [];
    originalAjax = $.ajax;

    $.ajax = jest.fn((options) => {
      sentRequests.push(options);
      // Return mock thenable with success chaining
      const api = {
        success(cb) {
          // Simulate server response if provided in test
          if (typeof options.__mockResponse !== 'undefined') {
            cb(options.__mockResponse);
          }
          return api;
        }
      };
      return api;
    });

    // Reset config
    global.window = global.window || {};
    delete window.webgoatConfig;
    localStorage.clear();
  });

  afterEach(() => {
    $.ajax = originalAjax;
  });

  test('login should NOT send the original hardcoded password value', () => {
    // Arrange
    // Ensure no config is set so fallback placeholder is used
    delete window.webgoatConfig;

    // Act
    // login is defined globally in jwt-refresh.js
    global.login('Jerry');

    // Assert
    expect(sentRequests).toHaveLength(1);
    const payload = JSON.parse(sentRequests[0].data);

    // The original vulnerable value was "bm5nhSkxCXZkKRy4"; ensure it is not used anymore.
    expect(payload.password).not.toBe('bm5nhSkxCXZkKRy4');
    // Also ensure password exists but is a non-empty placeholder string
    expect(typeof payload.password).toBe('string');
    expect(payload.password.length).toBeGreaterThan(0);
  });

  test('login should use password from window.webgoatConfig.jwtPassword when provided', () => {
    // Arrange
    window.webgoatConfig = { jwtPassword: 'CONFIG_SECRET' };

    // Act
    global.login('Jerry');

    // Assert
    expect(sentRequests).toHaveLength(1);
    const payload = JSON.parse(sentRequests[0].data);
    expect(payload.password).toBe('CONFIG_SECRET');
  });

  test('newToken should update tokens from server response and not rely on undefined globals', () => {
    // Arrange: seed existing tokens and mock server response
    localStorage.setItem('access_token', 'OLD_ACCESS');
    localStorage.setItem('refresh_token', 'OLD_REFRESH');

    const response = {
      access_token: 'NEW_ACCESS',
      refresh_token: 'NEW_REFRESH'
    };

    // Act
    $.ajax.mockImplementationOnce((options) => {
      // Attach a mock response so the test harness's $.ajax wrapper passes it to success()
      options.__mockResponse = response;
      const api = {
        success(cb) {
          cb(response);
          return api;
        }
      };
      return api;
    });

    global.newToken();

    // Assert: tokens are updated from response, not from undefined apiToken/refreshToken vars
    expect(localStorage.getItem('access_token')).toBe('NEW_ACCESS');
    expect(localStorage.getItem('refresh_token')).toBe('NEW_REFRESH');
  });
});
