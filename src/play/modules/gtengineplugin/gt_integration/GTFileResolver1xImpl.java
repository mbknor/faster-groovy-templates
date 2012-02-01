package play.modules.gtengineplugin.gt_integration;

import play.Play;
import play.template2.GTFileResolver;
import play.template2.GTTemplateLocationReal;
import play.vfs.VirtualFile;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

public class GTFileResolver1xImpl implements GTFileResolver.Resolver {

    // when null we look for templates in working directory, if list, we look for template in thouse folders..
    protected final File[] templateFolders;

    public GTFileResolver1xImpl(List<VirtualFile> templatesPaths) {
        templateFolders = new File[templatesPaths.size()];

        int i=0;
        for (VirtualFile vf : templatesPaths) {
            templateFolders[i] = vf.getRealFile();
            i++;
        }

    }


    public GTTemplateLocationReal getTemplateLocationReal(String queryPath) {
        // look for template file in all folders in templateFolders-list
        for ( File folder : templateFolders) {

            if ( folder == null) {
                // look for template in working dir.
                File file = new File(queryPath);
                if (file.exists() && file.isFile()) {
                    try {
                        return new GTTemplateLocationReal(VirtualFile.open(file).relativePath(), file.toURI().toURL());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {

                File file = new File ( folder, queryPath);
                if (file.exists() && file.isFile()) {
                    try {
                        return new GTTemplateLocationReal(VirtualFile.open(file).relativePath(), file.toURI().toURL());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        
        // try to find it directly on the app-root before we give up
        VirtualFile tf = Play.getVirtualFile(queryPath);
        if (tf != null && tf.exists() && !tf.isDirectory()) {
            try {
                return new GTTemplateLocationReal(tf.relativePath(), tf.getRealFile().toURI().toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        
        // didn't find it
        return null;
    }

    public GTTemplateLocationReal getTemplateLocationFromRelativePath(String relativePath) {
        VirtualFile vf = VirtualFile.fromRelativePath(relativePath);
        if ( vf == null || !vf.exists() || vf.isDirectory()) {
            return null;
        }

        try {
            return new GTTemplateLocationReal(relativePath, vf.getRealFile().toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
