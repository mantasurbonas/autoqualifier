package lt.visma.javahub.autoqualifier;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

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
			location.setAnnotationValue(valueToString(componentAnnotation));
			location.setFile(javaFile);
			location.setPosition(componentAnnotation.getBegin().get());
			
		return Collections.singletonList(location);
	}

	private String valueToString(AnnotationExpr componentAnnotation) {
		List<StringLiteralExpr> literals = componentAnnotation.findAll(StringLiteralExpr.class);
		if (literals == null || literals.size() == 0)
			return null;
		
		return literals.get(0).getValue();
	}

	private static boolean isComponentAnnotation(AnnotationExpr annotation) {
		if (annotation == null)
			return false;
		
		String name = annotation.getNameAsString().trim();
		
		return name.equals("Component") || name.equals("Service") || name.equals("Repository");
	}

}