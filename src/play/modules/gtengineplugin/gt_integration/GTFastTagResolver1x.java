package play.modules.gtengineplugin.gt_integration;

import play.Play;
import play.classloading.ApplicationClasses;
import play.classloading.ApplicationClassloaderState;
import play.template2.GTFastTag;
import play.template2.GTFastTagResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GTFastTagResolver1x implements GTFastTagResolver {

    private static Object lock = new Object();
    private static ApplicationClassloaderState _lastKnownApplicationClassloaderState = null;
    private static List<GTFastTag> fastTagClasses = null;



    public String resolveFastTag(String tagName) {

        synchronized (lock) {
            if (_lastKnownApplicationClassloaderState == null || !_lastKnownApplicationClassloaderState.equals(Play.classloader.currentState) || fastTagClasses == null) {
                _lastKnownApplicationClassloaderState = Play.classloader.currentState;
                fastTagClasses = new ArrayList<GTFastTag>();
                for (ApplicationClasses.ApplicationClass appClass : Play.classes.getAssignableClasses( GTFastTag.class ) ) {
                    try {
                        fastTagClasses.add( (GTFastTag)appClass.javaClass.newInstance());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        for ( GTFastTag fastTag : fastTagClasses) {
            String res = fastTag.resolveFastTag( tagName);
            if ( res != null) {
                // found a match
                return res;
            }
        }
        // no match found..
        return null;
    }
}
