package lt.visma.javahub.autoqualifier.actions;

import java.io.File;

import com.github.javaparser.Position;

public class SetFieldNameAction extends AbstractAction{

    private String propertyName;
    private File sourceFile;
    private Position position;

    public SetFieldNameAction setPropertyName(String newQualifier) {
        this.propertyName = newQualifier;
        return this;
    }

    public SetFieldNameAction setSourceFile(File file) {
        this.sourceFile = file;
        return this;
    }

    public SetFieldNameAction setPosition(Position lineNumber) {
        this.position = lineNumber;
        return this;
    }
    
    @Override
    public void perform() throws Exception {
        
        String annotation = "@Autowired";
        
        if (getPropertyName() != null && !getPropertyName().trim().isEmpty())
            annotation += " @Qualifier(\""+getPropertyName()+"\")";
        
        getFile(getSourceFile())
            .replaceLine(position.line-1, annotation);
    }
    
    public String getPropertyName() {
        return propertyName;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public Position getPosition() {
        return position;
    }


}
