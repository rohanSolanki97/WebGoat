const fs = require('fs');
const path = require('path');

describe('LessonContentModel delta behavior - pageNum extraction', () => {
  let originalDocument;

  beforeAll(() => {
    originalDocument = global.document;
  });

  afterAll(() => {
    global.document = originalDocument;
  });

  function createModelInstance() {
    // Minimal Backbone/HTMLContentModel mock to emulate the extended model
    const attributes = {};
    const model = {
      attributes,
      set(key, value) {
        this.attributes[key] = value;
      },
      get(key) {
        return this.attributes[key];
      },
      trigger() {
        // no-op for this delta test
      }
    };

    // Load the real file content and extract the setContent function body via eval in a scoped wrapper.
    const filePath = path.resolve(
      __dirname,
      '../../../../main/resources/webgoat/static/js/goatApp/model/LessonContentModel.js'
    );
    const src = fs.readFileSync(filePath, 'utf8');

    // Simple AMD wrapper shim to get at the returned object definition
    let exportedFactory = null;
    const define = function (deps, factory) {
      exportedFactory = factory(
        {}, // $
        { escape: v => v }, // _
        { Model: { prototype: {} } }, // Backbone (minimal)
        function HTMLContentModel() {} // HTMLContentModel
      );
    };

    // Execute the module in this test scope
    // eslint-disable-next-line no-eval
    eval(src);

    // exportedFactory is the extended prototype object with setContent, etc.
    model.setContent = exportedFactory.setContent.bind(model);

    return model;
  }

  test('setContent should set pageNum from URL with numeric suffix', () => {
    const model = createModelInstance();
    global.document = { URL: 'http://example.com/lesson/123.lesson/45' };

    model.setContent('<html/>', true);

    expect(model.get('pageNum')).toBe('45');
  });

  test('setContent should default pageNum to 0 when URL has no numeric suffix', () => {
    const model = createModelInstance();
    global.document = { URL: 'http://example.com/lesson/intro.lesson' };

    model.setContent('<html/>', true);

    expect(model.get('pageNum')).toBe(0);
  });
});
