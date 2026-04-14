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

            // Safer URL handling:
            // - Use a bounded regex to extract the `.lesson` base
            // - Avoid overly greedy patterns that could exhibit inefficient backtracking
            var url = String(document.URL || '');
            var lessonUrlMatch = url.match(/^(.*?\.lesson)/);
            if (lessonUrlMatch && lessonUrlMatch[1]) {
                this.set('lessonUrl', lessonUrlMatch[1]);
            } else {
                this.set('lessonUrl', url.replace(/\.lesson.*/,'.lesson'));
            }

            // Extract pageNum using a bounded, linear-time regex and a single match
            var pageNum = 0;
            var pageMatch = url.match(/\.lesson\/(\d{1,4})$/);
            if (pageMatch && pageMatch[1]) {
                pageNum = parseInt(pageMatch[1], 10) || 0;
            }
            this.set('pageNum', pageNum);

            this.trigger('content:loaded',this,loadHelps);
        },

        fetch: function (options) {
            options = options || {};
            return Backbone.Model.prototype.fetch.call(this, _.extend({ dataType: "html"}, options));
        }
    });
});
