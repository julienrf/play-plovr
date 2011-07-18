goog.provide('play.modules.plovr.CrudModel');

play.modules.plovr.CrudModel = function(className, options) {
  this.paramName = className.substring(0, 1).toLowerCase() + className.substring(1);
  this.routes = (options && options.routes) || routes[className + 's'];
  this.attr = this.attr || {};
  if (options.data !== undefined) {
    this.mixin(options.data)
  }
};

play.modules.plovr.CrudModel.prototype = {
  save: function(opt_callback, opt_target) {
    var data = new goog.Uri.QueryData();
    for (var key in this.attr) {
      data.add(this.paramName + '.' + key, this.attr[key]);
    }
    var route = this.attr.id !== undefined ? this.routes.update(this.attr.id) : this.routes.create();
    this.ajax(route, this.fetchCallback(opt_callback, opt_target), data.toString());
  },
  delete: function() {
    if (this.attr.id) {
      this.ajax(this.routes.delete(this.attr.id));
    }
  },
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
  mixin: function(data) {
    goog.mixin(this.attr, data);
  },
  ajax: function(route, opt_callback, opt_data) {
    var xhr = new goog.net.XhrIo();
    if (opt_callback != null) {
      goog.events.listen(xhr, goog.net.EventType.COMPLETE, opt_callback, false, this);
    }
    var data = opt_data !== undefined ? opt_data : null;
    xhr.send(route.url, route.method, data, {'Accept': 'application/json'});
  }
};
