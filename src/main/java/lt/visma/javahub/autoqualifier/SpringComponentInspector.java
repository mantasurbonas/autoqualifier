package lt.visma.javahub.autoqualifier;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import lt.visma.javahub.autoqualifier.model.ClassAnnotationLocation;

/***
 *  collects unnamed @Component @Repository or @Service Spring components. 
 *  
 * @author mantas.urbonas
 *
 */
public class SpringComponentInspector implements JavaFilesScanner.Inspector<ClassAnnotationLocation>{

	@Override
	public List<ClassAnnotationLocation> inspect(ClassOrInterfaceDeclaration classDeclaration, File javaFile) {
    	AnnotationExpr componentAnnotation = classDeclaration
				.getAnnotations().stream()
				.filter(a -> isComponentAnnotation(a))
				.findAny()
				.orElse(null);

    	if (componentAnnotation == null)
    		return Collections.emptyList();
    	
		ClassAnnotationLocation location = new ClassAnnotationLocation();
			location.setShortClassName(classDeclaration.getNameAsString());
			location.setFullClassName(classDeclaration.getFullyQualifiedName().orElse(null));
			location.setAnnotationName(componentAnnotation.getNameAsString());
			location.setFile(javaFile);
			location.setLine(componentAnnotation.getBegin().get().line);
			
		return Collections.singletonList(location);
	}

	private static boolean isComponentAnnotation(AnnotationExpr annotation) {
		if (annotation == null)
			return false;
		
		if (annotation.getChildNodes().size() > 1)
			return false; // already has name
		
		String name = annotation.getNameAsString().trim();
		
		return name.startsWith("Component") || name.startsWith("Service") || name.startsWith("Repository");
	}

}