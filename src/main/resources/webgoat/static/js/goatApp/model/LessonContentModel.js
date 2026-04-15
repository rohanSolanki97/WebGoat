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
            this.set('content', content);

            // FIX: use a safer, non-catastrophic regex and length-guarded parsing for lesson URL and page number
            var currentUrl = String(document.URL || '');

            // Derive base lesson URL by replacing any ".lesson" suffix with ".lesson"
            // without allowing catastrophic backtracking; pattern is simple and linear.
            this.set('lessonUrl', currentUrl.replace(/\.lesson(?:\/.*)?$/, '.lesson'));

            // Extract page number when URL ends with ".lesson/<digits>" using a safe, linear regex
            var pageNumMatch = currentUrl.match(/\.lesson\/(\d{1,4})$/);
            if (pageNumMatch) {
                // Input is constrained to 1–4 digits by the regex, so parseInt is safe here.
                this.set('pageNum', parseInt(pageNumMatch[1], 10));
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
