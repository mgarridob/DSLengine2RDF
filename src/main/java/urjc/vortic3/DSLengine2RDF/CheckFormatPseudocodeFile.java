package urjc.vortic3.DSLengine2RDF;

import java.util.Stack;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckFormatPseudocodeFile {
	ArrayList<String> code = new ArrayList<String>();
	public CheckFormatPseudocodeFile (ArrayList<String> line) {
		code = line;
	}	
	public boolean checkString(String line) {
		    char arrayOfLetters[] = line.toCharArray();
		    Stack<Character> stackLetters = new Stack<Character>();
		    int i;
		    for (i = 0; i < arrayOfLetters.length; i++) {
		      if (arrayOfLetters[i] == '(') {
		        stackLetters.push(arrayOfLetters[i]);
		      }else if (arrayOfLetters[i] == ')') {
		        if (!stackLetters.empty() && stackLetters.peek() != ')') {
		        	stackLetters.pop();
		        }else{
		        	stackLetters.push(arrayOfLetters[i]);
		        }
		      }
		    }
		  return stackLetters.empty();
	}

	public static int countStringInString(String search, String text) {
	    Pattern pattern = Pattern.compile(search);
	    Matcher matcher = pattern.matcher(text);
	    
	    int stringOccurrences = 0;
	    while (matcher.find()) {
	      stringOccurrences++;
	    }
	    return stringOccurrences;
	}
	public boolean checkPatron (String text, String pat) {			
		Pattern patron = Pattern.compile(pat);
		Matcher match = patron.matcher(text);
		return match.find();
	}
	public ArrayList<String> checkPseudocode() {
		int i;		
		File file;
		ArrayList<String> errors = new ArrayList<String>();
		int countFile = 0;
		int countPrefix = 0;		
		int countSubject = 0;
		int countPredObj = 0;
		for (i=0; i < code.size(); i++) {
			if (!checkString(code.get(i))) {				
				errors.add("unbalanced parentheses in line "+(i+1));
			}	
			if (!code.get(i).startsWith("FILE") && !code.get(i).startsWith("PREFIX") && !code.get(i).startsWith("BASE") && !code.get(i).startsWith("MAPPING") && !code.get(i).startsWith("SUBJECT") && !code.get(i).startsWith("PREDICATE-OBJECT")) {
				errors.add("invalid start of sentence in line "+(i+1));
			}
			if (code.get(i).startsWith("FILE") ) {
				countFile++;
			}
			if (code.get(i).startsWith("PREFIX") ) {
				countPrefix++;
			}			
			if (code.get(i).startsWith("SUBJECT") ) {
				countSubject++;
			}
			if (code.get(i).startsWith("PREDICATE-OBJECT") ) {
				countPredObj++;
			}
			
			if (code.get(i).startsWith("FILE")) {				
				if (!checkPatron(code.get(i),"^FILE[(#]{1}[^,]+[,][^,]+[)]")) {
					errors.add("invalid sentence format command FILE in line "+(i+1));
				} else {
					file = new File(code.get(i).substring(code.get(i).indexOf(',')+1, code.get(i).indexOf(')')));
					if (!file.exists()) {
						errors.add("the " + code.get(i).substring(code.get(i).indexOf(',')+1, code.get(i).indexOf(')')) + " file does not exist in line "+(i+1));
					}
				}
			}			
			if (code.get(i).startsWith("PREFIX")) {				
				if (!checkPatron(code.get(i),"^PREFIX[(]{1}[^,]+[,][^,]+[)]")) {
					errors.add("invalid sentence format command PREFIX in line "+(i+1));
				}
			}
			
			if (code.get(i).startsWith("SUBJECT")) {				
				if (!checkPatron(code.get(i),"^SUBJECT[(][^,]+[,][^,]+[)]")) {
					errors.add("invalid sentence format command SUBJECT in line "+(i+1));
				}
			}					
			if (code.get(i).startsWith("PREDICATE-OBJECT") && (countStringInString("PREDICATE",code.get(i)))!=2) {					
				errors.add("invalid sentence format command PREDICATE-OBJECT in line "+(i+1));
			}
			if (code.get(i).startsWith("PREDICATE-OBJECT") && countStringInString("OBJECT",code.get(i))!=2) {				
				errors.add("invalid sentence format command PREDICATE-OBJECT in line "+(i+1));
			}				
		}
		if (countFile == 0) {
			errors.add("FILE command not found");
		}
		if (countPrefix == 0) {
			errors.add("PREFIX command not found");
		}		
		if (countSubject == 0) {
			errors.add("SUBJECT command not found");
		}
		if (countPredObj == 0) {
			errors.add("PREDICATE-OBJECT command not found");
		}
		
		return errors;
	}

}

