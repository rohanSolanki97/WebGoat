/**
 * Delta Jest tests for jwt-refresh.js focusing on the changed behavior:
 * - Hard-coded password has been removed from the login payload.
 * - getDemoPassword() now provides a non-secret demo value, optionally via a meta data attribute.
 * - newToken uses server response fields instead of undefined variables.
 *
 * These tests verify:
 * - login() sends password from getDemoPassword(), not a hard-coded literal.
 * - getDemoPassword() reads from the configured meta tag and falls back when absent.
 * - newToken() updates tokens from the response object.
 *
 * Test file path (derived from main path):
 * src/test/resources/lessons/jwt/js/jwt-refresh.test.js
 */

const path = require('path');
const { JSDOM } = require('jsdom');

function loadJwtRefreshDom(html, url) {
  const dom = new JSDOM(html, { url: url || 'http://localhost/' });
  global.window = dom.window;
  global.document = dom.window.document;
  global.navigator = dom.window.navigator;
  global.localStorage = dom.window.localStorage;
  global.$ = require('jquery')(dom.window);
  global.jQuery = global.$;

  const scriptPath = path.resolve(
    __dirname,
    '../../../../main/resources/lessons/jwt/js/jwt-refresh.js'
  );
  // eslint-disable-next-line global-require, import/no-dynamic-require
  require(scriptPath);

  return dom;
}

describe('jwt-refresh.js delta tests for demo password and token handling', () => {
  afterEach(() => {
    delete global.window;
    delete global.document;
    delete global.navigator;
    delete global.localStorage;
    delete global.$;
    delete global.jQuery;
    delete global.webgoat;
    jest.resetModules();
  });

  test('login uses demo password from meta tag and not a hard-coded secret', () => {
    const html = `
      <html>
        <head>
          <meta id="jwt-demo-password" data-password="demo-from-meta" />
        </head>
        <body></body>
      </html>
    `;

    loadJwtRefreshDom(html);

    const ajaxSpy = jest.spyOn(global.$, 'ajax').mockImplementation((opts) => {
      if (opts && typeof opts.success === 'function') {
        opts.success({ access_token: 'a', refresh_token: 'r' });
      }
      return { success: jest.fn() };
    });

    global.$(document).trigger('ready');

    expect(ajaxSpy).toHaveBeenCalled();
    const callArg = ajaxSpy.mock.calls[0][0];
    const payload = JSON.parse(callArg.data);
    expect(payload.password).toBe('demo-from-meta');
    expect(payload.password).not.toBe('bm5nhSkxCXZkKRy4');

    ajaxSpy.mockRestore();
  });

  test('login falls back to non-secret default password when meta tag is absent', () => {
    const html = `<html><head></head><body></body></html>`;
    loadJwtRefreshDom(html);

    const ajaxSpy = jest.spyOn(global.$, 'ajax').mockImplementation((opts) => {
      if (opts && typeof opts.success === 'function') {
        opts.success({ access_token: 'a', refresh_token: 'r' });
      }
      return { success: jest.fn() };
    });

    global.$(document).trigger('ready');

    const payload = JSON.parse(ajaxSpy.mock.calls[0][0].data);
    expect(payload.password).toBe('demo-password');
    expect(payload.password).not.toBe('bm5nhSkxCXZkKRy4');

    ajaxSpy.mockRestore();
  });

  test('newToken updates tokens from server response instead of undefined variables', () => {
    const html = `<html><head></head><body></body></html>`;
    loadJwtRefreshDom(html);

    global.localStorage.setItem('access_token', 'old-access');
    global.localStorage.setItem('refresh_token', 'old-refresh');

    const ajaxSpy = jest.spyOn(global.$, 'ajax').mockImplementation((opts) => {
      if (opts && typeof opts.success === 'function') {
        opts.success({
          access_token: 'new-access',
          refresh_token: 'new-refresh'
        });
      }
      return { success: jest.fn() };
    });

    global.webgoat.customjs.addBearerToken();
    // newToken is defined inside the IIFE, so we trigger it indirectly by attaching it to window
    // for testing via a small shim.
    const newTokenFn = global.window.newToken || global.window['newToken'];
    if (typeof newTokenFn === 'function') {
      newTokenFn();
    }

    expect(global.localStorage.getItem('access_token')).toBe('new-access');
    expect(global.localStorage.getItem('refresh_token')).toBe('new-refresh');

    ajaxSpy.mockRestore();
  });
});
