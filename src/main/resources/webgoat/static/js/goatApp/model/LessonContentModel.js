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

            // Capture the current URL once and avoid unnecessary recomputation
            var currentUrl = String(document.URL || '');

            this.set('content', content);

            // Avoid overly complex regex; use simple, efficient parsing for lessonUrl
            // and ensure we do not accidentally process extremely long URLs with heavy regex.
            var lessonUrl = currentUrl;
            var lessonIndex = currentUrl.indexOf('.lesson');
            if (lessonIndex !== -1) {
                lessonUrl = currentUrl.substring(0, lessonIndex + '.lesson'.length);
            }
            this.set('lessonUrl', lessonUrl);

            // Use a lightweight and bounded regex for pageNum extraction to mitigate
            // inefficient regular expression complexity (ReDoS) concerns.
            var pageNum = 0;
            // Pattern: "<anything>.lesson/<1-4 digits>" at the end of the URL
            var match = currentUrl.match(/\.lesson\/(\d{1,4})$/);
            if (match && match[1]) {
                pageNum = parseInt(match[1], 10);
                if (!Number.isFinite(pageNum)) {
                    pageNum = 0;
                }
            }
            this.set('pageNum', pageNum);

            this.trigger('content:loaded', this, loadHelps);
        },

        fetch: function (options) {
            options = options || {};
            return Backbone.Model.prototype.fetch.call(this, _.extend({ dataType: "html"}, options));
        }
    });
});
