// File: src/test/resources/webgoat/static/js/goatApp/model/LessonContentModel.test.js
// Delta tests for LessonContentModel focusing on the regex changes in setContent.

const path = require('path');
const fs = require('fs');
const { JSDOM } = require('jsdom');

describe('LessonContentModel regex hardening (delta tests)', () => {
  let window, document, $, Backbone, LessonContentModel;

  beforeEach(() => {
    const dom = new JSDOM(
      '<!doctype html><html><head></head><body></body></html>',
      { url: 'http://localhost/lessons/SomeLesson.lesson/3' }
    );
    window = dom.window;
    document = window.document;
    global.window = window;
    global.document = document;

    $ = require('jquery')(window);
    Backbone = require('backbone');
    Backbone.$ = $;

    const underscore = require('underscore');
    jest.mock('underscore', () => underscore);
    jest.mock('backbone', () => Backbone);

    // Load the AMD module by evaluating it; in a real setup this would be handled by a bundler.
    const modulePath = path.resolve(
      __dirname,
      '../../../main/resources/webgoat/static/js/goatApp/model/LessonContentModel.js'
    );
    const code = fs.readFileSync(modulePath, 'utf8');

    let exportedModel = null;
    const define = (deps, factory) => {
      const depInstances = deps.map((dep) => {
        if (dep === 'jquery') return $;
        if (dep === 'underscore') return underscore;
        if (dep === 'backbone') return Backbone;
        if (dep === 'goatApp/model/HTMLContentModel') {
          return Backbone.Model.extend({});
        }
        throw new Error(`Unknown dependency: ${dep}`);
      });
      exportedModel = factory.apply(null, depInstances);
    };
    // eslint-disable-next-line no-eval
    eval(code);
    LessonContentModel = exportedModel;
  });

  afterEach(() => {
    jest.resetModules();
    delete global.window;
    delete global.document;
  });

  test('setContent computes same lessonUrl and pageNum for typical URL', () => {
    const model = new LessonContentModel();
    document.defaultView.location.href = 'http://localhost/lessons/SomeLesson.lesson/42';

    model.setContent('<div>content</div>');

    expect(model.get('lessonUrl')).toBe(
      'http://localhost/lessons/SomeLesson.lesson'
    );
    expect(model.get('pageNum')).toBe('42');
  });

  test('setContent sets pageNum to 0 when URL has no page suffix', () => {
    const model = new LessonContentModel();
    document.defaultView.location.href = 'http://localhost/lessons/SomeLesson.lesson';

    model.setContent('<div>content</div>');

    expect(model.get('lessonUrl')).toBe(
      'http://localhost/lessons/SomeLesson.lesson'
    );
    expect(model.get('pageNum')).toBe(0);
  });

  test('setContent executes quickly for long non-matching URLs (ReDoS mitigation)', () => {
    const model = new LessonContentModel();
    const longUrl =
      'http://localhost/' + 'a'.repeat(10000) + '/no-lesson-here';
    document.defaultView.location.href = longUrl;

    const start = Date.now();
    model.setContent('<div>content</div>');
    const durationMs = Date.now() - start;

    expect(durationMs).toBeLessThan(1000);
    expect(model.get('pageNum')).toBe(0);
  });
});
