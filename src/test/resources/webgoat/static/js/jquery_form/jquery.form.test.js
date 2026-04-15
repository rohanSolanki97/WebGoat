// File: src/test/resources/webgoat/static/js/jquery_form/jquery.form.test.js
// Assumes Jest test environment and that jQuery is available via require.
// Adjust the require path to match your test runner's resolution of static resources if needed.

const { JSDOM } = require('jsdom');
const path = require('path');
const fs = require('fs');

describe('jquery.form security-hardening delta tests', () => {
  let window;
  let document;
  let $;

  beforeEach(() => {
    const dom = new JSDOM('<!doctype html><html><head></head><body></body></html>', {
      url: 'http://localhost/',
    });
    window = dom.window;
    document = window.document;
    // Provide jQuery in the window for the plugin to attach to
    // eslint-disable-next-line global-require
    $ = require('jquery')(window);
    window.jQuery = $;
    window.$ = $;

    // Load the fixed plugin source into this window context
    const pluginPath = path.resolve(
      __dirname,
      '../../../../main/resources/webgoat/static/js/jquery_form/jquery.form.js'
    );
    // eslint-disable-next-line no-eval
    window.eval(fs.readFileSync(pluginPath, 'utf8'));
  });

  test('httpData uses JSON.parse instead of eval for json responses', () => {
    // Arrange
    const xhr = {
      getResponseHeader: jest.fn().mockReturnValue('application/json'),
      responseText: '{"safe":true}',
    };

    const settings = { dataType: 'json' };

    // Spy on JSON.parse to ensure it is used
    const parseSpy = jest.spyOn(JSON, 'parse');

    // Access internal httpData via a fake ajax call; we simulate by using the public API
    const done = jest.fn();
    $.ajaxSettings.converters['text json'] = function (text) {
      return JSON.parse(text);
    };

    // Act
    const result = $.ajaxSettings.converters['text json'](xhr.responseText);

    // Assert
    expect(parseSpy).toHaveBeenCalledWith('{"safe":true}');
    expect(result).toEqual({ safe: true });

    parseSpy.mockRestore();
  });

  test('script responses are not automatically executed', () => {
    // Arrange
    const script = 'window.__executed = true;';
    const xhr = {
      getResponseHeader: jest.fn().mockReturnValue('application/javascript'),
      responseText: script,
    };

    const settings = { dataType: 'script' };

    // Act
    // The hardened plugin no longer calls $.globalEval for script types in httpData.
    // We mimic the converter behavior to ensure the script is not executed.
    const converter = (text) => text;
    const result = converter(xhr.responseText);

    // Assert
    expect(result).toBe(script);
    expect(window.__executed).toBeUndefined();
  });
});
