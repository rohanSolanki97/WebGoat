// Intended test file path (derived from main path by replacing /main/ with /test/):
// src/test/resources/webgoat/static/js/jquery_form/jquery.formTest.js

// NOTE:
// - This suite is written as a delta test for the JSON parsing hardening in jquery.form.js.
// - It does not re-implement plugin internals; instead it exercises the observable behavior:
//   
//   
//   
// - It assumes that jquery.form.js has already been loaded into the test environment and
//   extended the global jQuery object as per the updated source file.

// describe('jquery.form JSON parsing hardening (delta tests)', () => {
//   beforeEach(() => {
//     // Ensure any previous side-effects are cleaned.
//     delete window.__jqueryFormScriptExecuted;
//   });

//   test('ajaxSubmit parses JSON responses using JSON.parse and does not call window.eval', (done) => {
//     // Arrange
//     const form = document.createElement('form');
//     // Minimal action attribute so plugin has a URL to use.
//     form.setAttribute('action', '/json-endpoint');
//     document.body.appendChild(form);

//     const $form = jQuery(form);

//     const jsonResponse = { foo: 'bar' };
//     const jsonText = JSON.stringify(jsonResponse);

//     // Spy on JSON.parse and window.eval
//     const jsonParseSpy = jest.spyOn(JSON, 'parse');
//     const evalSpy = jest.spyOn(window, 'eval');

//     // Stub $.ajax used by jquery.form to simulate JSON response
//     const ajaxSpy = jest.spyOn(jQuery, 'ajax').mockImplementation((options) => {
//       // Simulate a JSON response according to jQuery.ajax contract
//       if (options && typeof options.success === 'function') {
//         options.success(jsonResponse, 'success', {
//           responseText: jsonText,
//           getResponseHeader: () => 'application/json',
//         });
//       }
//       return {};
//     });

//     // Act
//     $form.ajaxSubmit({
//       dataType: 'json',
//       success: (data) => {
//         try {
//           // Assert
//           expect(data).toEqual(jsonResponse);
//           expect(jsonParseSpy).toHaveBeenCalled();
//           expect(evalSpy).not.toHaveBeenCalled();
//           done();
//         } finally {
//           // Cleanup spies and DOM
//           jsonParseSpy.mockRestore();
//           evalSpy.mockRestore();
//           ajaxSpy.mockRestore();
//           document.body.removeChild(form);
//         }
//       },
//     });
//   });

//   test('ajaxSubmit routes script responses through $.globalEval but not JSON.parse', (done) => {
//     // Arrange
//     const form = document.createElement('form');
//     form.setAttribute('action', '/script-endpoint');
//     document.body.appendChild(form);

//     const $form = jQuery(form);

//     const scriptBody = 'window.__jqueryFormScriptExecuted = true;';

//     const jsonParseSpy = jest.spyOn(JSON, 'parse');
//     const globalEvalSpy = jest.spyOn(jQuery, 'globalEval');

//     const ajaxSpy = jest.spyOn(jQuery, 'ajax').mockImplementation((options) => {
//       if (options && typeof options.success === 'function') {
//         options.success(undefined, 'success', {
//           responseText: scriptBody,
//           getResponseHeader: () => 'application/javascript',
//         });
//       }
//       return {};
//     });

//     // Act
//     $form.ajaxSubmit({
//       dataType: 'script',
//       success: () => {
//         try {
//           // Assert
//           expect(globalEvalSpy).toHaveBeenCalledWith(scriptBody);
//           expect(window.__jqueryFormScriptExecuted).toBe(true);
//           expect(jsonParseSpy).not.toHaveBeenCalled();
//           done();
//         } finally {
//           jsonParseSpy.mockRestore();
//           globalEvalSpy.mockRestore();
//           ajaxSpy.mockRestore();
//           document.body.removeChild(form);
//         }
//       },
//     });
//   });
// });
