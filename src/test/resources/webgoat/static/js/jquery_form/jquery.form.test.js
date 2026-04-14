// File: src/test/resources/webgoat/static/js/jquery_form/jquery.form.test.js

// Delta tests for jquery.form.js focusing on gated script evaluation (allowScriptEval + same-origin).
// We require the plugin so that it attaches to jQuery. Tests then use $.ajax to trigger httpData.

const $ = require('jquery');

// Require the updated plugin; it augments jQuery.fn and internal helpers.
require('webgoat/static/js/jquery_form/jquery.form.js');

describe('jquery.form delta tests (script eval gating)', () => {
  let originalAjax;
  let executedScripts;

  beforeEach(() => {
    executedScripts = [];
    originalAjax = $.ajax;

    // Spy on globalEval to see when scripts would be executed
    jest.spyOn($, 'globalEval').mockImplementation((code) => {
      executedScripts.push(code);
    });
  });

  afterEach(() => {
    $.ajax = originalAjax;
    $.globalEval.mockRestore();
  });

  function mockAjaxInvokeHttpData(responseOptions, ajaxOptions) {
    // Simulate a minimal jqXHR-like object as expected by the plugin's internal httpData.
    const xhr = {
      getResponseHeader: (header) =>
        header.toLowerCase() === 'content-type'
          ? responseOptions.contentType
          : '',
      responseText: responseOptions.body || '',
      responseXML: null
    };

    // The plugin's httpData is not exported, but the behavior is observable via $.ajax.
    // We intercept $.ajax to call its success handler with a fake xhr.
    $.ajax = jest.fn((opts) => {
      const merged = { ...opts, ...ajaxOptions };
      if (merged.success) {
        // Emulate jQuery passing (data, statusText, jqXHR)
        merged.success(xhr.responseText, 'success', xhr);
      }
      return { then: () => {} };
    });

    return xhr;
  }

  test('should NOT eval script by default even for same-origin script responses', () => {
    // Arrange: same-origin script response, but allowScriptEval not set
    const xhr = mockAjaxInvokeHttpData(
      {
        contentType: 'application/javascript',
        body: 'window.__TEST_MARKER__ = true;'
      },
      { url: window.location.href }
    );

    // Act
    $.ajax({
      url: window.location.href,
      type: 'GET',
      dataType: 'script'
    });

    // Assert: globalEval must not be called without explicit opt-in
    expect($.globalEval).not.toHaveBeenCalled();
    expect(executedScripts).toHaveLength(0);
  });

  test('should eval script only when allowScriptEval is true and same-origin', () => {
    // Arrange
    const scriptBody = 'window.__TEST_MARKER2__ = true;';
    const xhr = mockAjaxInvokeHttpData(
      {
        contentType: 'application/javascript',
        body: scriptBody
      },
      { url: window.location.href }
    );

    // Act
    $.ajax({
      url: window.location.href,
      type: 'GET',
      dataType: 'script',
      allowScriptEval: true
    });

    // Assert: now script should be evaluated
    expect($.globalEval).toHaveBeenCalledWith(scriptBody);
    expect(executedScripts).toContain(scriptBody);
  });

  test('should NOT eval script when URL is cross-origin, even if allowScriptEval is true', () => {
    // Arrange: cross-origin URL different from window.location
    const crossOriginUrl = 'https://attacker.example.com/payload.js';
    const scriptBody = 'window.__TEST_MARKER3__ = true;';
    const xhr = mockAjaxInvokeHttpData(
      {
        contentType: 'application/javascript',
        body: scriptBody
      },
      { url: crossOriginUrl }
    );

    // Act
    $.ajax({
      url: crossOriginUrl,
      type: 'GET',
      dataType: 'script',
      allowScriptEval: true
    });

    // Assert: script should not be evaluated due to same-origin guard
    expect($.globalEval).not.toHaveBeenCalled();
    expect(executedScripts).toHaveLength(0);
  });
});
