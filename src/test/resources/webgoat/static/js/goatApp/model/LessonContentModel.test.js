// Resolved test path (derived from src/main/resources → src/test/resources):
// src/test/resources/webgoat/static/js/goatApp/model/LessonContentModel.test.js

const vm = require('vm');
const fs = require('fs');
const path = require('path');

describe('LessonContentModel delta tests – URL parsing and pageNum logic', () => {
  let sandbox;
  let LessonContentModelCtor;

  function loadModule() {
    sandbox = {
      console,
      document: { URL: '' }
    };

    const _ = {
      escape: (s) => s,
      extend: Object.assign
    };

    const Backbone = {
      Model: function () {
        this.attributes = {};
      }
    };

    Backbone.Model.prototype.set = function (key, value) {
      this.attributes[key] = value;
    };

    Backbone.Model.prototype.fetch = function () {
      return { done: (cb) => cb('<html></html>') };
    };

    const HTMLContentModel = Backbone.Model;

    HTMLContentModel.extend = function (props) {
      function Ctor() {
        Backbone.Model.call(this);
        if (typeof props.initialize === 'function') {
          props.initialize.apply(this, arguments);
        }
      }
      Ctor.prototype = Object.create(Backbone.Model.prototype);
      Object.assign(Ctor.prototype, props);
      return Ctor;
    };

    Backbone.Model.extend = HTMLContentModel.extend;

    sandbox.define = function (deps, factory) {
      LessonContentModelCtor = factory({}, _, Backbone, HTMLContentModel);
    };

    const modulePath = path.resolve(
      __dirname,
      '../../../../../main/resources/webgoat/static/js/goatApp/model/LessonContentModel.js'
    );
    const code = fs.readFileSync(modulePath, 'utf8');
    vm.runInNewContext(code, sandbox);
  }

  beforeEach(() => {
    loadModule();
  });

  test('setContent computes lessonUrl and pageNum when URL contains ".lesson/NNNN"', () => {
    const instance = new LessonContentModelCtor();
    sandbox.document.URL = 'http://example.com/SomeLesson.lesson/12';

    instance.setContent('<html></html>', true);

    expect(instance.attributes.lessonUrl).toBe(
      'http://example.com/SomeLesson.lesson'
    );
    expect(instance.attributes.pageNum).toBe(12);
  });

  test('setContent computes lessonUrl and pageNum when URL lacks ".lesson/NNNN"', () => {
    const instance = new LessonContentModelCtor();
    sandbox.document.URL = 'http://example.com/OtherLesson.lesson';

    instance.setContent('<html></html>', true);

    expect(instance.attributes.lessonUrl).toBe(
      'http://example.com/OtherLesson.lesson'
    );
    expect(instance.attributes.pageNum).toBe(0);
  });
});
