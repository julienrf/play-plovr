package controllers;

import play.Play;
import play.cache.Cache;
import play.modules.plovr.RouteHelper;
import play.mvc.Controller;
import play.mvc.Router;

import java.util.Date;
import java.util.List;

/**
 * Provide helpers to reduce the gap between the code client-side and server-side
 */
public class ClosureLibrarySupport extends Controller {

    /**
     * Export routes annotated with the @ExportClientSide annotation.
     * 
     * It generates a javascript function for each route, which can be used like this:
     * For a route named MyController.myAction(String param), your client side code could look like that:
     *
     *   var route = routes.MyController.myAction('foo');
     *   goog.net.XhrIo.send(route.url, callback, route.method);
     */
    public static void routes() {
        List<Router.ActionDefinition> routes = null;

        // In prod mode, avoid to render multiple times the routes, since they are not supposed to change at runtime
        if (Play.mode == Play.Mode.PROD) {
            final Long lastModified = Cache.get("play.modules.plovr.routes.lastModified", Long.class); // That code looks a bit boiler plate… I would need a if (request.ifModifiedSince(Cache.get("lastModified")))
            if (lastModified != null && request.headers.containsKey("if-modified-since")) {
                final Long date = Long.parseLong(request.headers.get("if-modified-since").value());
                if (lastModified <= date) {
                    notModified();
                }
            }
            routes = Cache.get("play.modules.plovr.jsroutes", List.class);
        }
        
        if (routes == null) {
            routes = RouteHelper.getJavascriptRoutes(); // All the logic is in this single line
            Cache.set("play.modules.plovr.jsroutes", routes);
        }

        String namespace = Play.configuration.getProperty("plovr.jsroutes.namespace", "routes");

        final Date lastModified = new Date();
        Cache.set("play.modules.plovr.routes.lastModified", lastModified.getTime());
        response.setHeader("Last-Modified", "" + lastModified.getTime());
        
        request.format = "js";
        render(namespace, routes);
    }
}
