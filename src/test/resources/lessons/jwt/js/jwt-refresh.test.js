// Resolved test path (derived from src/main/resources → src/test/resources):
// src/test/resources/lessons/jwt/js/jwt-refresh.test.js

const vm = require('vm');
const fs = require('fs');
const path = require('path');

describe('jwt-refresh.js delta tests – password sourcing and login payload', () => {
  let sandbox;
  let ajaxMock;

  function loadScript() {
    sandbox = {
      window: {},
      localStorage: (function () {
        let store = {};
        return {
          getItem: (k) => store[k] || null,
          setItem: (k, v) => {
            store[k] = String(v);
          }
        };
      })(),
      webgoat: { customjs: {} },
      console
    };

    ajaxMock = jest.fn().mockReturnValue({ success: (cb) => cb({}) });

    const $ = function () {
      return {
        ready: (cb) => cb()
      };
    };
    $.ajax = ajaxMock;

    sandbox.$ = $;
    sandbox.jQuery = $;

    const scriptPath = path.resolve(
      __dirname,
      '../../../../../main/resources/lessons/jwt/js/jwt-refresh.js'
    );
    const code = fs.readFileSync(scriptPath, 'utf8');
    vm.runInNewContext(code, sandbox);
  }

  beforeEach(() => {
    loadScript();
  });

  test('getJwtDemoPassword uses window.webgoat.jwtDemoPassword when present', () => {
    sandbox.window.webgoat = sandbox.window.webgoat || {};
    sandbox.window.webgoat.jwtDemoPassword = 'CONFIGURED_SECRET';

    sandbox.login('TestUser');

    expect(ajaxMock).toHaveBeenCalled();
    const callArgs = ajaxMock.mock.calls[0][0];
    const body = JSON.parse(callArgs.data);
    expect(body.password).toBe('CONFIGURED_SECRET');
  });

  test('getJwtDemoPassword falls back to placeholder when config is absent', () => {
    sandbox.window.webgoat = {};

    sandbox.login('TestUser');

    expect(ajaxMock).toHaveBeenCalled();
    const callArgs = ajaxMock.mock.calls[0][0];
    const body = JSON.parse(callArgs.data);

    expect(body.password).toBe('CHANGE_ME_IN_CONFIG');
    expect(body.password).not.toBe('bm5nhSkxCXZkKRy4');
  });

  test('login payload no longer contains old hardcoded password literal', () => {
    sandbox.login('Jerry');

    expect(ajaxMock).toHaveBeenCalled();
    const body = JSON.parse(ajaxMock.mock.calls[0][0].data);

    expect(body.password).not.toBe('bm5nhSkxCXZkKRy4');
  });
});
