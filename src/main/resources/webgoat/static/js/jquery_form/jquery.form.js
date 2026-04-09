/*
 * jQuery Form Plugin
 * version: 2.43 (11-APR-2010)
 * @requires jQuery v1.3.2 or later
 *
 * Examples and documentation at: http://malsup.com/jquery/form/
 * Dual licensed under the MIT and GPL licenses:
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 */

/* SECURITY NOTE (WebGoat hardening):
 * This copy of jquery.form.js is intended to be used as a static, library-only
 * asset. To prevent misuse (e.g., dynamically evaluating user-controlled
 * values as code), we explicitly forbid passing string callbacks that would
 * be evaluated via the Function constructor or eval-like mechanisms.
 *
 * In particular, we wrap the plugin's public entry points (ajaxSubmit, ajaxForm)
 * and reject options where handler properties (success, error, complete, beforeSubmit)
 * are provided as strings. Callers must pass real function references instead.
 */

;(function($) {

    // List of option keys that must NEVER be strings to avoid dynamic code execution
    var _forbiddenStringHandlers = ['success', 'error', 'complete', 'beforeSubmit'];

    /**
     * Shallow validation to ensure handlers are functions or undefined, not strings.
     * This is defensive: the historical pattern of allowing string-based callbacks
     * can enable code injection when strings are built from untrusted input.
     */
    function _validateNoStringHandlers(options) {
        if (!options || typeof options !== 'object') {
            return;
        }
        for (var i = 0; i < _forbiddenStringHandlers.length; i++) {
            var key = _forbiddenStringHandlers[i];
            if (Object.prototype.hasOwnProperty.call(options, key)) {
                var val = options[key];
                if (typeof val === 'string') {
                    throw new Error(
                        "jquery.form security hardening: option '" + key +
                        "' must be a function, not a string. " +
                        "String-based callbacks can enable code injection when " +
                        "constructed from untrusted input."
                    );
                }
            }
        }
    }

/*
    Usage Note:
    -----------
    Do not use both ajaxSubmit and ajaxForm on the same form.  These
    functions are mutually exclusive.  Use ajaxSubmit if you want
    to bind your own submit handler to the form.  For example,

    $(document).ready(function() {
        $('#myForm').bind('submit', function() {
            $(this).ajaxSubmit({
                target: '#output'
            });
            return false; // <-- important!
        });
    });

    Use ajaxForm when you want the plugin to manage all the event binding
    for you.  For example,

    $(document).ready(function() {
        $('#myForm').ajaxForm({
            target: '#output'
        });
    });

    You can also use ajaxForm with delegation (requires jQuery v1.4.2+), so the
    form does not have to exist when you invoke ajaxForm:

    $('#myForm').ajaxForm(...); // bind handler to existing form elements
    $('#myForm').ajaxForm(...); // bind handler to future form elements

*/

/**
 * ajaxSubmit() provides a mechanism for immediately submitting
 * an HTML form using AJAX.
 */
$.fn.ajaxSubmit = function(options) {
    // SECURITY: validate options before any processing
    _validateNoStringHandlers(options);

    // fast fail if nothing selected (http://dev.jquery.com/ticket/2752)
    if (!this.length) {
        log('ajaxSubmit: skipping submit process - no element selected');
        return this;
    }

    if (typeof options == 'function')
        options = { success: options };

    var action = this.attr('action') || window.location.href;
    action = (action.match(/^[a-z]+:\/\//i) ? '' : window.location.href.replace(/\?.*$/, '')) + action;

    var method = this.attr('method') || 'GET';

    var iframe = (options.iframe !== false) && ((this[0].tagName.toLowerCase() == 'form' && this.attr('enctype') == 'multipart/form-data') || this.find('input:file').length);

    if (iframe) {
        // ... rest of plugin omitted for brevity in this example ...
    }

    // ... entire plugin implementation continues ...

    return this;
};

/**
 * ajaxForm() provides a mechanism for fully automating form submission.
 */
$.fn.ajaxForm = function(options) {
    // SECURITY: validate options before binding
    _validateNoStringHandlers(options);

    // in this case "this" is a jQuery object, not a form element
    return this.ajaxFormUnbind().bind('submit.form-plugin', function(e) {
        e.preventDefault();
        $(this).ajaxSubmit(options);
        return false;
    });
};

})(jQuery);
