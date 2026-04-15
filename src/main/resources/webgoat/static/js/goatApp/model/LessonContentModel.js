define([
    'jquery',
    'underscore',
    'backbone',
    'goatApp/model/HTMLContentModel'
],
function ($, _, Backbone, HTMLContentModel) {

    return HTMLContentModel.extend({
        urlRoot: null,
        defaults: {
            items: null,
            selectedItem: null
        },

        initialize: function (options) {

        },

        loadData: function (options) {
            // NOTE:
            // This value (options.name) should already be controlled by application code
            // (lesson identifiers), but we still encode defensively before using it as part
            // of the URL to avoid injection into the path.
            var safeName = encodeURIComponent(String(options.name || ''));

            this.urlRoot = safeName + '.lesson';
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

            // Use a more constrained, linear-time-safe regex for extracting the lesson URL.
            // Previous pattern: document.URL.replace(/\.lesson.*/, '.lesson')
            // This new pattern avoids nested or ambiguous constructs while preserving semantics.
            var href = String(document.URL || '');
            var lessonUrlMatch = href.match(/^[^?#]*?\.lesson\b/);
            if (lessonUrlMatch) {
                this.set('lessonUrl', lessonUrlMatch[0]);
            } else {
                // Fallback: use current URL without query/hash if pattern does not match.
                this.set('lessonUrl', href.split(/[?#]/)[0]);
            }

            // Safer, explicit page number extraction with bounded repetition and anchors.
            var pageNumMatch = href.match(/\.lesson\/(\d{1,4})(?:[?#]|$)/);
            if (pageNumMatch) {
                this.set('pageNum', pageNumMatch[1]);
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
