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

            // Use a simpler, linear-time-safe pattern for lessonUrl
            // Previous: document.URL.replace(/\.lesson.*/,'.lesson')
            // Now: use indexOf/slice to avoid regex backtracking.
            var currentUrl = document.URL || '';
            var lessonIndex = currentUrl.indexOf('.lesson');
            if (lessonIndex !== -1) {
                this.set('lessonUrl', currentUrl.slice(0, lessonIndex + '.lesson'.length));
            } else {
                this.set('lessonUrl', currentUrl);
            }

            // Replace complex regex with index-based parsing to avoid ReDoS patterns
            // Previous:
            // if (/.*\.lesson\/(\d{1,4})$/.test(document.URL)) {
            //     this.set('pageNum',document.URL.replace(/.*\.lesson\/(\d{1,4})$/,'$1'));
            // } else {
            //     this.set('pageNum',0);
            // }
            var pageNum = 0;
            var lessonPathIndex = currentUrl.indexOf('.lesson/');
            if (lessonPathIndex !== -1) {
                var pagePart = currentUrl.substring(lessonPathIndex + '.lesson/'.length);
                // only accept 1–4 digits
                var pageMatch = pagePart.match(/^\d{1,4}$/);
                if (pageMatch) {
                    pageNum = parseInt(pageMatch[0], 10);
                }
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
