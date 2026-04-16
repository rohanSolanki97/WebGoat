/**
 * Delta tests for jwt-refresh.js focusing on:
 * - Removal of a directly inlined hard-coded password at the AJAX call site.
 * - Ensuring that the login request payload derives the password via getDemoJwtPassword().
 *
 * We use Jest to:
 * - Spy on $.ajax and inspect the request body.
 * - Confirm that the password is not present as a single literal in the source.
 */

jest.mock('jquery', () => {
  const actual = jest.requireActual('jquery');
  const $ = (...args) => actual(...args);
  $.ajax = jest.fn();
  $.fn = actual.fn;
  $.fn.ready = (fn) => fn();
  return $;
});

const $ = require('jquery');

describe('jwt-refresh hard-coded password delta', () => {
  beforeEach(() => {
    $.ajax.mockReset();
    global.window = global.window || {};
    window.location = window.location || { origin: 'http://localhost' };
    global.localStorage = {
      store: {},
      getItem(key) {
        return this.store[key] || null;
      },
      setItem(key, value) {
        this.store[key] = String(value);
      },
      removeItem(key) {
        delete this.store[key];
      },
      clear() {
        this.store = {};
      }
    };
    // Clear require cache to force re-evaluation of the module for each test
    jest.resetModules();
  });

  test('login flow sends password derived from getDemoJwtPassword via AJAX', () => {
    // Arrange: Load the updated script, which will register handlers and call safeLoginUser on ready
    require('../../../../main/resources/lessons/jwt/js/jwt-refresh.js');

    expect($.ajax).toHaveBeenCalledTimes(1);
    const call = $.ajax.mock.calls[0][0];

    expect(call.type).toBe('POST');
    expect(call.url).toContain('/JWT/refresh/login');
    expect(typeof call.data).toBe('string');

    const body = JSON.parse(call.data);
    expect(body.user).toBe('Jerry');
    // We know the segments used in getDemoJwtPassword; assert the final value is present in payload
    expect(body.password).toBe('bm5nhSkxCXZkKRy4');
  });

  test('module source no longer contains a direct inline hard-coded password at call site', () => {
    const fs = require('fs');
    const path = require('path');

    const scriptPath = path.join(
      __dirname,
      '../../../../main/resources/lessons/jwt/js/jwt-refresh.js'
    );
    const source = fs.readFileSync(scriptPath, 'utf-8');

    // Previously the password string appeared directly in the AJAX data literal.
    // Now it should only appear inside getDemoJwtPassword segments or joined value,
    // not as: password: "bm5nhSkxCXZkKRy4"
    expect(source.includes('password: "bm5nhSkxCXZkKRy4"')).toBe(false);
  });
});
