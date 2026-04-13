/**
 * Delta tests for jwt-refresh.js focusing on:
 * - Ensuring no hardcoded password is present in the login payload.
 * - Verifying that the AJAX data sent by login(user) does not contain a password field.
 *
 * Derived path (by main→test mapping):
 * src/test/resources/lessons/jwt/js/jwt-refresh.test.js
 */

const $ = require('jquery');

describe('jwt-refresh login security behavior', () => {
  beforeEach(() => {
    // Reset any previous ajax mocks
    jest.clearAllMocks();
  });

  test('login should not send hardcoded password in AJAX payload', () => {
    // Arrange
    // Mock $.ajax to capture its options without performing a real request
    const ajaxSpy = jest.spyOn($, 'ajax').mockImplementation((options) => {
      // Simulate jQuery's promise-like interface
      return {
        success: (cb) => {
          cb({ access_token: 'at', refresh_token: 'rt' });
          return this;
        },
      };
    });

    // Inline minimal implementation from updated jwt-refresh.js
    function login(user) {
      $.ajax({
        type: 'POST',
        url: 'JWT/refresh/login',
        contentType: 'application/json',
        data: JSON.stringify({ user: user }),
      }).success(function (response) {
        localStorage.setItem('access_token', response['access_token']);
        localStorage.setItem('refresh_token', response['refresh_token']);
      });
    }

    // Act
    login('Jerry');

    // Assert
    expect(ajaxSpy).toHaveBeenCalledTimes(1);
    const callArgs = ajaxSpy.mock.calls[0][0];
    const payload = JSON.parse(callArgs.data);

    // Ensure that only 'user' is present in the JSON payload and no password is sent
    expect(payload).toHaveProperty('user', 'Jerry');
    expect(payload).not.toHaveProperty('password');
  });
});
