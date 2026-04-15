define(['jquery',
    'underscore',
    'backbone',
    'goatApp/model/HTMLContentModel'],
     function($,
        _,
        Backbone,
        HTMLContentModel){

    return HTMLContentModel.extend({
        urlRoot: null,
        defaults: {
            items: null,
            selectedItem: null
        },

        initialize: function (options) {

        },

        loadData: function(options) {
            // Safely derive lesson name for use in URL: sanitize and bound length
            var name = options && typeof options.name === 'string' ? options.name : '';
            var safeName = name.replace(/[^a-zA-Z0-9_\-]/g, '').slice(0, 64);
            this.urlRoot = encodeURIComponent(safeName) + '.lesson';
            var self = this;
            this.fetch().done(function(data) {
                self.setContent(data);
            });
        },

        setContent: function(content, loadHelps) {
            if (typeof loadHelps === 'undefined') {
                loadHelps = true;
            }
            this.set('content', content);

            var currentUrl = String(document.URL || '');
            this.set('lessonUrl', currentUrl.replace(/\.lesson.*/, '.lesson'));

            // Use a single bounded regex match to avoid unnecessary work and complexity
            var pageMatch = currentUrl.match(/\.lesson\/(\d{1,4})$/);
            if (pageMatch && pageMatch[1]) {
                this.set('pageNum', pageMatch[1]);
            } else {
                this.set('pageNum', 0);
            }

            this.trigger('content:loaded', this, loadHelps);
        },

        fetch: function (options) {
            options = options || {};
            return Backbone.Model.prototype.fetch.call(this, _.extend({ dataType: "html"}, options));
        }
    });
});
