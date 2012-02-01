package play.modules.gtengineplugin;

import play.PlayPlugin;
import play.templates.Template;
import play.vfs.VirtualFile;

public class GTEnginePlugin extends PlayPlugin {

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
        Template t = TemplateLoader.load(file);
        return t;

    }
}
