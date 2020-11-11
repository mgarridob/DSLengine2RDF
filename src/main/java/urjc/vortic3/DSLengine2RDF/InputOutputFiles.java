package urjc.vortic3.DSLengine2RDF;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;

public class InputOutputFiles {
	String sourceFile = "file.csv";
	BufferedReader br = null;
	String line = "";
	//separator is defined ","
	String csvSplitBy = ",";
	
	public InputOutputFiles (String file) {
		sourceFile = file;
	}
	
	public void readFile () { //no se usa
		try {
		    br = new BufferedReader(new FileReader(sourceFile));
		    while ((line = br.readLine()) != null) {                
		    	String[] datos = line.split(csvSplitBy);
		        //prints the file data on the screen.
		    	System.out.println(datos[0] + ", " + datos[1] + ", " + datos[2] + ", " + datos[3] + ", " + datos[4] + ", " + datos[5]);
		    }
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e1) {
		    e1.printStackTrace();
		} finally {
		    if (br != null) {
		        try {
		            br.close();
		        } catch (IOException e1) {
		            e1.printStackTrace();
		        }
		    }
		}
	}
	
	public String[] readHeaderCSV () {
		String[] data = {};
		try {		    
			br = new BufferedReader(new FileReader(sourceFile));
		    line = br.readLine();             
		    data = line.split(csvSplitBy);		    
		    return data;
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e1) {
		    e1.printStackTrace();
		} finally {
		    if (br != null) {
		        try {
		            br.close();		            
		        } catch (IOException e1) {
		            e1.printStackTrace();
		        }
		    }
		}
		return data;		
	}
	
	public ArrayList<String> readFilePseudocode () {
		ArrayList<String> linesPseudocode = new ArrayList<String>(); 
		try {
		    br = new BufferedReader(new FileReader(sourceFile));
		    while ((line = br.readLine()) != null ) {  
		        //prints the file data on the screen.		    	
		    	if (!line.isEmpty() && !line.equals("\n")) {
		    		linesPseudocode.add(line.replaceAll("\\s",""));	
		    	}   			    	
		    }		    
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e1) {
		    e1.printStackTrace();
		} finally {			
		    if (br != null) {
		        try {
		            br.close();
		        } catch (IOException e1) {
		            e1.printStackTrace();
		        }
		    }
		}		
		return linesPseudocode;
	}
	
	public void writeFile(ArrayList<String> code) {
		Writer out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sourceFile), "UTF-8"));
			
			// write line to line in file
			for (String line : code) {
				try {
					out.write(line+"\n");
				} catch (IOException ex) {
					System.out.println("Error to write: " + ex.getMessage());
				}
			}
		} catch (UnsupportedEncodingException ex2) {
			System.out.println("Error unsuported encoding: " + ex2.getMessage());
		} catch (FileNotFoundException ex3) {
			System.out.println("Error file not exists: " + ex3.getMessage());		
		} finally {
			try {
				out.close();
				System.out.println("writed file");
			} catch (IOException ex4) {
				System.out.println("Error close file: " + ex4.getMessage());
			}
		}
	}
}
