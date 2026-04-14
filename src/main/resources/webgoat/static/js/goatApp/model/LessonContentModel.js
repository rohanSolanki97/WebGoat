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
            // Keep encoded lesson name for safety; do not change semantics
            this.urlRoot = _.escape(encodeURIComponent(options.name)) + '.lesson';
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
            this.set('lessonUrl',document.URL.replace(/\.lesson.*/,'\.lesson'));
            // FIX: use simpler, linear regex to avoid backtracking-heavy pattern
            var pageNumMatch = /\/(\d{1,4})$/.exec(document.URL);
            if (pageNumMatch) {
                this.set('pageNum', pageNumMatch[1]);
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
