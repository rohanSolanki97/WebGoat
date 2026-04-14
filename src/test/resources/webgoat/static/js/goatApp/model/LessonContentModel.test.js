// File: src/test/resources/webgoat/static/js/goatApp/model/LessonContentModel.test.js

// NOTE: This Jest test assumes that the AMD module is bundled in a way that allows requiring it
// via the same path relative to the test environment. Adjust the require path if your bundler
// exposes AMD modules differently.

const _ = require('underscore');
const Backbone = require('backbone');

// We mock the HTMLContentModel dependency to isolate behavior in LessonContentModel.
jest.mock('goatApp/model/HTMLContentModel', () => {
  const Base = Backbone.Model.extend({});
  return Base;
});

// Import the updated module under test via the same path used in production bundling.
// In a typical Jest + bundler setup, this would resolve to the transpiled version.
const LessonContentModel = require('webgoat/static/js/goatApp/model/LessonContentModel');

describe('LessonContentModel delta tests (regex and URL handling)', () => {
  let model;
  let originalUrl;

  beforeEach(() => {
    // Preserve and stub document.URL
    originalUrl = global.document && global.document.URL;
    if (!global.document) {
      global.document = {};
    }
    model = new LessonContentModel();
  });

  afterEach(() => {
    // Restore document.URL
    if (typeof originalUrl !== 'undefined') {
      global.document.URL = originalUrl;
    }
  });

  test('setContent should correctly derive lessonUrl and pageNum for normal URLs', () => {
    // Arrange: typical lesson URL with page number
    global.document.URL =
      'http://localhost:8080/WebGoat/Lesson.lesson/12?foo=bar';

    // Act
    model.setContent('<h1>Lesson</h1>');

    // Assert: new behavior still preserves semantics
    expect(model.get('lessonUrl')).toBe(
      'http://localhost:8080/WebGoat/Lesson.lesson'
    );
    expect(model.get('pageNum')).toBe('12');
  });

  test('setContent should default pageNum to 0 when URL does not match page pattern', () => {
    // Arrange: lesson URL without trailing page number
    global.document.URL = 'http://localhost:8080/WebGoat/Lesson.lesson';

    // Act
    model.setContent('<h1>Lesson</h1>');

    // Assert
    expect(model.get('lessonUrl')).toBe(
      'http://localhost:8080/WebGoat/Lesson.lesson'
    );
    expect(model.get('pageNum')).toBe(0);
  });

  test('setContent should guard against excessively long URLs (ReDoS mitigation)', () => {
    // Arrange: simulate an extremely long URL that would previously be passed directly to regex
    const veryLong = 'http://example.com/' + 'a'.repeat(5000) + '.lesson/99';
    global.document.URL = veryLong;

    // Act
    model.setContent('<h1>Lesson</h1>');

    // Assert: defensive behavior for long URLs
    expect(model.get('lessonUrl')).toBe('');
    expect(model.get('pageNum')).toBe(0);
  });
});
