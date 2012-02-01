package play.modules.gtengineplugin;

import groovy.lang.Closure;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
import play.data.validation.Error;
import play.data.validation.Validation;
import play.exceptions.TagInternalException;
import play.exceptions.TemplateExecutionException;
import play.exceptions.TemplateNotFoundException;
import play.libs.Codec;
import play.mvc.Http;
import play.mvc.Router.ActionDefinition;
import play.mvc.Scope.Flash;
import play.mvc.Scope.Session;
import play.templates.BaseTemplate.RawData;
import play.templates.GroovyTemplate.ExecutableTemplate;
import play.templates.JavaExtensions;
import play.templates.TagContext;
import play.templates.Template;
import play.utils.HTML;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Fast tags implementation
 */
public class InternalLegacyFastTagsImpls {

    // Intentionally not porting fasttags too tied up with the current Play impl

    public static void _jsRoute(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        final Object arg = args.get("arg");
        if (!(arg instanceof ActionDefinition)) {
            throw new TemplateExecutionException(template.template, fromLine, "Wrong parameter type, try #{jsRoute @Application.index() /}", new TagInternalException("Wrong parameter type"));
        }
        final ActionDefinition action = (ActionDefinition)arg;
        out.print("{");
        if (action.args.isEmpty()) {
            out.print("url: function() { return '" + action.url.replace("&amp;", "&") + "'; },");
        } else {
            out.print("url: function(args) { var pattern = '" + action.url.replace("&amp;", "&") + "'; for (var key in args) { pattern = pattern.replace(':'+key, args[key]); } return pattern; },");
        }
        out.print("method: '" + action.method + "'");
        out.print("}");
    }

    public static void _authenticityToken(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        out.println("<input type=\"hidden\" name=\"authenticityToken\" value=\"" + Session.current().getAuthenticityToken() + "\">");
    }

    /**
     * Generates a html form element linked to a controller action
     * @param args tag attributes
     * @param body tag inner body
     * @param out the output writer
     * @param template enclosing template
     * @param fromLine template line number where the tag is defined
     */
    public static void _form(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        ActionDefinition actionDef = (ActionDefinition) args.get("arg");
        if (actionDef == null) {
            actionDef = (ActionDefinition) args.get("action");
        }
        String enctype = (String) args.get("enctype");
        if (enctype == null) {
            enctype = "application/x-www-form-urlencoded";
        }
        if (actionDef.star) {
            actionDef.method = "POST"; // prefer POST for form ....
        }
        if (args.containsKey("method")) {
            actionDef.method = args.get("method").toString();
        }
        if (!("GET".equals(actionDef.method) || "POST".equals(actionDef.method))) {
            String separator = actionDef.url.indexOf('?') != -1 ? "&" : "?";
            actionDef.url += separator + "x-http-method-override=" + actionDef.method.toUpperCase();
            actionDef.method = "POST";
        }
        String encoding = Http.Response.current().encoding;
        out.print("<form action=\"" + actionDef.url + "\" method=\"" + actionDef.method.toLowerCase() + "\" accept-charset=\""+encoding+"\" enctype=\"" + enctype + "\" " + serialize(args, "action", "method", "accept-charset", "enctype") + ">");
        if (!("GET".equals(actionDef.method))) {
            _authenticityToken(args, body, out, template, fromLine);
        }
        out.println(JavaExtensions.toString(body));
        out.print("</form>");
    }
    
    /**
     * The field tag is a helper, based on the spirit of Don't Repeat Yourself. 
     * @param args tag attributes
     * @param body tag inner body
     * @param out the output writer
     * @param template enclosing template
     * @param fromLine template line number where the tag is defined
     */
    public static void _field(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        Map<String,Object> field = new HashMap<String,Object>();
        String _arg = args.get("arg").toString();
        field.put("name", _arg);
        field.put("id", _arg.replace('.','_'));
        field.put("flash", Flash.current().get(_arg));
        field.put("flashArray", field.get("flash") != null && !StringUtils.isEmpty(field.get("flash").toString()) ? field.get("flash").toString().split(",") : new String[0]);
        field.put("error", Validation.error(_arg));
        field.put("errorClass", field.get("error") != null ? "hasError" : "");
        String[] pieces = _arg.split("\\.");
        Object obj = body.getProperty(pieces[0]);
        if(obj != null){
            if(pieces.length > 1){
                for(int i = 1; i < pieces.length; i++){
                    try{
                        Field f = obj.getClass().getField(pieces[i]);
                        if(i == (pieces.length-1)){
                            try{
                                Method getter = obj.getClass().getMethod("get"+JavaExtensions.capFirst(f.getName()));
                                field.put("value", getter.invoke(obj, new Object[0]));
                            }catch(NoSuchMethodException e){
                                field.put("value",f.get(obj).toString());
                            }
                        }else{
                            obj = f.get(obj);
                        }
                    }catch(Exception e){
                        // if there is a problem reading the field we dont set any value
                    }
                }
            }else{
                field.put("value", obj);
            }
        }
        body.setProperty("field", field);
        body.call();
    }

    /**
     * Generates a html link to a controller action
     * @param args tag attributes
     * @param body tag inner body
     * @param out the output writer
     * @param template enclosing template
     * @param fromLine template line number where the tag is defined
     */
    public static void _a(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        ActionDefinition actionDef = (ActionDefinition) args.get("arg");
        if (actionDef == null) {
            actionDef = (ActionDefinition) args.get("action");
        }
        if (!("GET".equals(actionDef.method))) {
            if (!("POST".equals(actionDef.method))) {
                String separator = actionDef.url.indexOf('?') != -1 ? "&" : "?";
                actionDef.url += separator + "x-http-method-override=" + actionDef.method;
                actionDef.method = "POST";
            }
            String id = Codec.UUID();
            out.print("<form method=\"POST\" id=\"" + id + "\" " +(args.containsKey("target") ? "target=\"" + args.get("target") + "\"" : "")+ " style=\"display:none\" action=\"" + actionDef.url + "\">");
            _authenticityToken(args, body, out, template, fromLine);
            out.print("</form>");
            out.print("<a href=\"javascript:document.getElementById('" + id + "').submit();\" " + serialize(args, "href") + ">");
            out.print(JavaExtensions.toString(body));
            out.print("</a>");
        } else {
            out.print("<a href=\"" + actionDef.url + "\" " + serialize(args, "href") + ">");
            out.print(JavaExtensions.toString(body));
            out.print("</a>");
        }
    }

    public static void _error(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        if (args.get("arg") == null && args.get("key") == null) {
            throw new TemplateExecutionException(template.template, fromLine, "Please specify the error key", new TagInternalException("Please specify the error key"));
        }
        String key = args.get("arg") == null ? args.get("key") + "" : args.get("arg") + "";
        Error error = Validation.error(key);
        if (error != null) {
            if (args.get("field") == null) {
                out.print(error.message());
            } else {
                out.print(error.message(args.get("field") + ""));
            }
        }
    }

    static boolean _evaluateCondition(Object test) {
        if (test != null) {
            if (test instanceof Boolean) {
                return ((Boolean) test).booleanValue();
            } else if (test instanceof String) {
                return ((String) test).length() > 0;
            } else if (test instanceof Number) {
                return ((Number) test).intValue() != 0;
            } else if (test instanceof Collection) {
                return !((Collection) test).isEmpty();
            } else {
                return true;
            }
        }
        return false;
    }

    static String __safe(Template template, Object val) {
        if (val instanceof RawData) {
            return ((RawData) val).data;
        }
        if (!template.name.endsWith(".html") || TagContext.hasParentTag("verbatim")) {
            return val.toString();
        }
        return HTML.htmlEscape(val.toString());
    }


    public static String serialize(Map<?, ?> args, String... unless) {
        StringBuilder attrs = new StringBuilder();
        Arrays.sort(unless);
        for (Object o : args.keySet()) {
            String attr = o.toString();
            String value = args.get(o) == null ? "" : args.get(o).toString();
            if (Arrays.binarySearch(unless, attr) < 0 && !attr.equals("arg")) {
                attrs.append(attr);
                attrs.append("=\"");
                attrs.append(value);
                attrs.append("\" ");
            }
        }
        return attrs.toString();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface Namespace {

        String value() default "";
    }
}
