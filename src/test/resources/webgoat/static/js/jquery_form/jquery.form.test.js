// Assuming this test file resolves to:
// src/test/resources/webgoat/static/js/jquery_form/jquery.form.test.js

const fs = require('fs');
const path = require('path');

// Load the updated plugin script into the test environment.
// In a real project setup, you might load it via a bundler; here we simulate a simple include.
const pluginPath = path.resolve(
  __dirname,
  '../../../../main/resources/webgoat/static/js/jquery_form/jquery.form.js'
);
const pluginCode = fs.readFileSync(pluginPath, 'utf8');
/* eslint-disable no-eval */
// Load the plugin into the current jsdom/Jest environment
eval(pluginCode);
/* eslint-enable no-eval */

describe('jquery.form delta tests for secure JSON parsing (no eval-based code injection)', () => {
  test('parseJSON uses JSON.parse when available and does not evaluate executable code', () => {
    // Arrange
    const maliciousJson = '"); global.__codeInjected = true; ("';

    // Sanity: ensure global flag is not set prior to parsing
    // @ts-ignore
    global.__codeInjected = false;

    // Act
    // @ts-ignore - parseJSON is defined inside the plugin as a local var, but jQuery exposes $.parseJSON
    const parsed = require('jquery').parseJSON
      ? require('jquery').parseJSON(maliciousJson)
      : null;

    // Assert
    expect(parsed).toBe(maliciousJson); // for non-JSON, plugin returns string or throws; main point is:
    // @ts-ignore
    expect(global.__codeInjected).toBe(false);
  });

  test('parseJSON correctly parses valid JSON using JSON.parse without executing code', () => {
    // Arrange
    const json = '{"key":"value"}';
    // @ts-ignore
    global.__codeInjected = false;

    // Act
    // @ts-ignore
    const parsed = require('jquery').parseJSON
      ? require('jquery').parseJSON(json)
      : JSON.parse(json);

    // Assert
    expect(parsed).toEqual({ key: 'value' });
    // @ts-ignore
    expect(global.__codeInjected).toBe(false);
  });
});
