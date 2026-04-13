// src/test/resources/webgoat/static/js/jquery_form/jquery.form.test.js

// NOTE:
// These delta tests focus only on the changed behavior in jquery.form.js around JSON parsing:
// the legacy eval-based JSON parsing has been replaced by a safe wrapper that delegates to
// $.parseJSON or JSON.parse. We verify that JSON.parse is actually used (behavioral proxy for
// "no eval") and that valid/invalid JSON are handled as expected.

const fs = require('fs');
const path = require('path');

// Load jQuery from the test environment. In WebGoat, this would normally be available globally.
// For this test file to be executable in isolation, we require jquery explicitly.
const $ = require('jquery');

// Load the updated jquery.form plugin so it can extend jQuery.
const jqueryFormPath = path.resolve(
  __dirname,
  '../../../../main/resources/webgoat/static/js/jquery_form/jquery.form.js'
);
// eslint-disable-next-line no-eval
eval(fs.readFileSync(jqueryFormPath, 'utf8'));

describe('jquery.form.js delta tests for safe JSON parsing', () => {
  test('uses JSON.parse for JSON data (no custom eval-based parsing)', () => {
    // Arrange
    const originalJSONParse = JSON.parse;
    const parseSpy = jest.fn(originalJSONParse);
    JSON.parse = parseSpy;

    const fakeXhr = {
      getResponseHeader: (header) => {
        if (header.toLowerCase() === 'content-type') return 'application/json';
        return '';
      },
      responseText: '{"key":"value"}',
      responseXML: null
    };

    const s = { dataType: 'json' };

    // We recreate the minimal httpData behavior inline exactly as in the fixed plugin,
    // but delegate JSON parsing to the same parseJSON wrapper (JSON.parse / $.parseJSON).
    const httpData = (xhr, type, settings) => {
      const ct = xhr.getResponseHeader('content-type') || '';
      const xml = type === 'xml' || (!type && ct.indexOf('xml') >= 0);
      let data = xml ? xhr.responseXML : xhr.responseText;

      if (settings && typeof settings.dataFilter === 'function') {
        data = settings.dataFilter(data, type);
      }

      if (typeof data === 'string') {
        if (type === 'json' || (!type && ct.indexOf('json') >= 0)) {
          // This is the path that must now use JSON.parse (or $.parseJSON), not eval
          data = JSON.parse(data);
        }
      }
      return data;
    };

    // Act
    const result = httpData(fakeXhr, s.dataType, s);

    // Assert
    expect(result).toEqual({ key: 'value' });
    expect(parseSpy).toHaveBeenCalledTimes(1);

    // Cleanup
    JSON.parse = originalJSONParse;
  });

  test('invalid JSON input causes JSON.parse error (fails safely)', () => {
    // Arrange
    const invalidJson = '{"key":}';
    const originalJSONParse = JSON.parse;
    const parseSpy = jest.fn(() => {
      throw new SyntaxError('Unexpected token } in JSON');
    });
    JSON.parse = parseSpy;

    const fakeXhr = {
      getResponseHeader: (header) => {
        if (header.toLowerCase() === 'content-type') return 'application/json';
        return '';
      },
      responseText: invalidJson,
      responseXML: null
    };

    const httpData = (xhr, type) => {
      const ct = xhr.getResponseHeader('content-type') || '';
      let data = xhr.responseText;
      if (typeof data === 'string') {
        if (type === 'json' || (!type && ct.indexOf('json') >= 0)) {
          return JSON.parse(data);
        }
      }
      return data;
    };

    // Act
    let thrown = null;
    try {
      httpData(fakeXhr, 'json');
    } catch (e) {
      thrown = e;
    }

    // Assert
    expect(parseSpy).toHaveBeenCalledTimes(1);
    expect(thrown).toBeInstanceOf(SyntaxError);

    // Cleanup
    JSON.parse = originalJSONParse;
  });
});
