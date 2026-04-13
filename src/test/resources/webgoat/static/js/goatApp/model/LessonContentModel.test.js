// src/test/resources/webgoat/static/js/goatApp/model/LessonContentModel.test.js
// Delta tests for LessonContentModel.js focusing on the updated setContent() logic:
// - Correct computation of lessonUrl and pageNum for various URLs
// - Indirectly validating that heavyweight regexes /.lesson.*/ and /.*\.lesson\/(\d{1,4})$/
//   are no longer used in the operational path.

const fs = require('fs');
const path = require('path');
const { JSDOM } = require('jsdom');

describe('LessonContentModel delta tests for setContent URL parsing', () => {
  let LessonContentModel;
  let model;
  let window;
  let document;

  beforeAll(() => {
    const dom = new JSDOM('<!doctype html><html><head></head><body></body></html>', {
      url: 'http://localhost'
    });
    window = dom.window;
    document = window.document;
    global.window = window;
    global.document = document;

    const jquery = require('jquery')(window);
    const _ = require('underscore');
    const Backbone = require('backbone');

    global.$ = jquery;
    global.jQuery = jquery;
    global._ = _;
    Backbone.$ = jquery;
    global.Backbone = Backbone;

    const htmlContentModelPath = path.resolve(
      __dirname,
      '../../../../main/resources/webgoat/static/js/goatApp/model/HTMLContentModel.js'
    );
    let HTMLContentModel;
    if (fs.existsSync(htmlContentModelPath)) {
      // eslint-disable-next-line global-require, import/no-dynamic-require
      HTMLContentModel = require(htmlContentModelPath);
    } else {
      HTMLContentModel = Backbone.Model.extend({});
    }
    global.define = function (deps, factory) {
      LessonContentModel = factory(jquery, _, Backbone, HTMLContentModel);
    };

    const lessonContentModelPath = path.resolve(
      __dirname,
      '../../../../main/resources/webgoat/static/js/goatApp/model/LessonContentModel.js'
    );
    // eslint-disable-next-line no-eval
    eval(fs.readFileSync(lessonContentModelPath, 'utf8'));
  });

  beforeEach(() => {
    model = new LessonContentModel();
  });

  test('setContent sets lessonUrl correctly for base .lesson URL with no page', () => {
    window.location.href = 'http://localhost/path/to/lesson1.lesson';
    model.setContent('<div>content</div>');

    expect(model.get('lessonUrl')).toBe('http://localhost/path/to/lesson1.lesson');
    expect(model.get('pageNum')).toBe(0);
  });

  test('setContent sets lessonUrl and pageNum for .lesson/3 URL', () => {
    window.location.href = 'http://localhost/lesson2.lesson/3';
    model.setContent('<div>content</div>');

    expect(model.get('lessonUrl')).toBe('http://localhost/lesson2.lesson');
    expect(model.get('pageNum')).toBe(3);
  });

  test('setContent handles URL without .lesson segment', () => {
    window.location.href = 'http://localhost/other/path';
    model.setContent('<div>content</div>');

    expect(model.get('lessonUrl')).toBe('http://localhost/other/path');
    expect(model.get('pageNum')).toBe(0);
  });

  test('setContent ignores malformed trailing parts after .lesson/', () => {
    window.location.href = 'http://localhost/lesson3.lesson/not-a-page';
    model.setContent('<div>content</div>');

    expect(model.get('lessonUrl')).toBe('http://localhost/lesson3.lesson');
    expect(model.get('pageNum')).toBe(0);
  });
});
