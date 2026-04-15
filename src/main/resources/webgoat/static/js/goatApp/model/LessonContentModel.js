define([
    'jquery',
    'underscore',
    'backbone',
    'goatApp/model/HTMLContentModel'
], function ($, _, Backbone, HTMLContentModel) {

    return HTMLContentModel.extend({
        urlRoot: null,
        defaults: {
            items: null,
            selectedItem: null
        },

        initialize: function (options) {

        },

        loadData: function (options) {
            // Keep original behavior but ensure name is encoded safely
            this.urlRoot = _.escape(encodeURIComponent(options.name)) + '.lesson';
            var self = this;
            this.fetch().done(function (data) {
                self.setContent(data);
            });
        },

        setContent: function (content, loadHelps) {
            if (typeof loadHelps === 'undefined') {
                loadHelps = true;
            }
            this.set('content', content);

            // Safer, non-backtracking URL manipulation using indexOf / substring
            var currentUrl = document.URL;
            var lessonIndex = currentUrl.indexOf('.lesson');
            if (lessonIndex !== -1) {
                this.set('lessonUrl', currentUrl.substring(0, lessonIndex + '.lesson'.length));
            } else {
                this.set('lessonUrl', currentUrl);
            }

            // Extract pageNum with simple arithmetic instead of regex backtracking
            var lastSlashIndex = currentUrl.lastIndexOf('/');
            var pagePart = currentUrl.substring(lastSlashIndex + 1);
            var pageNum = parseInt(pagePart, 10);
            if (!isNaN(pageNum) && pageNum >= 0 && pageNum <= 9999) {
                this.set('pageNum', pageNum);
            } else {
                this.set('pageNum', 0);
            }

            this.trigger('content:loaded', this, loadHelps);
        },

        fetch: function (options) {
            options = options || {};
            return Backbone.Model.prototype.fetch.call(
                this,
                _.extend({ dataType: 'html' }, options)
            );
        }
    });
});
