package lt.visma.javahub.autoqualifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.maven.plugin.logging.Log;

import lt.visma.javahub.autoqualifier.actions.AbstractAction;
import lt.visma.javahub.autoqualifier.actions.ClearFieldNameAction;
import lt.visma.javahub.autoqualifier.actions.ErrorAction;
import lt.visma.javahub.autoqualifier.actions.LogAction;
import lt.visma.javahub.autoqualifier.actions.SetBeanNameAction;
import lt.visma.javahub.autoqualifier.actions.SetFieldNameAction;
import lt.visma.javahub.autoqualifier.actions.WarningAction;
import lt.visma.javahub.autoqualifier.model.AutowiredFieldLocation;
import lt.visma.javahub.autoqualifier.model.ClassAnnotationLocation;
import lt.visma.javahub.utils.TextFile;

/***
 * main business logic of this tool:
 * 
 *  searches recursively a given source root for Java source files:
 *  
 *  finds Spring @Component s, @Repository ies and @Service s and either creates appropriate names or erases them (as configured).
 *  finds @Autowired fields and either creates appropriately named @Qualifier annotations or erases them (as configured). 
 * 
 * @author mantas.urbonas
 *
 */
public class Qualifier {

    public enum Mode{
        /*** do nothing */
        ignore,
        
        /*** logs all components and autowired fields found */
        log,
        
        /*** warn if some components are unnamed, or some autowired fields are unqualified - don't touch source files*/
        warnUnnamed,
        
        /*** warn if some components are named, or some autowired fields are qualified - don't touch source files*/
        warnNamed,
        
        /*** log error and stop build process if some components are unnamed, or some autowired fields are unqualified - don't touch source files*/ 
        errorUnnamed,
        
        /*** log error and stop build process if some components are named, or some autowired fields are qualified - don't touch source files*/ 
        errorNamed,
        
        /*** automatically name components and qualify autowired fields by dependency classname - update source files where necessary */ 
        nameByClass,
        
        /*** automatically name components and qualify autowired fields by random md5 hash  - update source files where necessary */ 
        nameRandomly,
        
        /*** remove names from components and autowired fields - update source files where necessary */
        unname
    }
    
    private Mode mode = Mode.log;
    
    private List<AbstractAction> actions = new ArrayList<>();
    
    private Log log;
    
    public Qualifier() {
    }
    
    public Qualifier setMode(String mode) {
        this.mode = Mode.valueOf(mode);
        return this;
    }

    public Qualifier setMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public Qualifier setLog(Log log) {
        this.log = log;
        return this;
    }
    
    public Qualifier clear() {
        this.actions.clear();
        return this;
    }
    
    public static List<ClassAnnotationLocation> findSpringComponents(Path rootPath) throws IOException {
        return new JavaFilesScanner(rootPath).findAll(new SpringComponentInspector());
    }
    
    public static List<AutowiredFieldLocation> findAutowiredFields(Path rootPath) throws IOException {
        return new JavaFilesScanner(rootPath).findAll(new AutowiredPropertiesInspector());
    }
    
    public Qualifier reviewSources(String rootPath) throws IOException {
        return reviewSources(Paths.get(rootPath));
    }
    
    public Qualifier reviewSources(File rootPath) throws IOException {
        return reviewSources(rootPath.toPath());
    }
    
    public Qualifier reviewSources(Path rootPath) throws IOException {
        if (mode == Mode.ignore)
            return this;
        
        if (log != null)
            log.info("autoqualifying sources ("+mode+") in directory "+rootPath);
        
        actions.clear();
        
        JavaFilesScanner javaFilesScanner = new JavaFilesScanner(rootPath);
        
        List<ClassAnnotationLocation> springComponentLocations = javaFilesScanner.findAll(new SpringComponentInspector());
        List<AutowiredFieldLocation> autowiredFieldsLocations = javaFilesScanner.findAll(new AutowiredPropertiesInspector());
        
        switch(mode) {
            
        case log:
            actions.addAll(logComponents(springComponentLocations));
            actions.addAll(logFields(autowiredFieldsLocations));
        
        case warnUnnamed:
        case warnNamed:
            actions.addAll(warnComponentsComply(springComponentLocations, mode));
            actions.addAll(warnFieldsComply(autowiredFieldsLocations, mode));
            return this;

        case errorUnnamed:
        case errorNamed:
            actions.addAll(assertComponentsComply(springComponentLocations, mode));
            actions.addAll(assertFieldsComply(autowiredFieldsLocations, mode));
            return this;
            
        case nameByClass:
        case nameRandomly:
            Map<String, String> beanNameMap = createNameMap(springComponentLocations, mode);
            
            actions.addAll(nameSpringComponents(springComponentLocations, beanNameMap));
            actions.addAll(qualifyAutowiredFields(autowiredFieldsLocations, beanNameMap));  
            return this;
            
        case unname:
            actions.addAll(unnameSpringComponents(springComponentLocations));
            actions.addAll(unqualifyAutowiredFields(autowiredFieldsLocations)); 
            return this;
            
        default:
            throw new RuntimeException("unrecognized mode : "+mode);
        }
    }
    
    public Qualifier executeActions() throws Exception{ 
        
        Map<String, TextFile> fileCache = new HashMap<>();
        
        boolean failed = false;
        
        for (AbstractAction action: actions) {
            if (action != null)
                action.setFileCache(fileCache).perform();
            
            failed = action instanceof ErrorAction || failed;
        }
        
        if (failed)
            throw new RuntimeException("failed due to the errors above.");
        
        saveSourceFiles(fileCache.values());
        
        return this;
    }

    public List<AbstractAction> getActions() {
        return actions;
    }

    
    private static Map<String, String> createNameMap(List<ClassAnnotationLocation> springComponentLocations, Mode mode) {
            return springComponentLocations.stream()
                .collect( Collectors.toMap ( l -> l.getShortClassName(), 
                                             l -> getQualifierName(mode, l) ) );
    }
    
    
    private List<AbstractAction> logComponents(List<ClassAnnotationLocation> springComponentLocations) {
        if (springComponentLocations.isEmpty())
            return Collections.singletonList(new LogAction("no Spring components found").setLog(log));
        
        return springComponentLocations.stream()
                .map(location -> logSpringComponent(location))
                .collect(Collectors.toList());
    }
    
    private List<AbstractAction> logFields(List<AutowiredFieldLocation> autowiredFieldsLocations) {
        if (autowiredFieldsLocations.isEmpty())
            return Collections.singletonList(new LogAction("no autowired fields found").setLog(log));
        
        return autowiredFieldsLocations.stream()
                .map(location -> logAutowiredField(location))
                .collect(Collectors.toList());
    }
    
    private List<AbstractAction> warnComponentsComply(List<ClassAnnotationLocation> springComponentLocations, Mode mode) {
        return springComponentLocations.stream()
                .map(component -> warnComponentComplies(component, mode))
                .filter(action -> action != null)
                .collect(Collectors.toList());
    }
    
    private List<AbstractAction> warnFieldsComply(List<AutowiredFieldLocation> autowiredFieldsLocations, Mode mode) {
        return autowiredFieldsLocations.stream()
                .map( location -> warnFieldComplies(location, mode))
                .filter( action -> action != null)
                .collect(Collectors.toList());
    }

    private List<AbstractAction> assertComponentsComply(List<ClassAnnotationLocation> springComponentLocations, Mode mode) {
        return springComponentLocations.stream()
                .map( location -> assertComponentComplies(location, mode))
                .filter( action -> action != null)
                .collect(Collectors.toList());
    }
    
    private List<AbstractAction> assertFieldsComply(List<AutowiredFieldLocation> autowiredFieldsLocations, Mode mode) {
        return autowiredFieldsLocations.stream()
                .map( location -> assertFieldComplies(location, mode))
                .filter(action -> action != null)
                .collect(Collectors.toList());
    }

    private List<AbstractAction> nameSpringComponents(List<ClassAnnotationLocation> springComponents, Map<String, String> beanNameMap) throws IOException {
        return springComponents.stream()
                    .map(component -> assignNameToSpringComponent(component, beanNameMap))
                    .filter(action -> action != null)
                    .collect(Collectors.toList());
    }

    private List<AbstractAction> qualifyAutowiredFields(List<AutowiredFieldLocation> fields, Map<String, String> beanNameMap) throws IOException {
        return fields.stream()
                    .map(field -> assignNameToAutowiredProperty(field, beanNameMap))
                    .filter(action -> action != null)
                    .collect(Collectors.toList());
    }

    private static List<AbstractAction> unnameSpringComponents(List<ClassAnnotationLocation> springComponents) throws IOException {
        return springComponents.stream()
                    .map(component -> unnameSpringComponent(component))
                    .filter(a -> a != null)
                    .collect(Collectors.toList());
    }

    private static List<AbstractAction> unqualifyAutowiredFields(List<AutowiredFieldLocation> fields) throws IOException {
        return fields.stream()
                    .map(field -> unnameAutowiredProperty(field))
                    .filter(a -> a != null)
                    .collect(Collectors.toList());
    }
    
    private LogAction logSpringComponent(ClassAnnotationLocation location) {
        return new LogAction(location.getAnnotationName()
                            + quote(location.getAnnotationValue())
                            + " in "
                            + location.getFullClassName())
                        .setLog(log);
    }

    private LogAction logAutowiredField(AutowiredFieldLocation location) {
        return new LogAction("@Autowired"
                            + quote(location.getPropertyQualifier())
                            + " "
                            + location.getPropertyClass()
                            + " in "
                            + location.getFile().getName() )
                    .setLog(log);
    }
    
    private AbstractAction assignNameToSpringComponent(ClassAnnotationLocation location, Map<String, String> beanNameMap) {
        if (isNamed(location))
            return null;
        
        String newQualifier = beanNameMap.get(location.getShortClassName());
        
        if (newQualifier == null || newQualifier.trim().isEmpty())
            return new ErrorAction()
                            .setSourceFile(location.getFile())
                            .setMessage("name not found for component "+location.getShortClassName())
                            .setLog(log);
            
        return new SetBeanNameAction()  
                            .setSourceFile(location.getFile())
                            .setBeanName(newQualifier)
                            .setAnnotationName(location.getAnnotationName())
                            .setPosition(location.getPosition());
    }

    private AbstractAction assignNameToAutowiredProperty(AutowiredFieldLocation field, Map<String, String> beanNameMap)  {
        if (isNamed(field))
            return null;
        
        String fieldType = field.getPropertyClass();
        String newQualifier = beanNameMap.get(fieldType);
        
        if (newQualifier == null || newQualifier.trim().isEmpty())
            return new ErrorAction()
                            .setSourceFile(field.getFile())
                            .setMessage("no qualifier known for a field of type "+field.getPropertyClass())
                            .setLog(log);
        
        return new SetFieldNameAction()
                            .setPropertyName(newQualifier)
                            .setSourceFile(field.getFile())
                            .setPosition(field.getAutowiredBegin());
    }

    private static AbstractAction unnameSpringComponent(ClassAnnotationLocation componentLocation) {
        if (!isNamed(componentLocation))
            return null;
        
        return new SetBeanNameAction()  
                            .setSourceFile(componentLocation.getFile())
                            .setBeanName(null)
                            .setAnnotationName(componentLocation.getAnnotationName())
                            .setPosition(componentLocation.getPosition() );
    }

    private static AbstractAction unnameAutowiredProperty(AutowiredFieldLocation field)  {
        if (!isNamed(field))
            return null;
        
        return new ClearFieldNameAction()
                            .setSourceFile(field.getFile())
                            .setLocation(field.getQualifierBegin(), field.getQualifierEnd());
    }
    
    private AbstractAction warnComponentComplies(ClassAnnotationLocation location, Mode mode) {
        if (complies(location, mode))
            return null;
        
        return new WarningAction()
                    .setMessage((isNamed(location)?"named":"unnamed") 
                                + " component: "
                                + location.getFullClassName())
                    .setSourceFile(location.getFile())
                    .setLog(log);
    }
    
    private AbstractAction warnFieldComplies(AutowiredFieldLocation location, Mode mode) {
        if (complies(location, mode))
            return null;
        
        return new WarningAction()
                    .setMessage((isNamed(location)?"named":"unnamed")
                                + " field: "
                                + location.getPropertyClass())
                    .setSourceFile(location.getFile())
                    .setLog(log);
    }

    private AbstractAction assertComponentComplies(ClassAnnotationLocation location, Mode mode) {
        if (complies(location, mode))
            return null;
        
        return new ErrorAction()
                    .setMessage((isNamed(location)?"named":"unnamed")
                                + " component: "
                                + location.getFullClassName())
                    .setSourceFile(location.getFile())
                    .setLog(log);
    }

    private AbstractAction assertFieldComplies(AutowiredFieldLocation location, Mode mode) {
        if (complies(location, mode))
            return null;
        
        return new ErrorAction()
                    .setMessage((isNamed(location)?"named":"unnamed")
                                + " field: "
                                + location.getPropertyClass())
                    .setSourceFile(location.getFile())
                    .setLog(log);
    }

    private static String getQualifierName(Mode mode, ClassAnnotationLocation annotation) {
        if (mode == Mode.nameRandomly)
            return hash(annotation.getFullClassName());
        
        if (mode == Mode.nameByClass)
            return annotation.getShortClassName();
        
        throw new RuntimeException("mode not specified: must be either 'nameRandomly' or 'nameByClass'");
    }

    private static void saveSourceFiles(Collection<TextFile> modifiedFiles) {
        for (TextFile file: modifiedFiles) 
            file
                .save()
                .close();
    }

    private static boolean complies(AutowiredFieldLocation location, Mode mode) {
        if (mode == Mode.errorNamed || mode == Mode.warnNamed)
            return ! isNamed(location);
        
        if (mode == Mode.errorUnnamed || mode == Mode.warnUnnamed)
            return isNamed(location);
        
        return true;
    }

    private static boolean complies(ClassAnnotationLocation location, Mode mode) {
        if (mode == Mode.errorNamed || mode == Mode.warnNamed)
            return ! isNamed(location);
        
        if (mode == Mode.errorUnnamed || mode == Mode.warnUnnamed)
            return isNamed(location);
        
        return true;
    }
    
    private static boolean isNamed(ClassAnnotationLocation location) {
        return !empty(location.getAnnotationValue());
    }

    private static boolean isNamed(AutowiredFieldLocation location) {
        return !empty(location.getPropertyQualifier());
    }
    
    private static String hash(String str) {
        try {
            byte[] array = MessageDigest.getInstance("MD5").digest(str.getBytes());
            
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i)
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    private static String quote(String str) {
        if (empty(str))
            return "";
        
        return "(\""+str+"\")";
    }
    
    private static boolean empty(String value) {
        return value == null || value.trim().isEmpty();
    }

}
