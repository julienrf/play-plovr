package play.modules.plovr;

import play.Logger;
import play.Play;
import play.classloading.ApplicationClasses;
import play.mvc.Router;
import play.utils.Java;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Find application routes and export them for Javascript use
 */
public class RouteHelper {
    /**
     * @return all actions (controller methods) annotated with the @ExportClientSide annotation
     */
    public static List<Method> exportedActions() {
        // Find controllers
        final List<Class> controllers = new ArrayList<Class>();
        for (ApplicationClasses.ApplicationClass clazz : Play.classes.all()) {
            if (clazz.name.startsWith("controllers.")) {
                if (clazz.javaClass != null && !clazz.javaClass.isInterface() && !clazz.javaClass.isAnnotation()) {
                    controllers.add(clazz.javaClass);
                }
            }
        }

        // Find annotated methods
        return Java.findAllAnnotatedMethods(controllers, ExportClientSide.class); // TODO filter only static void methods?
    }

    /**
     * Given a list of actions, this function returns a list of corresponding action definitions where each parameter is given
     * the value ":0:", ":1:", etc. and so on, according to its position.
     * @return A list of action definitions where action parameters were replaced with ":0:", ":1:", etc.
     */
    public static List<Router.ActionDefinition> getJavascriptRoutes() {
        final List<Router.ActionDefinition> resources = new ArrayList<Router.ActionDefinition>();
        for (Method action : exportedActions()) {
            final ExportClientSide exportAnnotation = action.getAnnotation(ExportClientSide.class);
            List<String> params = new ArrayList<String>();
            try {
                params = new ArrayList<String>(Arrays.asList(Java.parameterNames(action)));
            } catch (Exception e) {
                Logger.error("Unable to retrieve parameters for method %s. Error is: %s", action.getName(), e.getMessage());
            }

            for (String ignoredParam : exportAnnotation.skip()) {
                params.remove(ignoredParam); // We just skip ignored parameters
            }

            final Map<String, Object> args = new HashMap<String, Object>(params.size()); // action arguments
            for (int i = 0 ; i < params.size() ; i++) { // HACK this code is Javascript specific
                args.put(params.get(i), ":" + i + ":");
            }
            resources.add(Router.reverse(actionName(action), args));
        }
        return resources;
    }

    /**
     * @param action Action
     * @return The qualified name of the action, without arguments, e.g. "modules.content.ContentController.show"
     */
    public static String actionName(final Method action) {
        return action.getDeclaringClass().getName() + "." + action.getName();
    }
}
