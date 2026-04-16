define(['jquery',
    'underscore',
    'backbone',
    'goatApp/model/HTMLContentModel'],
     function($,
        _,
        Backbone,
        HTMLContentModel){

    return HTMLContentModel.extend({
        urlRoot:null,
        defaults: {
            items:null,
            selectedItem:null
        },

        initialize: function (options) {

        },

        loadData: function(options) {
            // Avoid double-encoding and unnecessary escaping that can lead to
            // inefficient regex handling and ambiguous URLs.
            // We treat options.name as a simple path segment and encode once.
            var name = options && typeof options.name === 'string' ? options.name : '';
            // Basic validation: allow only common safe characters in lesson names
            // to mitigate malformed input that could interact badly with regexes.
            var safeName = name.replace(/[^a-zA-Z0-9_\-./]/g, '');
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
            this.set('content',content);
            this.set('lessonUrl',document.URL.replace(/\.lesson.*/,'.lesson'));
            var pageMatch = document.URL.match(/\.lesson\/(\d{1,4})$/);
            if (pageMatch) {
                this.set('pageNum', pageMatch[1]);
            } else {
                this.set('pageNum',0);
            }
            this.trigger('content:loaded',this,loadHelps);
        },

        fetch: function (options) {
            options = options || {};
            return Backbone.Model.prototype.fetch.call(this, _.extend({ dataType: "html"}, options));
        }
    });
});
