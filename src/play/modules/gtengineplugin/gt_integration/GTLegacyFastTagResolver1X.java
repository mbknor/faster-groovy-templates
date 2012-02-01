package play.modules.gtengineplugin.gt_integration;

import groovy.lang.Closure;
import play.Play;
import play.classloading.ApplicationClasses;
import play.classloading.ApplicationClassloaderState;
import play.exceptions.TemplateExecutionException;
import play.modules.gtengineplugin.InternalLegacyFastTagsImpls;
import play.template2.GTJavaBase;
import play.template2.exceptions.GTTemplateRuntimeException;
import play.template2.legacy.GTLegacyFastTagResolver;
import play.templates.FastTags;
import play.templates.GroovyTemplate;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GTLegacyFastTagResolver1X implements GTLegacyFastTagResolver {

    private static class LegacyFastTag {
        public final String className;
        public final String methodName;

        private LegacyFastTag(String className, String methodName) {
            this.className = className;
            this.methodName = methodName;
        }
    }

    private static Object lock = new Object();
    private static ApplicationClassloaderState _lastKnownApplicationClassloaderState = null;
    private static Map<String, LegacyFastTag> _tagName2FastTag = new HashMap<String, LegacyFastTag>();

    static Class fc = FastTags.class;

    private static Map<String, LegacyFastTag> getTagName2FastTag() {
        synchronized (lock) {
            if (_lastKnownApplicationClassloaderState == null || !_lastKnownApplicationClassloaderState.equals(Play.classloader.currentState)) {
                // must reload
                _tagName2FastTag = new HashMap<String, LegacyFastTag>();
                _lastKnownApplicationClassloaderState = Play.classloader.currentState;

                // find all FastTag-classes
                List<ApplicationClasses.ApplicationClass> _fastTagClasses = Play.classes.getAssignableClasses( FastTags.class );

                List<Class> classes = new ArrayList<Class>(_fastTagClasses.size()+1);
                classes.add( InternalLegacyFastTagsImpls.class);
                for (ApplicationClasses.ApplicationClass appClass : _fastTagClasses) {
                    classes.add( appClass.javaClass);
                }

                for (Class clazz : classes) {
                    FastTags.Namespace namespace = (FastTags.Namespace)clazz.getAnnotation(FastTags.Namespace.class);
                    String namespacePrefix = "";
                    if ( namespace != null ) {
                        namespacePrefix = namespace.value() + ".";
                    }
                    for ( Method m : clazz.getDeclaredMethods()) {

                        if (m.getName().startsWith("_") && Modifier.isStatic(m.getModifiers()) ) {
                            String tagName = namespacePrefix + m.getName().substring(1);
                            _tagName2FastTag.put(tagName, new LegacyFastTag(clazz.getName(), m.getName()));
                        }
                    }
                }

            }
            return _tagName2FastTag;

        }
    }

    public static String getFullNameToBridgeMethod() {
        return GTLegacyFastTagResolver1X.class.getName() + ".legacyFastTagBridge";
    }

    public LegacyFastTagInfo resolveLegacyFastTag(String tagName) {

        LegacyFastTag tag = getTagName2FastTag().get(tagName);

        if (tag == null ) {
            return null;
        }

        return new LegacyFastTagInfo(getFullNameToBridgeMethod(), tag.className, tag.methodName);

    }

    public static void legacyFastTagBridge(String legacyFastTagClassName, String legacyFastTagMethodName, GTJavaBase template, Map<String, Object> args, Closure body ) {
        try {

            // get the class with the fasttag method on
            Class clazz = Play.classloader.loadClass(legacyFastTagClassName);
            // get the method
            Method m = clazz.getMethod(legacyFastTagMethodName,Map.class, Closure.class, PrintWriter.class, GroovyTemplate.ExecutableTemplate.class, Integer.TYPE);
            if (!Modifier.isStatic(m.getModifiers())) {
                throw new RuntimeException("A fast-tag method must be static: " + m);
            }

            PrintWriter out = new PrintWriter( template.out );
            GroovyTemplate.ExecutableTemplate executableTemplate = new GroovyTemplate.ExecutableTemplate() {

                @Override
                public Object run() {
                    throw new RuntimeException("Not implemented in this wrapper");
                }
            };
            
            int fromLine = 0;

            m.invoke(null, args, body, out, executableTemplate, fromLine);
        } catch (InvocationTargetException e) {
            if ( e.getCause() instanceof TemplateExecutionException) {
                // Must be transformed into GTTemplateRuntimeException
                throw new GTTemplateRuntimeException(e.getCause().getMessage());
            } else {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error when executing legacy fastTag " + legacyFastTagClassName+"."+legacyFastTagMethodName, e);
        }
    }

}
