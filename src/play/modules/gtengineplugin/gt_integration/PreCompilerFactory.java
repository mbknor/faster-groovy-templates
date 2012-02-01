package play.modules.gtengineplugin.gt_integration;

import play.template2.GTTemplateRepo;
import play.template2.compile.GTPreCompiler;
import play.template2.compile.GTPreCompilerFactory;

public class PreCompilerFactory implements GTPreCompilerFactory {
    public GTPreCompiler createCompiler(GTTemplateRepo templateRepo) {
        return new GTPreCompiler1xImpl(templateRepo);
    }
}
