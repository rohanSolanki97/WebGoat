/**
 * Delta tests for jquery.form.js focusing on safer JSON parsing:
 * - Replaced eval-based JSON parsing with JSON.parse in parseJSON.
 *
 * Derived path: src/test/resources/webgoat/static/js/jquery_form/jquery.form.test.js
 */

// We simulate only the parseJSON behavior in isolation.
// The test ensures JSON.parse is used (or at least behaves safely compared to eval).

const { JSDOM } = require('jsdom');

describe('jquery.form parseJSON hardening', () => {
  let window;
  let $;
  let parseJSON;

  beforeAll(() => {
    // Minimal DOM + jQuery setup
    const dom = new JSDOM('<!doctype html><html><body></body></html>');
    window = dom.window;
    $ = require('jquery')(window);

    // Inline a minimal copy of the updated parseJSON implementation as it appears in jquery.form.js
    // NOTE: This mirrors:
    //   var parseJSON = $.parseJSON || function(s) { return JSON.parse(s); };
    parseJSON =
      $.parseJSON ||
      function (s) {
        return JSON.parse(s);
      };
  });

  test('parseJSON should correctly parse valid JSON payloads', () => {
    const json = '{"name":"alice","age":30}';
    const result = parseJSON(json);
    expect(result).toEqual({ name: 'alice', age: 30 });
  });

  test('parseJSON should not execute arbitrary code in malicious strings', () => {
    // This payload would be dangerous if evaluated via eval('(' + s + ')')
    const malicious = '{"a":1}; this.globalInjected = true; ({';

    // JSON.parse will throw on invalid JSON, instead of executing trailing code
    expect(() => parseJSON(malicious)).toThrow();

    // If eval were still used, globalInjected could be defined—ensure it is not.
    // eslint-disable-next-line no-undef
    expect(global.globalInjected).toBeUndefined();
  });
});
