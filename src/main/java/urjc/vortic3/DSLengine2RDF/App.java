package urjc.vortic3.DSLengine2RDF;

import java.util.ArrayList;

/**
 * 
 *
 */
public class App 
{
    public static void main( String[] args )
    {    	
    	ArrayList<String> linesPseudocode = new ArrayList<String>();
    	String path = null;
    	if (args.length != 0) {
    		path = args[0];   
    	} else {    		
    		path = "pseudocode.txt";
    	}
		InputOutputFiles filePseudocode = new InputOutputFiles (path);
		linesPseudocode = filePseudocode.readFilePseudocode();		
		ArrayList<String> verifiedFile = new ArrayList<String>();
		CheckFormatPseudocodeFile checkFile = new CheckFormatPseudocodeFile(linesPseudocode);
		verifiedFile = checkFile.checkPseudocode();
		ArrayList<String> builtFile = new ArrayList<String>();
		
		if (verifiedFile.size() == 0) {
			System.out.println();
			System.out.println("OK!");
			System.out.println();
			BuildRDF constructor = new BuildRDF(linesPseudocode);
			builtFile = constructor.generateRDF();
			if (builtFile.size() > 0) {
				System.out.println("("+ builtFile.size() + ")" + " Build errors: ");
				for(int i = 0; i < builtFile.size(); i++) {
				     System.out.println(builtFile.get(i));
				}
			} 
		} else {
			System.out.println("("+verifiedFile.size() + ")" + " Format errors: ");
			for(int i = 0; i < verifiedFile.size(); i++) {
			     System.out.println(verifiedFile.get(i));
			}
		}		
    }
}
