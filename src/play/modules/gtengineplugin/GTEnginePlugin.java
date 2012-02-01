package play.modules.gtengineplugin;

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
    

    static {
        // need to init immediately since templates are being used even before onApplicationStart is fired
        TemplateLoader.init();
    }

    @Override
    public void onApplicationStart() {
        // need to re-init when app restarts
        TemplateLoader.init();
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
