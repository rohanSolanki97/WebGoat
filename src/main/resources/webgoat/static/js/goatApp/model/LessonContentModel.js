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
            this.set('lessonUrl',document.URL.replace(/\.lesson.*/,'.lesson'));
            // Optimized logic to extract pageNum without potentially inefficient regex
            var url = document.URL;
            var lastLessonIndex = url.lastIndexOf('.lesson/');
            if (lastLessonIndex !== -1) {
                var pageNumStr = url.substring(lastLessonIndex + '.lesson/'.length);
                var pageNum = parseInt(pageNumStr, 10);
                if (!isNaN(pageNum) && pageNum >= 0 && pageNum <= 9999) { // Assuming 1 to 4 digits
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
