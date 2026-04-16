// Intended test file path (derived from main path by replacing /main/ with /test/):
// src/test/resources/webgoat/static/js/goatApp/model/LessonContentModelTest.js

// NOTE:
// - This delta test suite focuses only on the updated URL parsing logic in setContent()
//   of LessonContentModel.
// - It assumes that the AMD module has been bundled/made available in the Jest environment
//   as a CommonJS module. The exact require path depends on the test runner/bundler config.
// - If the module is not directly require-able in your environment, adjust the require()
//   path below accordingly.

// TODO: Adjust this require path to match your Jest/module bundler configuration.
const LessonContentModel = require('../../../../../main/resources/webgoat/static/js/goatApp/model/LessonContentModel.js');

describe('LessonContentModel URL handling (delta tests)', () => {
  let originalDocument;

  beforeAll(() => {
    originalDocument = global.document;
  });

  afterAll(() => {
    global.document = originalDocument;
  });

  function createModelWithUrl(url) {
    global.document = { URL: url };
    // The AMD module extends a base model; here we instantiate the exported constructor.
    return new LessonContentModel();
  }

  test('derives lessonUrl and pageNum when URL has a numeric page segment', () => {
    const model = createModelWithUrl('http://example.com/SomeLesson.lesson/12');

    model.setContent('<html></html>');

    expect(model.get('lessonUrl')).toBe('http://example.com/SomeLesson.lesson');
    expect(model.get('pageNum')).toBe(12);
  });

  test('sets lessonUrl and pageNum=0 when no page number is present', () => {
    const model = createModelWithUrl('http://example.com/Another.lesson');

    model.setContent('<html></html>');

    expect(model.get('lessonUrl')).toBe('http://example.com/Another.lesson');
    expect(model.get('pageNum')).toBe(0);
  });

  test('handles URLs with long query strings without altering lessonUrl or pageNum', () => {
    const longQuery = 'x'.repeat(2000);
    const model = createModelWithUrl(
      `http://example.com/Complex.lesson/3?param=${longQuery}`
    );

    model.setContent('<html></html>');

    expect(model.get('lessonUrl')).toBe('http://example.com/Complex.lesson');
    expect(model.get('pageNum')).toBe(3);
  });
});
