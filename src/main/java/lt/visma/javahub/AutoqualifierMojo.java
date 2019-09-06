package lt.visma.javahub;


import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import lt.visma.javahub.autoqualifier.Qualifier;

@Mojo( name = "autoqualifier", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class AutoqualifierMojo extends AbstractMojo {
	
    /**
     * Location of sources directory.
     */
    @Parameter( defaultValue = "${project.build.sourceDirectory}", property = "srcDir")
    private File srcDirectory;

    /***
     * the mode: see Qualifier.Mode for available options
     */
    @Parameter( defaultValue = "log", required = true)
    private Qualifier.Mode mode;
    
    public void execute() throws MojoExecutionException{

        if ( srcDirectory == null || !srcDirectory.exists() )
            return;

		try {
			new Qualifier()
				.setMode(mode)
				.setLog(getLog())
				.reviewSources(srcDirectory)
				.executeActions();
		} catch (Exception e) {
			e.printStackTrace();
			throw new MojoExecutionException(e.getMessage());
		}
    }
}
