const { JSDOM } = require('jsdom');
const path = require('path');
const fs = require('fs');

describe('LessonContentModel delta tests', () => {
  let window;
  let document;
  let Backbone;
  let LessonContentModel;

  beforeEach(() => {
    const dom = new JSDOM('<!doctype html><html><head></head><body></body></html>', {
      url: 'http://localhost/Intro.lesson/3',
    });
    window = dom.window;
    document = window.document;

    const $ = require('jquery')(window);
    window.$ = $;
    window.jQuery = $;

    Backbone = require('backbone');
    Backbone.$ = $;
    window.Backbone = Backbone;

    // Minimal HTMLContentModel stub to satisfy dependency
    function HTMLContentModel() {}
    HTMLContentModel.extend = Backbone.Model.extend;
    window.HTMLContentModel = HTMLContentModel;

    // Load the fixed LessonContentModel.js as an AMD-style module
    const modulePath = path.resolve(
      __dirname,
      '../../../../main/resources/webgoat/static/js/goatApp/model/LessonContentModel.js'
    );
    const src = fs.readFileSync(modulePath, 'utf8');

    // Provide a minimal AMD define implementation for this test context
    window._ = {};
    window.define = function (deps, factory) {
      LessonContentModel = factory(window.$, window._, window.Backbone, window.HTMLContentModel);
    };

    // eslint-disable-next-line no-eval
    window.eval(src);
  });

  test('setContent_computesLessonUrlByTruncatingAtDotLesson', () => {
    // Arrange
    const model = new LessonContentModel();
    const content = '<html></html>';

    // Act
    model.setContent(content, true);
    const lessonUrl = model.get('lessonUrl');

    // Assert
    // For URL http://localhost/Intro.lesson/3, lessonUrl should be truncated to include .lesson
    expect(lessonUrl).toBe('http://localhost/Intro.lesson');
  });

  test('setContent_setsPageNumFromTrailingIntegerOrZeroWhenInvalid', () => {
    const model = new LessonContentModel();
    const content = '<html></html>';

    // Case 1: valid page number in URL
    window.document.defaultView.location.href = 'http://localhost/Intro.lesson/7';
    model.setContent(content, true);
    expect(model.get('pageNum')).toBe(7);

    // Case 2: invalid (non-numeric) suffix -> pageNum should be 0
    window.document.defaultView.location.href = 'http://localhost/Intro.lesson/not-a-number';
    model.setContent(content, true);
    expect(model.get('pageNum')).toBe(0);

    // Case 3: large number outside expected range -> defaults to 0
    window.document.defaultView.location.href = 'http://localhost/Intro.lesson/12345';
    model.setContent(content, true);
    expect(model.get('pageNum')).toBe(0);
  });
});
