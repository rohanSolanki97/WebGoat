// NOTE: This test file assumes a Jest environment where the jquery.form.js plugin
// is loaded into the test via a relative require from the resolved_file_path
// (src/main/resources/webgoat/static/js/jquery_form/jquery.form.js -> src/test/...).
// The core delta being tested is that script responses are no longer executed
// automatically via $.globalEval or similar.

const fs = require('fs');
const path = require('path');
const { JSDOM } = require('jsdom');

describe('jquery.form.js delta tests - prevent automatic script execution', () => {
  let window;
  let $;

  beforeEach(() => {
    // Setup a minimal DOM and jQuery instance for the plugin to attach to.
    const dom = new JSDOM('<!doctype html><html><head></head><body></body></html>', {
      url: 'http://localhost/'
    });
    window = dom.window;
    global.window = window;
    global.document = window.document;

    // Load jQuery into this environment.
    $ = require('jquery')(window);
    global.jQuery = $;

    // Stub $.ajax so that we can capture options and simulate a script response.
    jest.spyOn($, 'ajax').mockImplementation((opts) => {
      // Immediately invoke success with a "script" payload; plugin must not eval it.
      // In the original vulnerable version, httpData() would call $.globalEval on this.
      if (opts && typeof opts.success === 'function') {
        opts.success('window.__injected = true;', 'success', {
          getResponseHeader: (header) =>
            header.toLowerCase() === 'content-type' ? 'application/javascript' : ''
        });
      }
      return { done: () => {}, fail: () => {} };
    });

    // Ensure any prior plugin registration is cleared.
    delete $.fn.ajaxSubmit;

    // Load the fixed plugin source.
    const pluginPath = path.resolve(
      __dirname,
      '../../../main/resources/webgoat/static/js/jquery_form/jquery.form.js'
    );
    const pluginCode = fs.readFileSync(pluginPath, 'utf8');
    // Execute plugin code in this context; it will extend the existing jQuery.
    // eslint-disable-next-line no-eval
    eval(pluginCode);
  });

  afterEach(() => {
    jest.restoreAllMocks();
    delete global.window;
    delete global.document;
    delete global.jQuery;
    delete global.$;
    delete window.__injected;
  });

  test('ajaxSubmit does not execute returned script payload automatically', () => {
    const form = $('<form id="testForm"></form>');
    $('body').append(form);

    // Sanity check that plugin is loaded.
    expect(typeof $.fn.ajaxSubmit).toBe('function');

    // Call ajaxSubmit and let our $.ajax stub invoke success with script content.
    form.ajaxSubmit({
      type: 'POST',
      url: '/script-endpoint',
      dataType: 'script'
    });

    // In the vulnerable version, the script body would be executed and set window.__injected.
    // After the fix, this side effect must NOT occur.
    expect(window.__injected).toBeUndefined();
  });
});
