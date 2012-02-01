package play.classloading;

import play.Play;
import play.classloading.ApplicationClasses;
import play.template2.compile.GTTypeResolver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;


public class GTTypeResolver1xImpl implements GTTypeResolver {

    public byte[] getTypeBytes(String name) {

        ApplicationClasses.ApplicationClass applicationClass = Play.classes.getApplicationClass(name);

        // ApplicationClass exists
        if (applicationClass != null) {

            if (applicationClass.javaByteCode != null) {
                return applicationClass.javaByteCode;
            }
        }

        // look for standard class
        return Play.classloader.getClassDefinition(name);
    }

}
