const $ = require('jquery');
const Backbone = require('backbone');
const _ = require('underscore');

// Ensure Backbone uses jQuery
Backbone.$ = $;

// Require the updated module under test
// NOTE: In a real setup, require path may need adjustment to match bundler/AMD loader.
const LessonContentModelFactory = require('../../../../test/resources/webgoat/static/js/goatApp/model/LessonContentModel.js'); // TODO: Adjust if AMD is transformed to CommonJS

/**
 * Delta tests for LessonContentModel focusing on:
 * - Safer, bounded regular expressions used to derive lessonUrl and pageNum.
 *
 * We verify that:
 * - lessonUrl is correctly derived without relying on the old broad /.*...*/ patterns.
 * - pageNum is extracted only when present and limited to 1-4 digits.
 */

// Helper to simulate AMD-style return of the model
const LessonContentModel =
  LessonContentModelFactory && LessonContentModelFactory.__esModule
    ? LessonContentModelFactory.default || LessonContentModelFactory
    : LessonContentModelFactory;

describe('LessonContentModel regex hardening delta', () => {
  let originalLocation;

  beforeAll(() => {
    originalLocation = global.window && window.location;
    // Provide a minimal window.location mock if necessary
    if (!global.window) {
      global.window = {};
    }
    window.location = window.location || { href: '' };
    global.document = global.document || {};
  });

  afterAll(() => {
    if (originalLocation) {
      window.location = originalLocation;
    }
  });

  function withUrl(url, fn) {
    const oldUrl = document.URL;
    document.URL = url;
    try {
      fn();
    } finally {
      document.URL = oldUrl;
    }
  }

  test('setContent should derive lessonUrl from .lesson part of URL', () => {
    const model = new LessonContentModel();

    withUrl('http://localhost/WebGoat/lesson/SqlInjection.lesson/1?param=value#hash', () => {
      model.setContent('<html/>', false);
    });

    const lessonUrl = model.get('lessonUrl');
    expect(lessonUrl).toBe('http://localhost/WebGoat/lesson/SqlInjection.lesson');
  });

  test('setContent should set pageNum when URL ends with .lesson/<digits>', () => {
    const model = new LessonContentModel();

    withUrl('http://localhost/WebGoat/lesson/SqlInjection.lesson/42', () => {
      model.setContent('<html/>', false);
    });

    const pageNum = model.get('pageNum');
    expect(pageNum).toBe('42');
  });

  test('setContent should set pageNum to 0 when URL has no trailing page number', () => {
    const model = new LessonContentModel();

    withUrl('http://localhost/WebGoat/lesson/SqlInjection.lesson', () => {
      model.setContent('<html/>', false);
    });

    const pageNum = model.get('pageNum');
    expect(pageNum).toBe(0);
  });

  test('setContent should ignore non-matching long numeric segments beyond 4 digits', () => {
    const model = new LessonContentModel();

    withUrl('http://localhost/WebGoat/lesson/SqlInjection.lesson/12345', () => {
      model.setContent('<html/>', false);
    });

    const pageNum = model.get('pageNum');
    expect(pageNum).toBe(0);
  });
});
