package play.modules.gtengineplugin;

import play.Play;
import play.PlayPlugin;
import play.templates.Template;
import play.vfs.VirtualFile;

public class GTEnginePlugin extends PlayPlugin {
    
    // This module contains new (fixed) versions of some templates
    // This list is used to ignore the original
    private String[] relativePathsToIgnore = new String[]{
            "{play}/framework/templates/tags/errors.tag",
            "{play}/framework/templates/tags/fixture.tag",
            "{play}/framework/templates/tags/selenium.html",
            "{module:crud}/"
    };

    synchronized private void init() {
        fixTemplatesPathOrder();
        TemplateLoader.init();
    }

    private void fixTemplatesPathOrder() {
        // Make sure our app/view-folder is the first one amongst the modules listed in Play.templatesPath
        // Look for our path
        int index = 0;
        for( VirtualFile vf : Play.templatesPath ) {
            // This is our path if we find the special file here..
            if (vf.child("__faster_groovy_templates.txt").exists()) {
                // This is our path.
                if ( index == 1) {
                    // the location is correct
                } else {
                    // move it to location 1 (right after the app-view folder
                    Play.templatesPath.remove( index );
                    Play.templatesPath.add(1, vf);
                }
                break;
            }
            index++;
        }
    }

    @Override
    public void onLoad() {
        // Must init right away since we have to be inited when routes are being parsed
        init();
    }

    @Override
    public void onApplicationStart() {
        // need to re-init when app restarts
        init();
    }

    @Override
    public Template loadTemplate(VirtualFile file) {
        
        // Some templates are bundled with this module - fixed versions
        // Must check if we are requesting such template - and then ignore it..
        // This only happens when scanning for templates when precompiling

        // Must always compile routes-files
        String relativePath = file.relativePath();
        if ( !relativePath.endsWith("/conf/routes")) {
            for ( String pathToIgnore : relativePathsToIgnore) {
                if ( relativePath.startsWith( pathToIgnore)) {
                    return null;
                }
            }
        }

        Template t = TemplateLoader.load(file);
        return t;

    }
}
