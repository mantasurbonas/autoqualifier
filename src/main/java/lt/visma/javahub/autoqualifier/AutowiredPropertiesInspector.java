package lt.visma.javahub.autoqualifier;

import java.io.File;

import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import lt.visma.javahub.autoqualifier.model.PropertyAnnotationLocation;


/***
 *  collects found @Autowired fields from a Java source file. Skips the ones marked with @Qualifier() annotations. 
 *  
 * @author mantas.urbonas
 *
 */
public class AutowiredPropertiesInspector implements JavaFilesScanner.Inspector<PropertyAnnotationLocation>{

	@Override
	public List<PropertyAnnotationLocation> inspect(ClassOrInterfaceDeclaration classDeclaration, File javaFile) {
    	return classDeclaration
    			.getFields().stream()
    			.filter(f -> isAutowired(f))
				.map(f -> toLocation(f, javaFile))
				.collect(Collectors.toList());
	}

	private static PropertyAnnotationLocation toLocation(FieldDeclaration field, File javaFile) {
		PropertyAnnotationLocation location = new PropertyAnnotationLocation();
			location.setPropertyClass(field.getElementType().toClassOrInterfaceType().orElse(null).getNameAsString());
			location.setFile(javaFile);
			location.setLine(field.getBegin().get().line);
			location.setColumn(field.getBegin().get().column);
			location.setAnnotationName("Autowired");
		return location;
	}

	private static boolean isAutowired(FieldDeclaration f) {
		if (f == null)
			return false;
		
		boolean hasQualifier = f.getAnnotations().stream()
				.filter(a -> isQualifierAnnotation(a))
				.findAny()
				.isPresent();
		
		if (hasQualifier)
			return false;
		
		return f.getAnnotations().stream()
				.filter(a -> isAutowiredAnnotation(a))
				.findAny()
				.isPresent();
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
		
		if (annotation.getChildNodes().size() > 1)
			return false;
		
		String name = annotation.getNameAsString().trim();
		
		return name.startsWith("Qualifier");
	}
}