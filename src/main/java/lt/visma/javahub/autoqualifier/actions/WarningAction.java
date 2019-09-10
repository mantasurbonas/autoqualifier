package lt.visma.javahub.autoqualifier.actions;

import java.io.File;

import org.apache.maven.plugin.logging.Log;

public class WarningAction extends AbstractAction{

    private Log log;
    private File sourceFile;
    private String message;

    @Override
    public void perform() {
        String msg = "WARNING: "+getMessage()+" in "+sourceFile.getAbsolutePath();
        
        if (log != null)
            log.error(msg);
        else
            System.err.println(msg);
    }

    public WarningAction setSourceFile(File componentFile) {
        this.sourceFile = componentFile;
        return this;
    }

    public WarningAction setMessage(String msg) {
        this.message = msg;
        return this;
    }

    public WarningAction setLog(Log log) {
        this.log = log;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Log getLog() {
        return log;
    }

}
