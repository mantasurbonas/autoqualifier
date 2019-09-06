package kvapackage;

import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;


/***
 * test here
 * @author mantas.urbonas
 *
 */

@Component("kvakva")
@VeryNice
public class Kvakva {

    @Autowired
    private Blabla bla;
    
    @Autowired
    @Qualifier("some")
    private Sometype dontQualifyMeRepeatedly;
	
    private final void someMethod(String param) {
     
    	/*** some example here */
    	
    	////
    	// 	and here
    }
}
