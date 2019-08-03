package packagehere;

import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;


/***
 * test here
 * @author mantas.urbonas
 *
 */

@Component()
@VeryNice
public class Kvakva {

    @Autowired
    private Blabla bla;
    
    @Autowired
    @Qualifier("some")
    private BlaBla dontQualifyMeRepeatedly;
	
    private final void someMethod(String param) {
     
    	/*** some example here */
    	
    	////
    		// 	and here
    }
}
