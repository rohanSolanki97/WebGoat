// src/test/resources/lessons/jwt/js/jwt-refresh.test.js
// Delta tests for jwt-refresh.js focusing on removal of hard-coded password and the new
// getConfiguredPassword()/login() behavior.

const fs = require('fs');
const path = require('path');
const { JSDOM } = require('jsdom');

describe('jwt-refresh.js delta tests for password handling', () => {
  let window;
  let document;
  let $;

  beforeEach(() => {
    const dom = new JSDOM('<!doctype html><html><head></head><body></body></html>', {
      url: 'http://localhost'
    });
    window = dom.window;
    document = window.document;
    global.window = window;
    global.document = document;
    global.localStorage = window.localStorage;

    $ = require('jquery')(window);
    global.$ = $;
    global.jQuery = $;

    global.webgoat = { customjs: {} };

    jest.spyOn($, 'ajax').mockImplementation((options) => {
      const deferred = {
        success: function (cb) {
          cb({ access_token: 'token', refresh_token: 'rtoken' });
          return deferred;
        }
      };
      return deferred;
    });

    const scriptPath = path.resolve(
      __dirname,
      '../../../../main/resources/lessons/jwt/js/jwt-refresh.js'
    );
    const scriptContent = fs.readFileSync(scriptPath, 'utf8');

    expect(scriptContent).not.toMatch(/bm5nhSkxCXZkKRy4/);

    // eslint-disable-next-line no-eval
    eval(scriptContent);
  });

  afterEach(() => {
    if ($ && $.ajax && $.ajax.mockRestore) {
      $.ajax.mockRestore();
    }
  });

  test('getConfiguredPassword returns value from meta data-password when present', () => {
    const meta = document.createElement('meta');
    meta.setAttribute('name', 'webgoat-jwt-password');
    meta.setAttribute('data-password', 'meta-secret');
    document.head.appendChild(meta);

    const password = window.getConfiguredPassword
      ? window.getConfiguredPassword()
      : global.getConfiguredPassword();

    expect(password).toBe('meta-secret');
  });

  test("getConfiguredPassword returns 'CHANGE_ME_IN_CONFIG' when meta not present", () => {
    const password = window.getConfiguredPassword
      ? window.getConfiguredPassword()
      : global.getConfiguredPassword();

    expect(password).toBe('CHANGE_ME_IN_CONFIG');
  });

  test('login passes result of getConfiguredPassword into ajax request payload', () => {
    const meta = document.createElement('meta');
    meta.setAttribute('name', 'webgoat-jwt-password');
    meta.setAttribute('data-password', 'used-in-payload');
    document.head.appendChild(meta);

    const ajaxSpy = $.ajax;

    const loginFn = window.login || global.login;
    loginFn('Jerry');

    expect(ajaxSpy).toHaveBeenCalledTimes(1);
    const callArg = ajaxSpy.mock.calls[0][0];

    expect(callArg.type).toBe('POST');
    expect(callArg.url).toBe('JWT/refresh/login');

    const payload = JSON.parse(callArg.data);
    expect(payload.user).toBe('Jerry');
    expect(payload.password).toBe('used-in-payload');
  });
});
