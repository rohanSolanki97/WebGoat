const $ = require('jquery');
require('../../../../../main/resources/webgoat/static/js/jquery_form/jquery.form.js'); // load plugin

describe('jquery.form security hardening - callback validation', () => {
  test('ajaxSubmit should throw when success callback is provided as a string', () => {
    const form = $('<form></form>');

    expect(() =>
      form.ajaxSubmit({
        success: 'doSomethingDangerous'
      })
    ).toThrow(/must be a function, not a string/i);
  });

  test('ajaxForm should throw when beforeSubmit callback is provided as a string', () => {
    const form = $('<form></form>');

    expect(() =>
      form.ajaxForm({
        beforeSubmit: 'runBefore'
      })
    ).toThrow(/must be a function, not a string/i);
  });

  test('ajaxSubmit should accept function callbacks without throwing', () => {
    const form = $('<form></form>');
    const handler = jest.fn();

    expect(() =>
      form.ajaxSubmit({
        success: handler,
        error: handler,
        complete: handler,
        beforeSubmit: handler
      })
    ).not.toThrow();
  });

  test('ajaxForm should accept function callbacks without throwing', () => {
    const form = $('<form></form>');
    const handler = jest.fn();

    expect(() =>
      form.ajaxForm({
        success: handler
      })
    ).not.toThrow();
  });
});
