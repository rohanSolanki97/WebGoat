// File: src/test/resources/lessons/jwt/js/jwt-refresh.test.js
// Derived from source path by replacing '/main/' with '/test/':
//   src/main/resources/lessons/jwt/js/jwt-refresh.js
//   -> src/test/resources/lessons/jwt/js/jwt-refresh.test.js

// This test focuses only on the changed behavior:
// - No hard-coded password is used in login()
// - login() requires a non-empty password and fails closed otherwise
// - newToken() uses refresh token from localStorage and server response values

// JSDOM environment provided by Jest is assumed.

require('../../../../main/resources/lessons/jwt/js/jwt-refresh.js');

describe('jwt-refresh delta tests', () => {
  let originalAjax;

  beforeEach(() => {
    originalAjax = global.$ && global.$.ajax;
    global.$ = {
      ajax: jest.fn().mockReturnValue({
        success: function (cb) {
          // Allow chaining
          this._success = cb;
          return this;
        },
      }),
    };
    global.localStorage = (function () {
      let store = {};
      return {
        getItem: (key) => store[key] || null,
        setItem: (key, value) => {
          store[key] = String(value);
        },
        clear: () => {
          store = {};
        },
      };
    })();
    jest.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    if (originalAjax) {
      global.$.ajax = originalAjax;
    }
    console.error.mockRestore();
  });

  test('login should require a non-empty password and not send a hard-coded secret', () => {
    // login is defined in jwt-refresh.js in the global scope
    expect(typeof login).toBe('function');

    login('Jerry', '');

    // When password is empty, login must not issue an AJAX call
    expect($.ajax).not.toHaveBeenCalled();
    expect(console.error).toHaveBeenCalled();

    // With a provided password, it must be sent as provided and not overridden
    console.error.mockClear();
    const pwd = 'UserSuppliedSecret!';
    login('Jerry', pwd);

    expect($.ajax).toHaveBeenCalledTimes(1);
    const arg = $.ajax.mock.calls[0][0];
    const body = JSON.parse(arg.data);

    expect(body.user).toBe('Jerry');
    expect(body.password).toBe(pwd);
  });

  test('newToken should not proceed when no refresh token is stored', () => {
    expect(typeof newToken).toBe('function');

    newToken();

    expect($.ajax).not.toHaveBeenCalled();
    expect(console.error).toHaveBeenCalled();
  });

  test('newToken should use server response tokens instead of undefined globals', () => {
    localStorage.setItem('access_token', 'oldAccess');
    localStorage.setItem('refresh_token', 'oldRefresh');

    const ajaxReturn = {
      success: function (cb) {
        this._success = cb;
        return this;
      },
      _success: null,
    };
    $.ajax.mockReturnValue(ajaxReturn);

    newToken();

    expect($.ajax).toHaveBeenCalledTimes(1);
    const args = $.ajax.mock.calls[0][0];
    expect(JSON.parse(args.data)).toEqual({ refreshToken: 'oldRefresh' });

    // Simulate server response with new tokens
    const response = {
      access_token: 'newAccess',
      refresh_token: 'newRefresh',
    };
    ajaxReturn._success(response);

    expect(localStorage.getItem('access_token')).toBe('newAccess');
    expect(localStorage.getItem('refresh_token')).toBe('newRefresh');
  });
});
