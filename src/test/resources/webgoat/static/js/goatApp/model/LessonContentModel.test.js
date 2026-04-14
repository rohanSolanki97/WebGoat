// File: src/test/resources/webgoat/static/js/goatApp/model/LessonContentModel.test.js
// Delta tests for LessonContentModel focusing on changed regex behavior in setContent:
// - validate that pageNum extraction from URL uses the new linear regex logic.

define(['goatApp/model/LessonContentModel'], function (LessonContentModel) {
  describe('LessonContentModel setContent delta tests', function () {
    let originalUrl;

    beforeEach(function () {
      originalUrl = window.document.URL;
    });

    afterEach(function () {
      // Restore original URL after each test
      Object.defineProperty(window.document, 'URL', {
        configurable: true,
        writable: true,
        value: originalUrl
      });
    });

    function setDocumentUrl(url) {
      Object.defineProperty(window.document, 'URL', {
        configurable: true,
        writable: true,
        value: url
      });
    }

    it('extracts trailing numeric page number from URL using new regex', function () {
      // Arrange
      setDocumentUrl('http://localhost/WebGoat/lesson/1234');
      var model = new LessonContentModel();

      // Act: triggers setContent, which uses the new /\/(\d{1,4})$/ regex
      model.setContent('<html></html>', true);

      // Assert
      expect(model.get('pageNum')).toBe('1234');
    });

    it('sets pageNum to 0 when URL has no trailing digits', function () {
      // Arrange
      setDocumentUrl('http://localhost/WebGoat/lesson');
      var model = new LessonContentModel();

      // Act
      model.setContent('<html></html>', true);

      // Assert
      expect(model.get('pageNum')).toBe(0);
    });
  });
});
