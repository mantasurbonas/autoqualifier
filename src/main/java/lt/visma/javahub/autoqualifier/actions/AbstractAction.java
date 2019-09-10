package lt.visma.javahub.autoqualifier.actions;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import lt.visma.javahub.utils.TextFile;

public abstract class AbstractAction {

    protected Map<String, TextFile> fileCache;

    public abstract void perform() throws Exception;

    public AbstractAction setFileCache(Map<String, TextFile> fc) {
        this.fileCache = fc;
        return this;
    }
    
    protected TextFile getFile(File sourceFile) throws IOException {
        String key = sourceFile.getAbsolutePath();
        
        TextFile src = fileCache.get(key);
        if (src == null) {
            src = new TextFile(sourceFile);
            fileCache.put(key, src);
        }
        
        return src;
    }
    
}
