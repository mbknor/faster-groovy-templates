package play.modules.gtengineplugin.gt_integration;

import play.template2.GTTagContext;
import play.templates.TagContext;

import java.util.Map;

// Briding so that it is the TagContext-impl in play 1 that is the master one..
public class GTTagContextBridge extends GTTagContext {

    public static class GTTagContextInfoBridge implements GTTagContextInfo {
        private final TagContext real;

        public GTTagContextInfoBridge(TagContext real) {
            this.real = real;
        }

        @Override
        public String getTagName() {
            return real.tagName;
        }

        @Override
        public Map<String, Object> getData() {
            return real.data;
        }
    }

    @Override
    public void init() {
        TagContext.init();
    }

    @Override
    public void enterTag(String name) {
        TagContext.enterTag(name);
    }

    @Override
    public void exitTag() {
        TagContext.exitTag();
    }

    @Override
    public GTTagContextInfo current() {
        TagContext org = TagContext.current();
        return new GTTagContextInfoBridge(org);
    }

    @Override
    public GTTagContextInfo parent() {
        TagContext org = TagContext.parent();
        return new GTTagContextInfoBridge(org);
    }

    @Override
    public boolean hasParentTag(String name) {
        return TagContext.hasParentTag(name);
    }

    @Override
    public GTTagContextInfo parent(String name) {
        TagContext org = TagContext.parent(name);
        return new GTTagContextInfoBridge(org);
    }
}
