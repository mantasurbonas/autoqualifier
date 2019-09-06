package lt.visma.javahub.autoqualifier;

import java.io.File;

import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import lt.visma.javahub.autoqualifier.model.AutowiredFieldLocation;


/***
 *  collects found @Autowired fields from a Java source file (together with @Qualifier() annotations). 
 *  
 * @author mantas.urbonas
 *
 */
public class AutowiredPropertiesInspector implements JavaFilesScanner.Inspector<AutowiredFieldLocation>{

	@Override
	public List<AutowiredFieldLocation> inspect(ClassOrInterfaceDeclaration classDeclaration, File javaFile) {
    	return classDeclaration
    			.getFields().stream()
				.map(f -> toLocation(f, javaFile))
				.filter(l -> l != null)
				.collect(Collectors.toList());
	}

	private static AutowiredFieldLocation toLocation(FieldDeclaration field, File javaFile) {
		if (field == null)
			return null;
		
		AnnotationExpr autowired = field.getAnnotations().stream()
				.filter(a -> isAutowiredAnnotation(a))
				.findAny()
				.orElse(null);
		
		if (autowired == null)
			return null;
		
		AnnotationExpr qualifier = field.getAnnotations().stream()
				.filter(a -> isQualifierAnnotation(a))
				.findAny()
				.orElse(null);
		
		AutowiredFieldLocation location = new AutowiredFieldLocation();
			location.setPropertyClass(field.getElementType().toClassOrInterfaceType().orElse(null).getNameAsString());
			location.setFile(javaFile);
			location.setAutowiredLocation(autowired.getBegin(), autowired.getEnd());
			
		if (qualifier != null) {
			location.setQualifierLocation(qualifier.getBegin(), qualifier.getEnd());
			location.setPropertyQualifier(valueToString(qualifier));
		}
		
		return location;
	}

	private static String valueToString(AnnotationExpr componentAnnotation) {
		List<StringLiteralExpr> literals = componentAnnotation.findAll(StringLiteralExpr.class);
		if (literals == null || literals.size() == 0)
			return null;
		
		return literals.get(0).getValue();
	}
	
	private static boolean isAutowiredAnnotation(AnnotationExpr annotation) {
		if (annotation == null)
			return false;
		
		if (annotation.getChildNodes().size() > 1)
			return false;
		
		String name = annotation.getNameAsString().trim();
		
		return name.startsWith("Autowired");
	}

	private static boolean isQualifierAnnotation(AnnotationExpr annotation) {
		if (annotation == null)
			return false;
		
		if (annotation.getChildNodes().size() < 1)
			return false;
		
		String name = annotation.getNameAsString().trim();
		
		return name.startsWith("Qualifier");
	}
}