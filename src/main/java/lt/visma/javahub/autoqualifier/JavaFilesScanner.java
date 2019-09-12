package lt.visma.javahub.autoqualifier;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.utils.SourceRoot;

/***
 * scans a files in a given root package, invokes a given analyzer on each Java class source found.
 * @author mantas.urbonas
 */
public class JavaFilesScanner {
    
    private SourceRoot sourceRoot;
    private File rootPath;
    
    private static final FileFilter JAVA_FILES_FILTER = file -> ! file.isDirectory() && file.getName().endsWith(".java");
    private static final FileFilter DIRECTORIES_FILTER = file -> file.isDirectory();

    public static interface Inspector <M>{
        public List<M> inspect(ClassOrInterfaceDeclaration classDeclaration, File javaFile);
    }
    
    public JavaFilesScanner (Path rootPath){
        this.sourceRoot = new SourceRoot(rootPath);
        this.rootPath = rootPath.toFile();
    }

    public <T> List<T> findAll(Inspector<T> analyzer) {      
        List<T> ret = new ArrayList<>();
        String currentPackage= "";
        
        scanRecursively(rootPath, currentPackage, analyzer, ret);
        
        return ret;
    }
    
    private <T> void scanRecursively(File rootPath, String currentPackage, Inspector<T> analyzer, List<T> results) {
		File javaFiles[] = rootPath.listFiles(JAVA_FILES_FILTER);
        
        if (javaFiles == null)
        	return; // throw new RuntimeException("not a directory "+rootPath);
        
        scanJavaFiles(currentPackage, javaFiles, analyzer, results);

        if (!currentPackage.isEmpty())
            currentPackage = currentPackage + ".";
        
		File subfolders[] = rootPath.listFiles(DIRECTORIES_FILTER );
        for (File subfolder: subfolders) 
            scanRecursively(subfolder, currentPackage+subfolder.getName(), analyzer, results);
    }

    private <T> void scanJavaFiles(String currentPackage, File[] javaFiles, Inspector<T> analyzer, List<T> results) {
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
