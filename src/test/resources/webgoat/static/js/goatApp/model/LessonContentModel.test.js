// File path: src/test/resources/webgoat/static/js/goatApp/model/LessonContentModel.test.js
// Note: AMD module; we test exported behavior by requiring through a loader in Node.
// For simplicity, we assume the built artifact exposes the same API via a direct require.
// If a different bundling setup is used, adjust the require path accordingly.

const Backbone = require('backbone');
const _ = require('underscore');

// Minimal HTMLContentModel stub with Backbone.Model behavior
class HTMLContentModel extends Backbone.Model {}

// Require the updated module and inject its dependencies using the AMD factory pattern.
// In the actual build, this might already be bundled; here we emulate the AMD call.
const fs = require('fs');
const path = require('path');

const modulePath = path.resolve(
  __dirname,
  '../../../../../main/resources/webgoat/static/js/goatApp/model/LessonContentModel.js'
);
const moduleSource = fs.readFileSync(modulePath, 'utf8');

// Execute the AMD define wrapper in a sandbox to get the extended model
let LessonContentModelFactory;
const define = (deps, factory) => {
  LessonContentModelFactory = factory;
};
// eslint-disable-next-line no-new-func
new Function('define', moduleSource)(define);
const LessonContentModel = LessonContentModelFactory(
  require('jquery'),
  _,
  Backbone,
  HTMLContentModel
);

describe('LessonContentModel delta tests (sanitization and regex behavior)', () => {
  test('loadData should sanitize options.name and bound its length before using as urlRoot', () => {
    const model = new LessonContentModel();

    const longName = 'abc'.repeat(30); // length 90 > 64
    const maliciousName = longName + '../weird?name=<script>';
    model.loadData({ name: maliciousName });

    const urlRoot = model.urlRoot;

    // urlRoot must be encoded safeName + '.lesson'
    expect(urlRoot.endsWith('.lesson')).toBe(true);

    const encodedSafeName = urlRoot.replace(/\.lesson$/, '');
    const safeName = decodeURIComponent(encodedSafeName);

    // Only allowed characters should remain (alphanumeric, underscore, hyphen)
    expect(/^[a-zA-Z0-9_-]+$/.test(safeName)).toBe(true);

    // Length should be bounded to 64 characters
    expect(safeName.length).toBeLessThanOrEqual(64);

    // Ensure dangerous characters were removed
    expect(safeName).not.toContain('.');
    expect(safeName).not.toContain('/');
    expect(safeName).not.toContain('<');
    expect(safeName).not.toContain('>');
  });

  test('setContent should set pageNum from URL only when matching bounded pattern', () => {
    const model = new LessonContentModel();

    // Simulate a URL with a valid 1–4 digit page number
    const originalUrl = global.document && document.URL;
    global.document = { URL: 'http://example/lesson/intro.lesson/1234' };

    model.setContent('<html>content</html>');

    expect(model.get('pageNum')).toBe('1234');

    // Simulate a URL that does not match the bounded pattern (too many digits)
    global.document.URL = 'http://example/lesson/intro.lesson/123456';

    model.setContent('<html>content</html>');

    // Should fall back to 0 when pattern does not match
    expect(model.get('pageNum')).toBe(0);

    // Restore original URL if it existed
    if (originalUrl) {
      global.document.URL = originalUrl;
    }
  });
});
