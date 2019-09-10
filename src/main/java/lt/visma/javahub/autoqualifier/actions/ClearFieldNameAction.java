package lt.visma.javahub.autoqualifier.actions;

import java.io.File;

import com.github.javaparser.Position;

import lt.visma.javahub.utils.TextFile;

public class ClearFieldNameAction extends AbstractAction{

    private File sourceFile;
    private Position begin;
    
    @SuppressWarnings("unused")
    private Position end;

    @Override
    public void perform() throws Exception {
        
        TextFile textFile = getFile(getSourceFile());
        int lineNumber = begin.line -1;
        
        String annotation = textFile.getLine(lineNumber); //replaceLine(lineNumber, annotation);
        if (annotation == null || annotation.trim().isEmpty())
            return;
        
        int autowiredCol = annotation.indexOf("@Autowired");
        if (autowiredCol >= 0)
            textFile.replaceLine(lineNumber, "@Autowired");
        else
            textFile.replaceLine(lineNumber, "");
    }

    public ClearFieldNameAction setSourceFile(File file) {
        this.sourceFile = file;
        return this;
    }

    public ClearFieldNameAction setLocation(Position begin, Position end) {
        this.setBegin(begin);
        this.end = end;
        return this;
    }

    public Position getBegin() {
        return begin;
    }

    public void setBegin(Position begin) {
        this.begin = begin;
    }

    public File getSourceFile() {
        return sourceFile;
    }
}
