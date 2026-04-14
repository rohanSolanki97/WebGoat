define(
  ['jquery', 'underscore', 'backbone', 'goatApp/model/HTMLContentModel'],
  function ($, _, Backbone, HTMLContentModel) {
    return HTMLContentModel.extend({
      urlRoot: null,
      defaults: {
        items: null,
        selectedItem: null
      },

      initialize: function (options) {},

      loadData: function (options) {
        this.urlRoot = _.escape(encodeURIComponent(options.name)) + '.lesson';
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

        // Normalize URL string once for safe reuse
        var currentUrl = String(document.URL || '');

        // More efficient, precompiled regexes with no catastrophic backtracking risk
        // and explicit length guard to avoid ReDoS on overly long URLs.
        var lessonUrlRegex = /\.lesson.*/;
        var pageNumRegex = /.*\.lesson\/(\d{1,4})$/;

        // Guard against extremely long URLs before applying regex
        if (currentUrl.length > 2048) {
          // Fallback: do not process excessively long URLs
          this.set('lessonUrl', '');
          this.set('pageNum', 0);
        } else {
          this.set('lessonUrl', currentUrl.replace(lessonUrlRegex, '.lesson'));

          var pageMatch = pageNumRegex.exec(currentUrl);
          if (pageMatch && pageMatch[1]) {
            this.set('pageNum', pageMatch[1]);
          } else {
            this.set('pageNum', 0);
          }
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
  }
);
