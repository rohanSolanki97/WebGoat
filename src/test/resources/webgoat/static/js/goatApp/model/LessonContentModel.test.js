// Delta tests for LessonContentModel.js focusing on:
// - Sanitization of options.name in loadData (no longer double-encoding / unsafe characters).
// - Simplified and efficient regex handling in setContent for pageNum extraction.

const Backbone = require('backbone');
const _ = require('underscore');

// Assume the AMD module has been built or exposed in test env as a CommonJS module
const LessonContentModel = require('../../../../../main/resources/webgoat/static/js/goatApp/model/LessonContentModel.js'); // TODO: Adjust require path if build differs

describe('LessonContentModel delta tests', () => {
  test('loadData should sanitize name and set urlRoot with encoded safeName', () => {
    const model = new LessonContentModel();
    const fetchSpy = jest.spyOn(model, 'fetch').mockImplementation(function (options) {
      const d = {
        done: (cb) => {
          cb('<html>content</html>');
          return d;
        }
      };
      return d;
    });

    model.loadData({ name: 'lesson../evil?<>&' });

    // The unsafe characters should be stripped before encoding.
    // safeName regex in code: /[^a-zA-Z0-9_\-./]/g
    // So expected safeName here is 'lesson../evil'.
    const expectedSafeName = 'lesson../evil';
    expect(model.urlRoot).toBe(encodeURIComponent(expectedSafeName) + '.lesson');

    fetchSpy.mockRestore();
  });

  test('setContent should extract pageNum from URL using efficient regex', () => {
    const model = new LessonContentModel();

    const originalUrl = global.document && global.document.URL;
    Object.defineProperty(global.document, 'URL', {
      value: 'http://example.com/Some.lesson/123',
      configurable: true
    });

    model.setContent('<html>content</html>');

    expect(model.get('lessonUrl')).toBe('http://example.com/Some.lesson');
    expect(model.get('pageNum')).toBe('123');

    // Restore URL if it existed
    if (originalUrl !== undefined) {
      Object.defineProperty(global.document, 'URL', {
        value: originalUrl,
        configurable: true
      });
    }
  });

  test('setContent should default pageNum to 0 when URL does not match pattern', () => {
    const model = new LessonContentModel();

    const originalUrl = global.document && global.document.URL;
    Object.defineProperty(global.document, 'URL', {
      value: 'http://example.com/Some.lesson',
      configurable: true
    });

    model.setContent('<html>content</html>');

    expect(model.get('pageNum')).toBe(0);

    if (originalUrl !== undefined) {
      Object.defineProperty(global.document, 'URL', {
        value: originalUrl,
        configurable: true
      });
    }
  });
});
