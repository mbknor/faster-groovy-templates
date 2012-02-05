package play.modules.gtengineplugin.gt_integration;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingPropertyException;
import play.Play;
import play.data.binding.Unbinder;
import play.exceptions.ActionNotFoundException;
import play.exceptions.NoRouteFoundException;
import play.exceptions.PlayException;
import play.exceptions.UnexpectedException;
import play.mvc.ActionInvoker;
import play.mvc.Http;
import play.mvc.Router;
import play.template2.GTGroovyBase;
import play.template2.GTJavaBase;
import play.template2.exceptions.GTRuntimeExceptionForwarder;
import play.template2.exceptions.GTTemplateRuntimeException;
import play.templates.GroovyTemplate;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class GTGroovyBase1xImpl extends GTGroovyBase {

    @Override
    public Object getProperty(String property) {
        try {
            if (property.equals("actionBridge")) {
                // special object used to resolving actions
                GTJavaBase template = (GTJavaBase)super.getProperty("java_class");
                return new ActionBridge(template.templateLocation.relativePath);
            }
            return super.getProperty(property);
        } catch (MissingPropertyException mpe) {
            return null;
        }
    }

    @Override
    public Class _resolveClass(String clazzName) {
        try {
            return Play.classloader.loadClass(clazzName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static class ActionBridge extends GroovyObjectSupport {

        String templateName = null;
        String controller = null;
        boolean absolute = false;

        public ActionBridge(String templateName, String controllerPart, boolean absolute) {
            this.templateName = templateName;
            this.controller = controllerPart;
            this.absolute = absolute;
        }

        public ActionBridge(String templateName) {
            this.templateName = templateName;
        }

        @Override
        public Object getProperty(String property) {
            return new ActionBridge(templateName, controller == null ? property : controller + "." + property, absolute);
        }

        public Object _abs() {
            this.absolute = true;
            return this;
        }

        private Integer computeMethodHash(Class<?>[] parameters) {
            String[] names = new String[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Class<?> param = parameters[i];
                names[i] = "";
                if (param.isArray()) {
                    int level = 1;
                    param = param.getComponentType();
                    // Array of array
                    while (param.isArray()) {
                        level++;
                        param = param.getComponentType();
                    }
                    names[i] = param.getName();
                    for (int j = 0; j < level; j++) {
                        names[i] += "[]";
                    }
                } else {
                    names[i] = param.getName();
                }
            }
            return computeMethodHash(names);
        }

        private Integer computeMethodHash(String[] parameters) {
            StringBuffer buffer = new StringBuffer();
            for (String param : parameters) {
                buffer.append(param);
            }
            Integer hash = buffer.toString().hashCode();
            if (hash < 0) {
                return -hash;
            }
            return hash;
        }



        @Override
        @SuppressWarnings("unchecked")
        public Object invokeMethod(String name, Object param) {
            try{
                try {
                    if (controller == null) {
                        controller = Http.Request.current().controller;
                    }
                    String action = controller + "." + name;
                    if (action.endsWith(".call")) {
                        action = action.substring(0, action.length() - 5);
                    }
                    try {
                        Map<String, Object> r = new HashMap<String, Object>();
                        Method actionMethod = (Method) ActionInvoker.getActionMethod(action)[1];
                        String[] names = (String[]) actionMethod.getDeclaringClass().getDeclaredField("$" + actionMethod.getName() + computeMethodHash(actionMethod.getParameterTypes())).get(null);
                        if (param instanceof Object[]) {
                            if(((Object[])param).length == 1 && ((Object[])param)[0] instanceof Map) {
                                r = (Map<String,Object>)((Object[])param)[0];
                            } else {
                                // too many parameters versus action, possibly a developer error. we must warn him.
                                if (names.length < ((Object[]) param).length) {
                                    throw new NoRouteFoundException(action, null);
                                }
                                for (int i = 0; i < ((Object[]) param).length; i++) {
                                    if (((Object[]) param)[i] instanceof Router.ActionDefinition && ((Object[]) param)[i] != null) {
                                        Unbinder.unBind(r, ((Object[]) param)[i].toString(), i < names.length ? names[i] : "", actionMethod.getAnnotations());
                                    } else if (isSimpleParam(actionMethod.getParameterTypes()[i])) {
                                        if (((Object[]) param)[i] != null) {
                                            Unbinder.unBind(r, ((Object[]) param)[i].toString(), i < names.length ? names[i] : "", actionMethod.getAnnotations());
                                        }
                                    } else {
                                        Unbinder.unBind(r, ((Object[]) param)[i], i < names.length ? names[i] : "", actionMethod.getAnnotations());
                                    }
                                }
                            }
                        }
                        Router.ActionDefinition def = Router.reverse(action, r);
                        if (absolute) {
                            def.absolute();
                        }
                        if (templateName.endsWith(".xml")) {
                            def.url = def.url.replace("&", "&amp;");
                        }
                        return def;
                    } catch (ActionNotFoundException e) {
                        throw new NoRouteFoundException(action, null);
                    }
                } catch (Exception e) {
                    if (e instanceof PlayException) {
                        throw (PlayException) e;
                    }
                    throw new UnexpectedException(e);
                }
            } catch (Exception e) {
                throw new GTRuntimeExceptionForwarder(e);
            }
        }

        static boolean isSimpleParam(Class type) {
            return Number.class.isAssignableFrom(type) || type.equals(String.class) || type.isPrimitive();
        }

    }

}
