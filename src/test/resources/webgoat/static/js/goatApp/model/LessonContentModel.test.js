// File: src/test/resources/webgoat/static/js/goatApp/model/LessonContentModel.test.js
// Derived from source path by replacing '/main/' with '/test/':
//   src/main/resources/webgoat/static/js/goatApp/model/LessonContentModel.js
//   -> src/test/resources/webgoat/static/js/goatApp/model/LessonContentModel.test.js

// NOTE: This test is written assuming a CommonJS environment with Jest and that the AMD module
// is bundled or exposed in a way that allows requiring the model constructor. In a real project
// this might be wired differently via RequireJS or a bundler.
// TODO: Adjust the require path if your build exposes the model differently.

const $ = require('jquery');
const _ = require('underscore');
const Backbone = require('backbone');

// Minimal HTMLContentModel stub to satisfy the dependency in LessonContentModel.js
class HTMLContentModel extends Backbone.Model {}
// Shim define to allow the AMD module to register itself and return the extended model
global.define = function (deps, factory) {
  module.exports = factory($, _, Backbone, HTMLContentModel);
};

require('../../../../../main/resources/webgoat/static/js/goatApp/model/LessonContentModel.js');
const LessonContentModel = module.exports;

describe('LessonContentModel delta tests', () => {
  let model;

  beforeEach(() => {
    model = new LessonContentModel();
  });

  test('loadData should build urlRoot using encodeURIComponent only (no HTML escaping)', () => {
    const name = 'Lesson 1 & Intro';
    model.loadData({ name });

    const expected = encodeURIComponent(name) + '.lesson';
    expect(model.urlRoot).toBe(expected);
  });

  test('setContent should extract pageNum from URL using safe regex without catastrophic backtracking', () => {
    const originalLocation = global.document && global.document.URL;
    // Simulate a lesson URL with page number
    global.document = {
      URL: 'http://localhost:8080/WebGoat/lesson/SomeLesson.lesson/1234',
    };

    const content = '<div>dummy</div>';
    model.setContent(content);

    expect(model.get('pageNum')).toBe('1234');

    // Restore original document if it existed
    if (originalLocation) {
      global.document.URL = originalLocation;
    }
  });

  test('setContent should default pageNum to 0 when URL does not contain page number', () => {
    const originalLocation = global.document && global.document.URL;
    global.document = {
      URL: 'http://localhost:8080/WebGoat/lesson/SomeLesson.lesson',
    };

    const content = '<div>dummy</div>';
    model.setContent(content);

    expect(model.get('pageNum')).toBe(0);

    if (originalLocation) {
      global.document.URL = originalLocation;
    }
  });
});
