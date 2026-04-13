// NOTE: Assumes this test file is placed at
// src/test/resources/webgoat/static/js/jquery_form/jquery.form.test.js

const fs = require('fs');
const path = require('path');

describe('jquery.form.js delta security behavior', () => {
  const pluginPath = path.resolve(
    __dirname,
    '../../../main/resources/webgoat/static/js/jquery_form/jquery.form.js'
  );

  test('plugin file should not contain eval-based JSON parsing or globalEval', () => {
    const contents = fs.readFileSync(pluginPath, 'utf8');

    // Assert legacy insecure patterns are not present
    expect(contents).not.toMatch(/window\['eval'\]\s*\(/);
    expect(contents).not.toMatch(/eval\(/);
    expect(contents).not.toMatch(/\.globalEval\s*\(/);

    // Assert that JSON.parse is used for JSON handling
    expect(contents).toMatch(/JSON\.parse\s*\(/);
  });

  test('httpData path should not auto-execute script responses', () => {
    const contents = fs.readFileSync(pluginPath, 'utf8');

    // In the fixed version the script branch comment should mention that globalEval is disabled
    const httpDataSection = contents.split('var httpData = function')[1] || '';
    expect(httpDataSection).toContain(
      'do not automatically execute arbitrary script responses'
    );
    expect(httpDataSection).not.toContain('$.globalEval');
  });
});
