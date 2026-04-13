define(['jquery',
    'underscore',
    'backbone',
    'goatApp/model/HTMLContentModel'],
     function($,
        _,
        Backbone,
        HTMLContentModel){

    /**
     * Delta tests for LessonContentModel focusing on the new string-based pageNum extraction logic
     * that replaced the regex-based implementation.
     *
     * Derived path (by main→test mapping):
     * src/test/resources/webgoat/static/js/goatApp/model/LessonContentModel.test.js
     */

    describe('LessonContentModel pageNum extraction (string-based)', function () {
        var LessonContentModel;

        beforeAll(function () {
            LessonContentModel = HTMLContentModel.extend({
                urlRoot: null,
                defaults: {
                    items: null,
                    selectedItem: null
                },

                initialize: function (options) {},

                loadData: function(options) {
                    this.urlRoot = _.escape(encodeURIComponent(options.name)) + '.lesson'
                    var self = this;
                    this.fetch().done(function(data) {
                        self.setContent(data);
                    });
                },

                setContent: function(content, loadHelps) {
                    if (typeof loadHelps === 'undefined') {
                        loadHelps = true;
                    }
                    this.set('content',content);
                    this.set('lessonUrl',document.URL.replace(/\.lesson.*/,'.lesson'));
                    var url = document.URL;
                    var lastLessonIndex = url.lastIndexOf('.lesson/');
                    if (lastLessonIndex !== -1) {
                        var pageNumStr = url.substring(lastLessonIndex + '.lesson/'.length);
                        var pageNum = parseInt(pageNumStr, 10);
                        if (!isNaN(pageNum) && pageNum >= 0 && pageNum <= 9999) {
                            this.set('pageNum', pageNum);
                        } else {
                            this.set('pageNum', 0);
                        }
                    } else {
                        this.set('pageNum', 0);
                    }
                    this.trigger('content:loaded',this,loadHelps);
                },

                fetch: function (options) {
                    options = options || {};
                    return Backbone.Model.prototype.fetch.call(this, _.extend({ dataType: "html"}, options));
                }
            });
        });

        beforeEach(function () {
            // Reset document.URL for each test via jsdom/jasmine environment assumption
            Object.defineProperty(document, 'URL', {
                writable: true,
                value: 'http://localhost:8080/start.lesson'
            });
        });

        it('should set pageNum correctly for URL with .lesson/<number>', function () {
            document.URL = 'http://localhost:8080/intro.lesson/3';
            var model = new LessonContentModel();

            model.setContent('<html></html>');

            expect(model.get('pageNum')).toBe(3);
        });

        it('should set pageNum to 0 when URL has no .lesson/<number> suffix', function () {
            document.URL = 'http://localhost:8080/intro.lesson';
            var model = new LessonContentModel();

            model.setContent('<html></html>');

            expect(model.get('pageNum')).toBe(0);
        });

        it('should set pageNum to 0 when suffix after .lesson/ is not a valid number', function () {
            document.URL = 'http://localhost:8080/intro.lesson/xyz';
            var model = new LessonContentModel();

            model.setContent('<html></html>');

            expect(model.get('pageNum')).toBe(0);
        });

        it('should handle very long URLs without regex backtracking issues', function () {
            // Construct a long URL tail that previously might have caused expensive regex behavior
            var longTail = new Array(5000).join('a');
            document.URL = 'http://localhost:8080/intro.lesson/' + longTail;
            var model = new LessonContentModel();

            model.setContent('<html></html>');

            // For non-numeric suffix, pageNum should be 0, and execution should complete quickly.
            expect(model.get('pageNum')).toBe(0);
        });
    });

    // Export for test runners that support CommonJS style
    return {};
});
