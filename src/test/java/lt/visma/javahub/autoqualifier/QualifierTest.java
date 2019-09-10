package lt.visma.javahub.autoqualifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.Test;

import com.github.javaparser.utils.CodeGenerationUtils;

import lt.visma.javahub.autoqualifier.Qualifier.Mode;
import lt.visma.javahub.autoqualifier.actions.AbstractAction;
import lt.visma.javahub.autoqualifier.actions.ClearFieldNameAction;
import lt.visma.javahub.autoqualifier.actions.ErrorAction;
import lt.visma.javahub.autoqualifier.actions.LogAction;
import lt.visma.javahub.autoqualifier.actions.SetBeanNameAction;
import lt.visma.javahub.autoqualifier.actions.SetFieldNameAction;
import lt.visma.javahub.autoqualifier.actions.WarningAction;

public class QualifierTest {

    @Test
    public void shouldLogComponentsAndProperties() throws Exception{
        Path where = CodeGenerationUtils.mavenModuleRoot(Main.class).resolve("src/test/resources/kvapackage/");
        
        List<AbstractAction> actions = new Qualifier()
            .setMode(Mode.log)
            .reviewSources(where)
            .getActions();
        
        assertEquals(3, actions.size());
        
        boolean isLogActions = true;
        for (AbstractAction action: actions)
            isLogActions = isLogActions & action instanceof LogAction;

        assertTrue(isLogActions);
        
        assertEquals("Component(\"kvakva\") in kvapackage.Kvakva", ((LogAction)actions.get(0)).getMessage());
        assertEquals("@Autowired Blabla in Kvakva.java", ((LogAction)actions.get(1)).getMessage());
        assertEquals("@Autowired(\"some\") Sometype in Kvakva.java", ((LogAction)actions.get(2)).getMessage());
    }

    @Test
    public void shouldWarnUnnamedComponentsAndProperties() throws Exception{
        Path where = CodeGenerationUtils.mavenModuleRoot(Main.class).resolve("src/test/resources/");
        
        List<AbstractAction> actions = new Qualifier()
            .setMode(Mode.warnUnnamed)
            .reviewSources(where)
            .getActions();
        
        assertEquals(3, actions.size());
        
        boolean isWarningActions = true;
        for (AbstractAction action: actions)
            isWarningActions = isWarningActions & action instanceof WarningAction;

        assertTrue(isWarningActions);
        
        assertEquals("unnamed component: Blabla", ((WarningAction)actions.get(0)).getMessage());
        assertEquals("unnamed field: Kvakva", ((WarningAction)actions.get(1)).getMessage());
        assertEquals("unnamed field: Blabla", ((WarningAction)actions.get(2)).getMessage());
    }
    
    @Test
    public void shouldErrorNamedComponentsAndProperties() throws Exception{
        Path where = CodeGenerationUtils.mavenModuleRoot(Main.class).resolve("src/test/resources/");
        
        List<AbstractAction> actions = new Qualifier()
            .setMode(Mode.errorNamed)
            .reviewSources(where)
            .getActions();
        
        assertEquals(2, actions.size());
        
        boolean isWarningActions = true;
        for (AbstractAction action: actions)
            isWarningActions = isWarningActions & action instanceof ErrorAction;

        assertTrue(isWarningActions);
        
        assertEquals("named component: kvapackage.Kvakva", ((ErrorAction)actions.get(0)).getMessage());
        assertEquals("named field: Sometype", ((ErrorAction)actions.get(1)).getMessage());
    }

    @Test
    public void shouldUnnameNamedComponentsAndProperties() throws Exception{
        Path where = CodeGenerationUtils.mavenModuleRoot(Main.class).resolve("src/test/resources/");
        
        List<AbstractAction> actions = new Qualifier()
            .setMode(Mode.unname)
            .reviewSources(where)
            .getActions();
        
        assertEquals(2, actions.size());
        
        SetBeanNameAction componentUnnameAction = (SetBeanNameAction)actions.get(0);
        assertNull(componentUnnameAction.getBeanName());
        assertEquals("Kvakva.java", componentUnnameAction.getSourceFile().getName());
        assertEquals(13, componentUnnameAction.getPosition().line);
        
        ClearFieldNameAction fieldUnnameAction = (ClearFieldNameAction)actions.get(1);
        assertEquals("Kvakva.java", fieldUnnameAction.getSourceFile().getName());
        assertEquals(21, fieldUnnameAction.getBegin().line);
    }
    
    @Test
    public void shouldNameUnnamedComponentsAndProperties() throws Exception{
        Path where = CodeGenerationUtils.mavenModuleRoot(Main.class).resolve("src/test/resources/");
        
        List<AbstractAction> actions = new Qualifier()
            .setMode(Mode.nameByClass)
            .reviewSources(where)
            .getActions();
        
        assertEquals(3, actions.size());
        
        SetBeanNameAction componentNameAction = (SetBeanNameAction)actions.get(0);
        assertEquals("Blabla.java", componentNameAction.getSourceFile().getName());
        assertEquals("Blabla", componentNameAction.getBeanName());
        assertEquals(12, componentNameAction.getPosition().line);
        
        SetFieldNameAction fieldNameAction = (SetFieldNameAction)actions.get(1);
        assertEquals("Blabla.java", fieldNameAction.getSourceFile().getName());
        assertEquals("Kvakva", fieldNameAction.getPropertyName());
        assertEquals(15, fieldNameAction.getPosition().line);
        
        SetFieldNameAction fieldNameAction2 = (SetFieldNameAction)actions.get(2);
        assertEquals("Kvakva.java", fieldNameAction2.getSourceFile().getName());
        assertEquals("Blabla", fieldNameAction2.getPropertyName());
        assertEquals(17, fieldNameAction2.getPosition().line);
    }
}
