/**
 * Delta Jest tests for LessonContentModel.js focusing on the changed behavior:
 * - Regular expressions used to derive lessonUrl and pageNum were simplified and made safer.
 *
 * These tests verify that:
 * - lessonUrl is correctly normalized to end with ".lesson" regardless of trailing segments.
 * - pageNum is correctly parsed as an integer from URLs ending with ".lesson/<digits>".
 *
 * Test file path (derived from main path):
 * src/test/resources/webgoat/static/js/goatApp/model/LessonContentModel.test.js
 */

const path = require('path');
const { JSDOM } = require('jsdom');

function loadLessonContentModel(url) {
  const dom = new JSDOM('<html><head></head><body></body></html>', { url });
  global.window = dom.window;
  global.document = dom.window.document;
  global.navigator = dom.window.navigator;

  const $ = require('jquery')(dom.window);
  const _ = require('underscore');
  const Backbone = require('backbone');

  global.$ = $;
  global.jQuery = $;
  global._ = _;
  Backbone.$ = $;

  const htmlContentModelPath = path.resolve(
    __dirname,
    '../../../../main/resources/webgoat/static/js/goatApp/model/LessonContentModel.js'
  );

  // The AMD define wrapper expects a loader; here we simulate it by requiring directly
  // and invoking the factory with our dependencies.
  // eslint-disable-next-line global-require, import/no-dynamic-require
  const factory = require(htmlContentModelPath);
  const LessonContentModel = factory($, _, Backbone, Backbone.Model);

  return { LessonContentModel, Backbone, $ };
}

describe('LessonContentModel delta tests for regex-based URL and page parsing', () => {
  afterEach(() => {
    delete global.window;
    delete global.document;
    delete global.navigator;
    delete global.$;
    delete global.jQuery;
    delete global._;
    jest.resetModules();
  });

  test('setContent normalizes lessonUrl to end with .lesson', () => {
    const url = 'http://localhost/SomeLesson.lesson/123';
    const { LessonContentModel } = loadLessonContentModel(url);

    const model = new LessonContentModel();
    model.setContent('<h1>content</h1>', true);

    const lessonUrl = model.get('lessonUrl');
    expect(lessonUrl.endsWith('.lesson')).toBe(true);
    expect(lessonUrl).toBe('http://localhost/SomeLesson.lesson');
  });

  test('setContent extracts pageNum as integer when URL ends with .lesson/<digits>', () => {
    const url = 'http://localhost/Another.lesson/42';
    const { LessonContentModel } = loadLessonContentModel(url);

    const model = new LessonContentModel();
    model.setContent('<h1>content</h1>', true);

    expect(model.get('pageNum')).toBe(42);
  });

  test('setContent sets pageNum to 0 when URL does not contain page suffix', () => {
    const url = 'http://localhost/Another.lesson';
    const { LessonContentModel } = loadLessonContentModel(url);

    const model = new LessonContentModel();
    model.setContent('<h1>content</h1>', true);

    expect(model.get('pageNum')).toBe(0);
  });
});
