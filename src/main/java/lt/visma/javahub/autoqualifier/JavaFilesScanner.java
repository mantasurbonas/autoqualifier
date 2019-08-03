package lt.visma.javahub.autoqualifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.utils.SourceRoot;

public class JavaFilesScanner <T> {
	
	public static interface Inspector <M>{
		public List<M> inspect(ClassOrInterfaceDeclaration classDeclaration, File javaFile);
	}
	
	public List<T> findAll(Path rootPath, Inspector<T> analyzer) throws IOException {   	
        List<T> ret = new ArrayList<>();
        String currentPackage= "";

        SourceRoot sourceRoot = new SourceRoot(rootPath);
        
        scanRecursively(rootPath.toFile(), sourceRoot, currentPackage, analyzer, ret);
        
        return ret;
    }

	private void scanRecursively(File rootPath, SourceRoot sourceRoot, String currentPackage, Inspector<T> analyzer, List<T> results) throws IOException {
    	File javaFiles[] = rootPath.listFiles(f -> ! f.isDirectory() && f.getName().endsWith(".java"));
    	scanJavaFiles(sourceRoot, currentPackage, javaFiles, analyzer, results);

    	if (!currentPackage.isEmpty())
    		currentPackage = currentPackage + ".";
    	
    	File subfolders[] = rootPath.listFiles(f -> f.isDirectory() );
    	for (File subfolder: subfolders) 
    		scanRecursively(subfolder, sourceRoot, currentPackage+subfolder.getName().toString(), analyzer, results);
	}

	private void scanJavaFiles(SourceRoot sourceRoot, String currentPackage, File[] javaFiles, Inspector<T> analyzer, List<T> results) {
		for (File javaFile: javaFiles) {
			CompilationUnit cu = sourceRoot.parse(currentPackage, javaFile.getName());
			
		    results.addAll(cu
		    		.findAll(ClassOrInterfaceDeclaration.class)
		    		.stream()
		    		.map(c -> analyzer.inspect(c, javaFile))
		    		.flatMap(List::stream)
		    		.collect(Collectors.toList()));
		}
	}

}
