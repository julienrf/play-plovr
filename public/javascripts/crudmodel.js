goog.provide('play.modules.plovr.CrudModel');

goog.require('goog.net.XhrIo');
goog.require('goog.events');
goog.require('goog.Uri.QueryData');

play.modules.plovr.CrudModel = function(className, options) {
  this.paramName = className.substring(0, 1).toLowerCase() + className.substring(1);

  // The routes that will be used for CRUD operations
  this.routes = (options && options.routes) || routes[className + 's'];

  // Model properties
  this.attr = this.attr || {};
  if (options.data !== undefined) {
    this.mixin(options.data)
  }
};

play.modules.plovr.CrudModel.prototype = {
  // Save a model (performs a 'create' if the model does not have an id, otherwise performs an 'update')
  save: function(opt_callback, opt_target) {
    // URI-encode model properties
    var data = new goog.Uri.QueryData();
    for (var key in this.attr) {
      data.add(this.paramName + '.' + key, this.attr[key]);
    }

    // Choose the action to perform
    var route = this.attr.id == undefined ? this.routes.create() : this.routes.update(this.attr.id);
    
    this.ajax(route, this.fetchCallback(opt_callback, opt_target), data.toString());
  },
  delete: function() {
    if (this.attr.id) {
      this.ajax(this.routes.delete(this.attr.id));
    }
  },
  // Read the state of a model from the server and updates its properties accordingly
  fetch: function(opt_callback, opt_target) {
    if (this.attr.id !== undefined) {
      this.ajax(this.routes.read(this.attr.id), this.fetchCallback(opt_callback, opt_target));
    }
  },
  fetchCallback: function(opt_callback, opt_target) {
    return function(e) {
      this.mixin(e.target.getResponseJson());
      if (opt_callback) {
        var target = opt_target || this;
        goog.bind(opt_callback, target)(this);
      }
    };
  },
  // Update model properties from a given hash of values
  mixin: function(data) {
    goog.mixin(this.attr, data);
  },
  /**
   * Asynchronously call a given route
   * @param route Route to call (must have an url and a method properties)
   * @param opt_callback (optional) Function to call when we receive the response
   * @param opt_data (optional) Uri-encoded data to add to the ajax call
   */
  ajax: function(route, opt_callback, opt_data) {

    var xhr = new goog.net.XhrIo();
    if (opt_callback != null) {
      goog.events.listen(xhr, goog.net.EventType.COMPLETE, opt_callback, false, this);
    }

    var data = opt_data !== undefined ? opt_data : null;

    var headers = {
      'Accept': 'application/json',
      'X-Requested-With': 'XMLHttpRequest'
    };

    if (route.method == 'PUT') { // FIXME Why doesn't it work with regular PUT verb?
      route.method = 'POST';
      headers['X-HTTP-Method-Override'] = 'PUT';
    }

    xhr.send(route.url, route.method, data, headers);
  }
};
