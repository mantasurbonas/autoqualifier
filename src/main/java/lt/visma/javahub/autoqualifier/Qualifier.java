package lt.visma.javahub.autoqualifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lt.visma.javahub.autoqualifier.model.ClassAnnotationLocation;
import lt.visma.javahub.autoqualifier.model.PropertyAnnotationLocation;
import lt.visma.javahub.utils.TextFile;

public class Qualifier {

	public enum Mode{
		classname,
		random
	}
	
	private Mode mode = Mode.random;

	private Map<String, TextFile> modifiedFiles = null;
	
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

	public static List<ClassAnnotationLocation> findSpringComponents(Path rootPath) throws IOException {
		return new JavaFilesScanner<ClassAnnotationLocation>().findAll(rootPath, new SpringComponentInspector());
	}
	
	public static List<PropertyAnnotationLocation> findAutowiredFields(Path rootPath) throws IOException {
		return new JavaFilesScanner<PropertyAnnotationLocation>().findAll(rootPath, new AutowiredPropertiesInspector());
	}

	public Qualifier reviewSources(String rootPath) throws IOException {
		return reviewSources(Paths.get(rootPath));
	}
	
	public Qualifier reviewSources(Path rootPath) throws IOException {
		List<ClassAnnotationLocation> springComponentLocations = findSpringComponents(rootPath);

		System.out.println("these Spring components were found: ");
		for(ClassAnnotationLocation location: springComponentLocations)
			System.out.println(location);
		
		System.out.println("bean name map created: ");
		Map<String, String> beanNameMap = createNameMap(springComponentLocations);
		System.out.println(beanNameMap);
		
		List<PropertyAnnotationLocation> autowiredFieldsLocations = findAutowiredFields(rootPath);
		
		System.out.println("autowired fields were found: ");
		for(PropertyAnnotationLocation location: autowiredFieldsLocations)
			System.out.println(location);
		
		this.modifiedFiles = new HashMap<>();
		
		nameSpringComponents(springComponentLocations, beanNameMap);
		
		qualifyAutowiredFields(autowiredFieldsLocations, beanNameMap);
		
		saveSourceFiles();
		
		return this;
	}

	private void nameSpringComponents(List<ClassAnnotationLocation> springComponents, Map<String, String> beanNameMap) throws IOException {
		for(ClassAnnotationLocation componentLocation: springComponents) {
			File componentFile = componentLocation.getFile();
			String newQualifier = beanNameMap.get(componentLocation.getShortClassName());
			if (newQualifier == null || newQualifier.isEmpty()) {
				System.out.println("Warning: name not found for component "+componentLocation);
				return;
			}
				
			String newAnnotation = "@" + componentLocation.getAnnotationName() + "(\"" + newQualifier + "\")";
			
			getSourceFile(componentFile)
				.replaceLine(componentLocation.getLine()-1, newAnnotation);
		}
	}

	private void qualifyAutowiredFields(List<PropertyAnnotationLocation> fields, Map<String, String> beanNameMap) throws IOException {
		for(PropertyAnnotationLocation field: fields) {
			
			String fieldType = field.getPropertyClass();
			String newQualifier = beanNameMap.get(fieldType);
			if (newQualifier == null || newQualifier.trim().isEmpty()) {
				System.out.println("Warning: no qualifier known for field "+field);
				continue;
			}
			
			getSourceFile(field.getFile())
				.appendText(field.getLine()-1, " @Qualifier(\""+newQualifier+"\")");
		}
	}

	private Map<String, String> createNameMap(List<ClassAnnotationLocation> springComponentLocations) {
		if (mode == Mode.random)
			return springComponentLocations.stream()
				.collect( Collectors.toMap ( l -> l.getShortClassName(), 
										     l -> hash(l.getFullClassName())) );
		
		if (mode == Mode.classname)
			return springComponentLocations.stream()
					.collect( Collectors.toMap ( l -> l.getShortClassName(), 
											     l -> l.getShortClassName()) );
		
		throw new RuntimeException("mode not specified: must be either 'random' or 'classname'");
	}

	private TextFile getSourceFile(File file) throws IOException {
		String key = file.getAbsolutePath();
		
		TextFile src = modifiedFiles.get(key);
		if (src == null) {
			src = new TextFile(file);
			modifiedFiles.put(key, src);
		}
		
		return src;
	}

	private void saveSourceFiles() {
		for (Map.Entry<String, TextFile> file: modifiedFiles.entrySet()) {
			
			System.out.println("saving file "+file.getKey());
			
			file.getValue().save().close();
		}
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
	
}
