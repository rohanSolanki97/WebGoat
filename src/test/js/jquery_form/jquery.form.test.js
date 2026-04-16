// Assuming this Jest test lives under a test root that can require the plugin file directly.
// The updated plugin disables eval-based JSON parsing and automatic script execution.
// These delta tests focus on ensuring that:
// - JSON is parsed only through safe parsers and does not use eval.
// - Script responses are not automatically executed.

const fs = require('fs');
const path = require('path');
const { JSDOM } = require('jsdom');

describe('jquery.form.js delta tests - secure JSON and script handling', () => {
  let window;
  let $;

  beforeEach(() => {
    // Create a fresh DOM and window for each test
    const dom = new JSDOM(`<!doctype html><html><head></head><body>
      <meta name="csrf-token" content="dummy-token">
      <meta name="csrf-param" content="authenticity_token">
      <form id="testForm" action="/test" method="post">
        <input type="text" name="field1" value="value1"/>
      </form>
    </body></html>`, { url: 'http://localhost' });

    window = dom.window;
    // Load jQuery into the JSDOM window
    const jqueryFactory = require('jquery');
    $ = jqueryFactory(window);

    // Expose global variables the plugin expects
    window.jQuery = $;
    window.$ = $;
    global.window = window;
    global.document = window.document;
    global.jQuery = $;
    global.$ = $;

    // Load the fixed jquery.form.js plugin into this environment
    const pluginPath = path.resolve(
      __dirname,
      '../../../main/resources/webgoat/static/js/jquery_form/jquery.form.js'
    );
    const pluginSource = fs.readFileSync(pluginPath, 'utf8');
    // Execute plugin source in the JSDOM window context
    const scriptEl = window.document.createElement('script');
    scriptEl.textContent = pluginSource;
    window.document.head.appendChild(scriptEl);
  });

  afterEach(() => {
    // Cleanup globals to avoid cross-test interference
    delete global.window;
    delete global.document;
    delete global.jQuery;
    delete global.$;
  });

  test('does not use globalEval to auto-execute script responses', (done) => {
    // Arrange
    const form = $('#testForm');

    // Spy on globalEval - in the fixed version it should not be invoked by the plugin
    const originalGlobalEval = $.globalEval || function () {};
    const globalEvalSpy = jest.fn(originalGlobalEval);
    $.globalEval = globalEvalSpy;

    // Mock $.ajax to invoke the success callback the way jquery.form expects.
    // We simulate a "script" response type and ensure it is *not* executed.
    const ajaxSpy = jest.spyOn($, 'ajax').mockImplementation((options) => {
      // Simulate a script-like response that would have been executed previously
      const fakeXhr = {
        getResponseHeader: () => 'application/javascript',
        responseText: 'window.__scriptExecutedByPlugin = true;'
      };
      // Call the success handler directly
      if (options && typeof options.success === 'function') {
        options.success(fakeXhr.responseText, 'success', fakeXhr);
      }
      // Simulate completion
      if (options && typeof options.complete === 'function') {
        options.complete(fakeXhr, 'success');
      }
      return { then: () => {} };
    });

    // Act
    form.ajaxSubmit({
      dataType: 'script',
      success: () => {
        // Assert
        expect(globalEvalSpy).not.toHaveBeenCalled();
        expect(window.__scriptExecutedByPlugin).toBeUndefined();
        ajaxSpy.mockRestore();
        $.globalEval = originalGlobalEval;
        done();
      }
    });
  });

  test('uses safe JSON parser and does not fall back to eval', (done) => {
    // Arrange
    const form = $('#testForm');

    // Ensure parseJSON and JSON.parse are available to avoid error path
    // and confirm that eval is not used.
    const parseJsonSpy = jest.spyOn($, 'parseJSON');
    const evalSpy = jest.spyOn(window, 'eval').mockImplementation(() => {
      throw new Error('eval should not be called by jquery.form');
    });

    const ajaxSpy = jest.spyOn($, 'ajax').mockImplementation((options) => {
      const payload = '{"foo":"bar"}';
      const fakeXhr = {
        getResponseHeader: () => 'application/json',
        responseText: payload
      };
      if (options && typeof options.success === 'function') {
        options.success(payload, 'success', fakeXhr);
      }
      if (options && typeof options.complete === 'function') {
        options.complete(fakeXhr, 'success');
      }
      return { then: () => {} };
    });

    // Act
    form.ajaxSubmit({
      dataType: 'json',
      success: (data) => {
        // Assert: data should be a parsed object, and eval must not have been used
        expect(data).toEqual({ foo: 'bar' });
        expect(parseJsonSpy).toHaveBeenCalled();
        expect(evalSpy).not.toHaveBeenCalled();

        ajaxSpy.mockRestore();
        parseJsonSpy.mockRestore();
        evalSpy.mockRestore();
        done();
      }
    });
  });
});
