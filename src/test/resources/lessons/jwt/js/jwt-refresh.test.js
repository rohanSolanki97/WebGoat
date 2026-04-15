const { JSDOM } = require('jsdom');
const path = require('path');
const fs = require('fs');

describe('jwt-refresh delta tests', () => {
  let window;
  let document;
  let $;

  beforeEach(() => {
    const dom = new JSDOM('<!doctype html><html><head></head><body></body></html>', {
      url: 'http://localhost/',
      runScripts: 'dangerously',
      resources: 'usable',
    });
    window = dom.window;
    document = window.document;

    $ = require('jquery')(window);
    window.$ = $;
    window.jQuery = $;

    window.webgoat = { customjs: {} };
    window.localStorage = {
      store: {},
      setItem(key, value) {
        this.store[key] = String(value);
      },
      getItem(key) {
        return this.store[key] || null;
      },
      removeItem(key) {
        delete this.store[key];
      },
      clear() {
        this.store = {};
      },
    };

    // Prepare a controllable config object for password
    window.webgoatConfig = {};
  });

  function loadJwtRefreshScript() {
    const scriptPath = path.resolve(
      __dirname,
      '../../../../main/resources/lessons/jwt/js/jwt-refresh.js'
    );
    const src = fs.readFileSync(scriptPath, 'utf8');
    // eslint-disable-next-line no-eval
    window.eval(src);
  }

  test('login_usesPasswordFromWebgoatConfigWhenProvided', () => {
    // Arrange
    window.webgoatConfig.jwtDemoPassword = 'configProvidedPassword';

    const ajaxMock = jest.fn().mockReturnValue({ success: jest.fn() });
    $.ajax = ajaxMock;

    // Act
    loadJwtRefreshScript(); // this will also call login('Jerry') on document.ready

    // Assert
    expect(ajaxMock).toHaveBeenCalledTimes(1);
    const ajaxArgs = ajaxMock.mock.calls[0][0];
    const sentBody = JSON.parse(ajaxArgs.data);
    expect(sentBody.password).toBe('configProvidedPassword');
    expect(sentBody.user).toBe('Jerry');
  });

  test('login_fallsBackToDemoPasswordWhenConfigMissing', () => {
    // Arrange
    delete window.webgoatConfig.jwtDemoPassword;

    const ajaxMock = jest.fn().mockReturnValue({ success: jest.fn() });
    $.ajax = ajaxMock;

    // Act
    loadJwtRefreshScript();

    // Assert
    const ajaxArgs = ajaxMock.mock.calls[0][0];
    const sentBody = JSON.parse(ajaxArgs.data);
    expect(sentBody.password).toBe('jwt-demo-password');
  });
});
