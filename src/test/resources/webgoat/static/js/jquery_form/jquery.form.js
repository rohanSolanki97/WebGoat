const vm = require('vm');
const fs = require('fs');
const path = require('path');

describe('jquery.form.js delta security behavior', () => {
  test('parseJSON fallback uses JSON.parse and does not invoke eval', () => {
    const sandbox = {
      window: {},
      console: { log: () => {}, error: () => {} },
      jQuery: function () {},
      define: undefined,
    };
    sandbox.window = sandbox;
    sandbox.jQuery.fn = { prop: function () {} };

    sandbox.jQuery.parseJSON = undefined;

    let evalCalled = false;
    sandbox.window.eval = () => {
      evalCalled = true;
    };

    const pluginPath = path.join(
      __dirname,
      '..',
      '..',
      '..',
      'main',
      'resources',
      'webgoat',
      'static',
      'js',
      'jquery_form',
      'jquery.form.js'
    );
    const source = fs.readFileSync(pluginPath, 'utf8');
    vm.runInNewContext(source, sandbox);

    const harnessSource = `
      (function($) {
        $.fn.__testParseJson = function(jsonString) {
          if (typeof $.parseJSON === 'function') {
            return $.parseJSON(jsonString);
          }
          return JSON.parse(jsonString);
        };
      })(jQuery);
    `;
    vm.runInNewContext(harnessSource, sandbox);

    const json = '{"secure":true,"value":42}';
    const result = sandbox.jQuery.fn.__testParseJson(json);

    expect(result).toEqual({ secure: true, value: 42 });
    expect(evalCalled).toBe(false);
  });
});
