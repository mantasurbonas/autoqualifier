package lt.visma.javahub.autoqualifier;

import java.nio.file.Path;

import com.github.javaparser.utils.CodeGenerationUtils;

import lt.visma.javahub.autoqualifier.Qualifier.Mode;

public class Main {

	public static void main(String []s) throws Exception {
		Path where = CodeGenerationUtils.mavenModuleRoot(Main.class).resolve("src/test/resources");
		// Paths.get("C:\\work\\src\\github\\objectFlowsSample\\src\\main\\java");
		
		new Qualifier().setMode(Mode.random).reviewSources(where);
	}
	
}
