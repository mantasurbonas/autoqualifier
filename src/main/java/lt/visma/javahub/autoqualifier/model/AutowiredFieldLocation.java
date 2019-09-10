package lt.visma.javahub.autoqualifier.model;

import java.io.File;
import java.util.Optional;

import com.github.javaparser.Position;

public class AutowiredFieldLocation {
    private File file;
    private String propertyClass;
    private String propertyQualifier;
    
    private Position qualifierBegin;
    private Position qualifierEnd;
    private Position autowiredEnd;
    private Position autowiredBegin;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getPropertyClass() {
        return propertyClass;
    }

    public void setPropertyClass(String propertyClass) {
        this.propertyClass = propertyClass;
    }

    public String getPropertyQualifier() {
        return propertyQualifier;
    }

    public AutowiredFieldLocation setPropertyQualifier(String propertyQualifier) {
        this.propertyQualifier = propertyQualifier;
        return this;
    }

    public AutowiredFieldLocation setAutowiredLocation(Optional<Position> begin, Optional<Position> end) {
        if (begin.isEmpty() || end.isEmpty())
            return this;
        
        this.autowiredBegin = begin.get();
        this.autowiredEnd = end.get();
        
        return this;
    }
    
    public AutowiredFieldLocation setQualifierLocation(Optional<Position> begin, Optional<Position> end) {
        if (begin.isEmpty() || end.isEmpty())
            return this;
        
        this.qualifierBegin = begin.get();
        this.qualifierEnd = end.get();
        
        return this;
    }

    public Position getQualifierBegin() {
        return qualifierBegin;
    }
    
    public Position getQualifierEnd() {
        return qualifierEnd;
    }

    public Position getAutowiredBegin() {
        return autowiredBegin;
    }

    public Position getAutowiredEnd() {
        return autowiredEnd;
    }
    
    public String toString() {
        return "@Autowired(\"" +propertyQualifier+ "\") " + propertyClass + " at " + autowiredBegin.line + ":" + autowiredBegin.column + " in " + file;
    }


}