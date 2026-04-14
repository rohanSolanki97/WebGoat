// File: src/test/resources/lessons/jwt/js/jwt-refresh.test.js
// Delta tests for jwt-refresh.js focusing on:
// - The new configuration-driven credential provider (no hard-coded password).
// - The login() function using window.webgoat.config.getJwtRefreshCredentials.
// These tests assume jQuery is available in the test environment.

describe('jwt-refresh credential provider delta tests', () => {
  beforeEach(() => {
    // Reset global webgoat object before each test
    global.window = global.window || {};
    window.webgoat = {};
    window.webgoat.config = {};
    window.webgoat.customjs = {};
    // Stub jQuery ajax
    global.$ = {
      ajax: jest.fn().mockReturnValue({ success: function (cb) { cb({}); } })
    };
    // Load the updated script under test
    jest.resetModules();
    require('../../../../main/resources/lessons/jwt/js/jwt-refresh.js');
  });

  test('default credential provider returns placeholder password, not hard-coded secret', () => {
    // Arrange
    const creds = window.webgoat.config.getJwtRefreshCredentials('Jerry');

    // Assert
    expect(creds.user).toBe('Jerry');
    expect(creds.password).toBe('CHANGE_ME_IN_SECURE_CONFIG');
  });

  test('login uses credential provider rather than hard-coded password', () => {
    // Arrange: override provider to a test-specific value and track what AJAX receives
    const providedCreds = { user: 'Jerry', password: 'test-password' };
    window.webgoat.config.getJwtRefreshCredentials = jest.fn().mockReturnValue(providedCreds);

    const ajaxSpy = jest.spyOn($, 'ajax').mockReturnValue({
      success: function (cb) {
        cb({ access_token: 'a', refresh_token: 'r' });
      }
    });

    // Require again to use the overridden provider and expose login()
    jest.resetModules();
    require('../../../../main/resources/lessons/jwt/js/jwt-refresh.js');

    // Act: call login via the global function defined in the script
    // eslint-disable-next-line no-undef
    login('Jerry');

    // Assert: ajax should be called with JSON-stringified credentials from the provider
    expect(window.webgoat.config.getJwtRefreshCredentials).toHaveBeenCalledWith('Jerry');
    expect(ajaxSpy).toHaveBeenCalled();
    const ajaxArgs = ajaxSpy.mock.calls[0][0];
    expect(JSON.parse(ajaxArgs.data)).toEqual(providedCreds);
  });
});
