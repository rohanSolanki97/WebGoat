// Intended test file path (derived from main path by replacing /main/ with /test/):
// src/test/resources/lessons/jwt/js/jwt-refreshTest.js

// NOTE:
// - These delta tests validate that jwt-refresh.js no longer uses a hard-coded password and
//   that it first calls the "demo-password" endpoint, then POSTs to the login endpoint with
//   the password returned from the server.
// - The exact way jwt-refresh.js is loaded in Jest depends on your project setup. Adjust
//   the require() path below so that it imports the updated script into the test environment.

jest.mock('jquery', () => {
  const ajaxMock = jest.fn();
  const $ = function (arg) {
    // Capture document.ready handler without automatically invoking it.
    if (typeof arg === 'function') {
      $.readyHandler = arg;
    }
    return $;
  };
  $.ajax = ajaxMock;
  $.readyHandler = null;
  return $;
});

const $ = require('jquery');

// TODO: Adjust this require path to match how jwt-refresh.js is exposed in your tests.
require('../../../../../main/resources/lessons/jwt/js/jwt-refresh.js');

describe('jwt-refresh.js hard-coded password remediation (delta tests)', () => {
  beforeEach(() => {
    $.ajax.mockReset();
  });

  test('document.ready flow fetches demo password then logs in with returned password', () => {
    const demoPasswordResponse = { password: 'demoSecret' };
    const ajaxCalls = [];

    $.ajax.mockImplementation((options) => {
      ajaxCalls.push(options);
      if (options.url === 'JWT/refresh/demo-password' && options.type === 'GET') {
        // Simulate success of demo-password endpoint
        if (typeof options.done === 'function') {
          options.done(demoPasswordResponse);
        }
        return { done: (cb) => cb(demoPasswordResponse), fail: () => {} };
      }
      if (options.url === 'JWT/refresh/login' && options.type === 'POST') {
        if (typeof options.success === 'function') {
          options.success({ access_token: 'a', refresh_token: 'r' });
        }
        return { success: (cb) => cb({}), fail: () => {} };
      }
      return { done: () => {}, fail: () => {} };
    });

    // Act: manually emulate DOM ready.
    if (typeof $.readyHandler === 'function') {
      $.readyHandler();
    }

    // Assert: first call is to demo-password endpoint
    expect(ajaxCalls.length).toBeGreaterThanOrEqual(2);
    const first = ajaxCalls[0];
    expect(first.url).toBe('JWT/refresh/demo-password');
    expect(first.type).toBe('GET');

    const second = ajaxCalls[1];
    expect(second.url).toBe('JWT/refresh/login');
    expect(second.type).toBe('POST');

    const body = JSON.parse(second.data);
    expect(body.user).toBe('Jerry');
    expect(body.password).toBe('demoSecret');
    // Ensure original hard-coded password literal is not used
    expect(body.password).not.toBe('bm5nhSkxCXZkKRy4');
  });

  test('no AJAX call includes the original hard-coded password literal', () => {
    $.ajax.mockImplementation((options) => {
      return { done: () => {}, fail: () => {}, success: () => {} };
    });

    if (typeof $.readyHandler === 'function') {
      $.readyHandler();
    }

    const calls = $.ajax.mock.calls;
    for (const [opts] of calls) {
      if (opts && typeof opts.data === 'string') {
        const body = JSON.parse(opts.data);
        if (Object.prototype.hasOwnProperty.call(body, 'password')) {
          expect(body.password).not.toBe('bm5nhSkxCXZkKRy4');
        }
      }
    }
  });
});
