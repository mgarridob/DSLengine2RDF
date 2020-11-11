package urjc.vortic3.DSLengine2RDF;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

public class GenericFunctions {
	
	public static String changeValueEnum (String value, String values, String correpondences) {
		String[] listValue = value.split(",");
		String[] listValues = values.split(",");
		String[] listCorrespondences = correpondences.split(",");
		ArrayList<String> data = new ArrayList<String>();
		for (int j = 0; j < listValue.length; j++ ) {	
			for (int i = 0; i < listValues.length; i++ ) {			
				if (listValue[j].equals(listValues[i]) ) {
					listValue[j] = listCorrespondences[i];
					data.add(listValue[j]);
				}
			}
		}		
		return convertToString(data);
	}
	
	public static String getFieldQuery (String value, String joinsValues, String returnField) {		
		String[] listJoinsValues = joinsValues.split(",");
		int last = listJoinsValues.length-1;
		int i = 0;
		Set<String> result = new HashSet<String>();
		result.add(value);		
		ArrayList<String> list = new ArrayList<String>();		
		String getValue;
		while (i < listJoinsValues.length) {
			if (last == i+3) {
				getValue = returnField;
			} else {
				getValue = listJoinsValues[i+5];
			}			
			result = readFile(result,listJoinsValues[i+2],listJoinsValues[i+3],getValue);			
			i = i+4;
			list.clear();
			list.addAll(result);					
		}		
		if (list.isEmpty()) {
			return "";
		} else {			
			return convertToString(list);			
		}
		
	}	
	
	public static String getFieldQueryEnum (String value, String joinsValues, String returnField, String values, String correpondences) {
		String res = getFieldQuery(value,joinsValues,returnField);		
		return changeValueEnum(res,values,correpondences);
	}
	
	public static Set<String> readFile (Set<String> values, String file, String field, String obtField) {
		BufferedReader br = null;
		String line = "";		
		String csvSplitBy = ",";
		int numField = -1;
		int numObt=0;				
		ArrayList<String> list = new ArrayList<String>();			
		list.clear();
		list.addAll(values);
		Set<String> result = new HashSet<String>();			
		try {
		    br = new BufferedReader(new FileReader(file));
		    if (list.size() > 0) {
		    	while ((line = br.readLine()) != null) {  
		    		ArrayList<String> strCommas = getFieldsCSV(line, csvSplitBy);				    	
			    	String[] data = new String[strCommas.size()];
			    	for (int y=0; y < strCommas.size(); y++) {
			    		data[y] = strCommas.get(y);
			    	}			    	
			    	if (numField == -1) {		    		
			    		for (int i = 0; i < data.length; i++) {
			    			if (data[i].equals(field)) {
			    				numField = i;		    				
			    			}
			    			if (data[i].equals(obtField)) {
			    				numObt = i;			    				
			    			}
			    		}
			    	}		    	
			    	for (int k = 0; k < list.size(); k++ ) {		    		
			    		if (list.get(k).equals(data[numField])) {
				    		result.add(data[numObt]);			    		
				    	}
			    	}		    	
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
		return result;
	}
	
	public static String getWheelchair (String valueWheelchair, String locationType, String parentStation, String file) {
		
		String res = new String();
		ArrayList<String> list = new ArrayList<String>();	
		Set<String> valueW = new HashSet<String>();	
		if (valueWheelchair.equals("1") || valueWheelchair.equals("2")) {
			return valueWheelchair;
		} else {
			if (locationType.equals("0") || locationType.equals("2") || locationType == null) {				
				valueW.add(parentStation);
				valueW = readFile(valueW,file,"stop_id","wheelchair_boarding");
				list.clear();
				list.addAll(valueW);
				res = convertToString(list);
				if (list.isEmpty()) {
					return ""; //UNKNOWN
				} else {
					return res;
				}
			} else {
				return "";//UNKNOWN
			}
		}
		
	}

	public static ArrayList<String> readFileSort (Set<String> values, String file, String field, String obtField) {
		BufferedReader br = null;
		String line = "";		
		String csvSplitBy = ",";		
		int numField = -1;
		int numObt=0;				
		ArrayList<String> list = new ArrayList<String>();			
		list.clear();
		list.addAll(values);		
		Set<String> result = new HashSet<String>();	
		TreeMap<Integer, String> tm= new TreeMap<Integer, String>();
		ArrayList<String> resul2 = new ArrayList<String>();			
		try {
		    br = new BufferedReader(new FileReader(file));
		    if (list.size() > 0) {
		    	while ((line = br.readLine()) != null) { 
		    		ArrayList<String> strCommas = getFieldsCSV(line, csvSplitBy);				    	
			    	String[] data = new String[strCommas.size()];
			    	for (int y=0; y < strCommas.size(); y++) {
			    		data[y] = strCommas.get(y);
			    	}
			    	//data = line.split(csvSplitBy);
			    	if (numField == -1) {		    		
			    		for (int i = 0; i < data.length; i++) {
			    			if (data[i].equals(field)) {
			    				numField = i;		    				
			    			}
			    			if (data[i].equals(obtField)) {
			    				numObt = i;			    				
			    			}
			    		}
			    	}		    	
			    	for (int k = 0; k < list.size(); k++ ) {		    		
			    		if (list.get(k).equals(data[numField])) {
				    		tm.put(Integer.parseInt(data[numObt]),data[numField]);	    						    		
				    	}
			    	}		    	
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
		for( Iterator<Integer> it2 = tm.keySet().iterator(); it2.hasNext();) { 
		    Integer s = (Integer)it2.next();
		    String s1 = (String)tm.get(s);		    
		    result.add(s1);
		    resul2.add(s1);
		}	
		
		return resul2;
	}
	
	public static String getSequenceline (String valueRoute_id, String fileRoutes, String fileTrips, String fileStopTimes) {
		Set<String> result = new HashSet<String>();
		ArrayList<String> result2 = new ArrayList<String>();		
		result.add(valueRoute_id);		
		result = readFile(result,fileTrips,"route_id","trip_id");		
		result = readFile(result,fileStopTimes,"trip_id","stop_id");		
		result2 = readFileSort(result,fileStopTimes,"stop_id","stop_sequence");		
		
		return convertToString(result2); 
		
	}
	
	public static String convertToString (ArrayList<String> data) {
		String output = new String();
		for (int i = 0; i < data.size(); i++) {
			output += data.get(i) + ",";
		}
		if (data.size() > 0) {
			output = output.substring(0, output.length()-1);
		}
		
		return output;
	}
	
	
	public static ArrayList<String> getFieldsCSV (String cadena, String caracter) {
	        int posicion, contador = 0, posBefore = 0;	        
	        posicion = cadena.indexOf(caracter);
	        ArrayList<String> strArray = new ArrayList<String>();
	        while (posicion != -1) { 
	            contador++;         	            
	            if (contador == 1) {	            	
	            	strArray.add(cadena.substring(0, posicion));
	            } else if (posicion < cadena.length()) {	            	
	            	strArray.add(cadena.substring(posBefore,posicion));
	            } 
	            posBefore = posicion+1;	            
	            posicion = cadena.indexOf(caracter, posicion + 1);
	            if (posicion == -1 && posBefore-1 == cadena.length()-1) {	            	
	            	strArray.add(cadena.substring(posBefore-1,cadena.length()-1));
	            } 
	            if (posicion == -1 && posBefore-1 != cadena.length()-1) {
	            	strArray.add(cadena.substring(posBefore,cadena.length()));
	            }	            
	        }
	        return strArray;
	}

}
