const vm = require('vm');
const fs = require('fs');
const path = require('path');

describe('jwt-refresh.js delta security behavior', () => {
  function loadScriptIntoSandbox() {
    const sandbox = {
      window: {},
      localStorage: {
        _store: {},
        setItem(key, value) {
          this._store[key] = String(value);
        },
        getItem(key) {
          return this._store[key];
        }
      },
      webgoat: { customjs: {}, config: {} },
      document: { readyState: 'complete' },
      $: function () {},
    };

    // Basic jQuery-like ready stub and ajax stub
    sandbox.$ = jest.fn((arg) => {
      if (typeof arg === 'function') {
        // Simulate $(document).ready(...)
        arg();
      }
      return { ajax: sandbox.$.ajaxMock };
    });
    sandbox.$.ajaxMock = jest.fn().mockReturnValue({
      success: function (cb) {
        // Invoke success callback with a sample response
        cb({ access_token: 'at', refresh_token: 'rt' });
        return this;
      },
    });
    sandbox.$.ajax = sandbox.$.ajaxMock;

    sandbox.window = sandbox;

    const scriptPath = path.join(
      __dirname,
      '..',
      '..',
      '..',
      'main',
      'resources',
      'lessons',
      'jwt',
      'js',
      'jwt-refresh.js'
    );
    const source = fs.readFileSync(scriptPath, 'utf8');

    vm.runInNewContext(source, sandbox);
    return sandbox;
  }

  test('login uses getJwtPassword() value from webgoat.config and not a hardcoded literal', () => {
    const sandbox = loadScriptIntoSandbox();

    // Configure a non-default, clearly identifiable password value
    sandbox.webgoat.config.jwtPassword = 'CONFIG_DRIVEN_SECRET';

    // Re-run login explicitly to ensure it uses getJwtPassword()
    sandbox.login('Jerry');

    // Capture the ajax payload used in the last call
    expect(sandbox.$.ajaxMock).toHaveBeenCalled();
    const lastCall = sandbox.$.ajaxMock.mock.calls[sandbox.$.ajaxMock.mock.calls.length - 1][0];

    // The password field in the JSON body should equal the configured secret
    const payload = JSON.parse(lastCall.data);
    expect(payload.user).toBe('Jerry');
    expect(payload.password).toBe('CONFIG_DRIVEN_SECRET');

    // Ensure no obvious hardcoded password literal appears in the payload
    const disallowedLiteral = 'bm5nhSkxCXZkKRy4';
    expect(JSON.stringify(payload)).not.toContain(disallowedLiteral);
  });
});
