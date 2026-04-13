// File: src/test/resources/lessons/jwt/js/jwt-refresh.test.js
// Delta tests for jwt-refresh.js focusing on:
// - Removal of hard-coded password from login payload.
// - Correct use of response tokens in login and newToken flows.

const path = require('path');
const fs = require('fs');
const { JSDOM } = require('jsdom');

describe('jwt-refresh.js security remediation (delta tests)', () => {
  let window, document, $, ajaxMock;

  const loadScript = () => {
    const scriptPath = path.resolve(
      __dirname,
      '../../../main/resources/lessons/jwt/js/jwt-refresh.js'
    );
    const code = fs.readFileSync(scriptPath, 'utf8');
    // eslint-disable-next-line no-eval
    eval(code);
  };

  beforeEach(() => {
    const dom = new JSDOM(
      '<!doctype html><html><head></head><body></body></html>',
      { url: 'http://localhost/' }
    );
    window = dom.window;
    document = window.document;
    global.window = window;
    global.document = document;
    global.localStorage = window.localStorage;

    $ = require('jquery')(window);
    global.$ = $;

    global.webgoat = {
      currentUser: { username: 'Alice' },
      customjs: {}
    };

    ajaxMock = jest.spyOn($, 'ajax').mockImplementation(() => ({
      success: (cb) => {
        cb({
          access_token: 'ACCESS_FROM_SERVER',
          refresh_token: 'REFRESH_FROM_SERVER'
        });
        return { success: () => {} };
      }
    }));
  });

  afterEach(() => {
    jest.restoreAllMocks();
    delete global.$;
    delete global.webgoat;
    delete global.window;
    delete global.document;
    delete global.localStorage;
  });

  test('login does not send hard-coded password and uses credentialHint instead', () => {
    loadScript();

    expect(ajaxMock).toHaveBeenCalledTimes(1);
    const call = ajaxMock.mock.calls[0][0];
    expect(call.url).toBe('JWT/refresh/login');

    const payload = JSON.parse(call.data);
    expect(payload.user).toBe('Alice');
    expect(payload.credentialHint).toBe('jwt-refresh-flow');
    expect(payload.password).toBeUndefined();

    expect(localStorage.getItem('access_token')).toBe('ACCESS_FROM_SERVER');
    expect(localStorage.getItem('refresh_token')).toBe('REFRESH_FROM_SERVER');
  });

  test('newToken uses stored refresh_token and updates tokens from server response', () => {
    localStorage.setItem('access_token', 'OLD_ACCESS');
    localStorage.setItem('refresh_token', 'OLD_REFRESH');

    loadScript();

    ajaxMock.mockReset();
    ajaxMock.mockImplementation(() => ({
      success: (cb) => {
        cb({
          access_token: 'NEW_ACCESS',
          refresh_token: 'NEW_REFRESH'
        });
        return { success: () => {} };
      }
    }));

    window.newToken();

    expect(ajaxMock).toHaveBeenCalledTimes(1);
    const call = ajaxMock.mock.calls[0][0];
    const body = JSON.parse(call.data);
    expect(body.refreshToken).toBe('OLD_REFRESH');

    expect(localStorage.getItem('access_token')).toBe('NEW_ACCESS');
    expect(localStorage.getItem('refresh_token')).toBe('NEW_REFRESH');
  });

  test('addBearerToken returns Authorization header using access_token', () => {
    loadScript();

    localStorage.setItem('access_token', 'TOKEN123');

    const headers = webgoat.customjs.addBearerToken();
    expect(headers.Authorization).toBe('Bearer TOKEN123');
  });
});
