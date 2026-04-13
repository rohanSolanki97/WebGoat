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

            // Use a simple, linear replacement: find the first ".lesson" and replace the rest.
            // This avoids a potentially inefficient regex like /\.lesson.*/.
            var currentUrl = document.URL;
            var lessonIndex = currentUrl.indexOf('.lesson');
            if (lessonIndex !== -1) {
                this.set('lessonUrl', currentUrl.substring(0, lessonIndex + '.lesson'.length));
            } else {
                this.set('lessonUrl', currentUrl);
            }

            // Replace complex regex /.*\.lesson\/(\d{1,4})$/ with a safe substring/indexOf approach.
            // We only need to extract up to 4 digits after ".lesson/" at the end of the URL.
            var pageNum = 0;
            var lessonSegment = '.lesson/';
            var segmentIndex = currentUrl.indexOf(lessonSegment);
            if (segmentIndex !== -1) {
                var start = segmentIndex + lessonSegment.length;
                var suffix = currentUrl.substring(start);
                var match = suffix.match(/^(\d{1,4})$/);
                if (match) {
                    pageNum = parseInt(match[1], 10);
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
