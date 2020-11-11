package urjc.vortic3.DSLengine2RDF;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.*;

public class BuildRDF {
	
	ArrayList<String> code;	
	
	public BuildRDF (ArrayList<String> pseudocode) {
		code = pseudocode;
	}
	
	public ArrayList<String> generateRDF() {	
		String str, strJoin, strJoin1, strJoin2,returnValue;
		String param = new String();
		String param2 = new String();
		String param3 = new String();
		String prefixURI = new String();
		String aliasFile = new String();
		String prefixPredicate = new String();
		String prefixObj = new String();
		String aliasFileParam = new String();
		int i,j;		
		ArrayList<ArrayList<String>> commands = new ArrayList<ArrayList<String>>();		
		ArrayList<String> paramsComm = new ArrayList<String>();
		ArrayList<String> errors = new ArrayList<String>();		
		Hashtable<String,String> sources = new Hashtable<String,String>();
		Hashtable<String,String> prefixes = new Hashtable<String,String>();
		Hashtable<String,String[]> files = new Hashtable<String,String[]>();
		String varFile = new String();
		String values = new String();
		String enums = new String();
		String path = new String();
		String alias;
		String url;	
		String typeContainer = new String();
		boolean contains = false;
		String field;
		String nameClass;
		String predicate,object,nameProperty;		
		int numberMatchs;
		BufferedReader br = null;
		String line = "";		
		String csvSplitBy = ",";
		Model m = ModelFactory.createDefaultModel();
		ArrayList<Integer> posSubjects = new ArrayList<Integer>();		
		int initSubjects = 0;
		int endSubjects = 0;
		int contS = -1;
		
		for (i = 0; i < code.size(); i++) {
			if (code.get(i).startsWith("FILE")) {
				varFile = code.get(i).substring(code.get(i).indexOf("(")+1, code.get(i).indexOf(","));
				path = code.get(i).substring(code.get(i).indexOf(",")+1, code.get(i).indexOf(")"));
				sources.put(varFile,path);
				InputOutputFiles fileCSV = new InputOutputFiles (path);
				String[] headers = fileCSV.readHeaderCSV();	
				files.put(varFile, headers);				
			}
			if (code.get(i).startsWith("PREFIX")) {
				alias = code.get(i).substring(code.get(i).indexOf("(")+1, code.get(i).indexOf(","));
				url = code.get(i).substring(code.get(i).indexOf(",")+1, code.get(i).indexOf(")"));							
				prefixes.put(alias,url);
				m.setNsPrefix( alias, url );
			}			
			if (code.get(i).startsWith("SUBJECT")) {				
				aliasFile = code.get(i).substring(code.get(i).indexOf("#"), code.get(i).indexOf("."));	
				field = code.get(i).substring(code.get(i).indexOf(".")+1, code.get(i).indexOf(","));
				prefixURI = code.get(i).substring(code.get(i).indexOf(",")+1, code.get(i).indexOf(":"));								
				nameClass = code.get(i).substring(code.get(i).indexOf(":")+1, code.get(i).indexOf(")"));
				contS++;
				posSubjects.add(contS);
				paramsComm = setSubject(aliasFile,field,prefixURI,nameClass);							
				commands.add(paramsComm);
				contains = checkReference(files,aliasFile,field);								
				if (!contains) {
					errors.add(" field not exists in line "+(i+1));						
				}	
				if (!prefixes.containsKey(prefixURI)) {
					errors.add(" prefix not exists in line "+(i+1));				
				}									
			} 
			if (code.get(i).startsWith("PREDICATE-OBJECT")) { 
				contS++;
				predicate = code.get(i).substring(code.get(i).indexOf("(")+11, code.get(i).indexOf(")"));				
				prefixPredicate = predicate.substring(0,predicate.indexOf(":"));
				nameProperty = predicate.substring(predicate.indexOf(":")+1,predicate.length());				
				object = code.get(i).substring(code.get(i).indexOf(",")+8,code.get(i).length()-2);
				if (!prefixes.containsKey(prefixPredicate)) {
					errors.add(" prefix not exists in line "+(i+1));					
				}						
				if (object.contains("VALUES") && object.contains("ENUMS") && !object.startsWith("QUERY")) {	
					aliasFile = object.substring(object.indexOf("#"), object.indexOf("."));
					field = object.substring(object.indexOf(".")+1,object.indexOf(","));
					values = object.substring(object.indexOf("VALUES(")+7,object.indexOf("),ENUMS"));
					enums = object.substring(object.indexOf("ENUMS(")+6,object.length()-1);					
					contains = checkReference(files,aliasFile,field);								
					if (!contains) {
						errors.add(" field not exists in line "+(i+1));						
					}
					if (field.equals("wheelchair_boarding")) {
						paramsComm = setWheelchairBoarding (prefixPredicate, nameProperty, aliasFile, field, "location_type", "parent_station", values, enums);									
					} else {
						paramsComm = setEnum (prefixPredicate, nameProperty, aliasFile, field, values, enums);						
					}
					commands.add(paramsComm);				
				} else if (object.startsWith("QUERY")) { //QUERY 						
					numberMatchs = numberMatchesString("MATCH",object);
					str = object.substring(6,object.length());	
					param2 = "";
					for (j=1; j <= numberMatchs; j++) {						
						strJoin = str.substring(0, str.indexOf(")")+1);												
						strJoin1 = strJoin.substring(6, str.indexOf(","));						
						varFile = strJoin1.substring(0,strJoin1.indexOf("."));
						field = strJoin1.substring(strJoin1.indexOf(".")+1,strJoin1.length());						
						contains = checkReference(files,varFile,field);		
						if (!contains) {
							errors.add(" field not exists in line "+(i+1));						
						}
						if (j == 1) {
							aliasFileParam = varFile;
							param = field;
						}
						param2 += sources.get(varFile).toString()+","+field+",";										
						strJoin2 = strJoin.substring(strJoin.indexOf(",")+1,strJoin.indexOf(")"));						
						varFile = strJoin2.substring(0,strJoin2.indexOf("."));
						field = strJoin2.substring(strJoin2.indexOf(".")+1,strJoin2.length());
						contains = checkReference(files,varFile,field);		
						if (!contains) {
							errors.add(" field not exists in line "+(i+1));						
						}						
						str = str.substring(strJoin.indexOf(")")+2, str.length());	
						param2 +=sources.get(varFile).toString()+","+field+",";
					}
					if (object.contains("VALUES") && object.contains("ENUMS")) {
						if (str.contains("CONTAINER")) {
							typeContainer = str.substring(str.indexOf("(")+1,str.indexOf(","));
							if (!typeContainer.equals("SEQ") && !typeContainer.equals("ALT") && !typeContainer.equals("BAG") ) {
								errors.add(" container's commands  not found in line "+(i+1));
							}
							returnValue = str.substring(str.indexOf(",")+1,str.indexOf(")"));
							str = str.substring(str.indexOf("),")+1,str.length()-1);	
						} else {
							returnValue = str.substring(0,str.indexOf(","));
							str = str.substring(str.indexOf(",")+1,str.length()-1);							
						}
						values = str.substring(str.indexOf("VALUES(")+7,str.indexOf("),ENUMS"));
						enums = str.substring(str.indexOf("ENUMS(")+6,str.length()-1);
					} else if (object.contains(":")) {
						if (str.contains("CONTAINER")) {
							typeContainer = str.substring(str.indexOf("(")+1,str.indexOf(","));
							if (!typeContainer.equals("SEQ") && !typeContainer.equals("ALT") && !typeContainer.equals("BAG") ) {
								errors.add(" container's commands  not found in line "+(i+1));
							}
							prefixURI = str.substring(str.indexOf(",")+1,str.indexOf(":"));
						} else {
							prefixURI = str.substring(0,str.indexOf(":")); 
						}													
						if (!prefixes.containsKey(prefixURI)) {
							errors.add(" prefix not exists in line "+(i+1));
						}
						returnValue = str.substring(str.indexOf(":")+1,str.indexOf(")"));	
					} else {
						if (str.contains("CONTAINER")) {
							typeContainer = str.substring(str.indexOf("(")+1,str.indexOf(","));
							if (!typeContainer.equals("SEQ") && !typeContainer.equals("ALT") && !typeContainer.equals("BAG") ) {
								errors.add(" container's commands  not found in line "+(i+1));
							}
							returnValue = str.substring(str.indexOf(",")+1,str.indexOf(")"));
						} else {
							returnValue = str.substring(0,str.indexOf(")"));
						}
						
					}
					varFile = returnValue.substring(0,returnValue.indexOf("."));						
					field = returnValue.substring(returnValue.indexOf(".")+1,returnValue.length());					
					contains = checkReference(files,varFile,field);	
					if (!contains) {
						errors.add(" field not exists in line "+(i+1));						
					}	
					param2 = param2.substring(0, param2.length()-1);
					param3 = field; 
																	
					if (object.contains("VALUES") && object.contains("ENUMS")) {	
						if (object.contains("CONTAINER")) {
							paramsComm = setQueryEnumContainer(prefixPredicate, nameProperty, aliasFileParam, param, param2, param3, values, enums, typeContainer);
						} else {
							paramsComm = setQueryEnum(prefixPredicate, nameProperty, aliasFileParam, param, param2, param3, values, enums);	
						}
													
						commands.add(paramsComm);
					} else if (object.contains(":")) {	
						if (str.contains("CONTAINER")) {
							paramsComm = setQueryURIContainer(prefixPredicate, nameProperty, aliasFileParam, param, param2, param3, prefixes.get(prefixURI),typeContainer);														
						} else {
							paramsComm = setQueryURI(prefixPredicate, nameProperty, aliasFileParam, param, param2, param3, prefixes.get(prefixURI));							
						}	
						commands.add(paramsComm);
					} else {
						if (str.contains("CONTAINER")) {
							paramsComm = setQueryLiteralContainer(prefixPredicate, nameProperty, aliasFileParam, param, param2, param3,typeContainer);
						} else {
							paramsComm = setQueryLiteral(prefixPredicate, nameProperty, aliasFileParam, param, param2, param3);
						}
						commands.add(paramsComm);
					}												
				} else {   //constant  true	field		
					if (object.contains(":#") && object.contains("CONTAINER")) {  //CONTAINER+URI+REFERENCE																
						typeContainer = object.substring(object.indexOf("(")+1,object.indexOf(","));
						if (!typeContainer.equals("SEQ") && !typeContainer.equals("ALT") && !typeContainer.equals("BAG") ) {
							errors.add(" container's commands  not found in line "+(i+1));
						} else {
							prefixObj = object.substring(object.indexOf(",")+1,object.indexOf(":"));						
							varFile = object.substring(object.indexOf("#"),object.indexOf("."));																						
							field = object.substring(object.indexOf(".")+1,object.length()-1);
							contains = checkReference(files,varFile,field);								
							if (!contains) {
								errors.add(" field not exists in line "+(i+1));						
							}		
							if (!prefixes.containsKey(prefixObj)) {
								errors.add(" prefix not exists in line "+(i+1));	
							} 
							if (contains && prefixes.containsKey(prefixObj) && object.contains("#routes.route_id")) {
								paramsComm = setContainerRefURIsequence(prefixPredicate, nameProperty, typeContainer, prefixObj, varFile, field);
								commands.add(paramsComm);
							}
						}																	
					} else	if (object.contains(":#")) {  //URI+REFERENCE
						prefixObj = object.substring(0,object.indexOf(":"));						
						varFile = object.substring(object.indexOf("#"),object.indexOf("."));																						
						field = object.substring(object.indexOf(".")+1,object.length());
						contains = checkReference(files,varFile,field);		
						if (!contains) {
							errors.add(" field not exists in line "+(i+1));						
						}		
						if (!prefixes.containsKey(prefixObj)) {
							errors.add(" prefix not exists in line "+(i+1));	
						}						
						paramsComm = setRefURI(prefixPredicate, nameProperty, prefixObj, varFile, field);												
						commands.add(paramsComm);
					} else if (object.contains("#") && object.contains("CONTAINER")) {  //CONTAINER+REFERENCE																
							typeContainer = object.substring(object.indexOf("(")+1,object.indexOf(","));
							if (object.equals("SEQ") || object.equals("ALT") || object.equals("BAG") ) {
								errors.add(" container's commands  not found in line "+(i+1));
							} else {														
								varFile = object.substring(object.indexOf("#"),object.indexOf("."));																						
								field = object.substring(object.indexOf(".")+1,object.length()-1);
								contains = checkReference(files,varFile,field);								
								if (!contains) {
									errors.add(" field not exists in line "+(i+1));						
								}		
								if (!prefixes.containsKey(prefixObj)) {
									errors.add(" prefix not exists in line "+(i+1));	
								} 
								if (contains && prefixes.containsKey(prefixObj) && object.contains("#routes.route_id")) {
									paramsComm = setContainerRefsequence(prefixPredicate, nameProperty, typeContainer, varFile, field);
									commands.add(paramsComm);
								}
							}		
					} else if (object.contains("#")){ //REFERENCE
						varFile = object.substring(object.indexOf("#"),object.indexOf("."));						
						field = object.substring(object.indexOf(".")+1,object.length());
						contains = checkReference(files,varFile,field);		
						if (!contains) {
							errors.add(" field not exists in line "+(i+1));						
						}	
						paramsComm = setRefLit ( prefixPredicate, nameProperty, varFile, field);						
						commands.add(paramsComm);
					} else if (object.contains(":")) { //URI+LITERAL
						prefixObj = object.substring(0,object.indexOf(":"));		
						field = object.substring(object.indexOf(":")+1,object.length());
						if (!prefixes.containsKey(prefixObj)) {
							errors.add(" prefix not exists in line "+(i+1));	
						} 
						paramsComm = setURILiteral ( prefixPredicate, nameProperty, prefixObj, field);						
						commands.add(paramsComm);
					} else { //LITERAL
						paramsComm = setLiteral ( prefixPredicate, nameProperty, object);						
						commands.add(paramsComm);
					}															
				}
			}									
		}	

		if (errors.size()==0)	{			
			for (int cont = 0; cont < posSubjects.size(); cont++) {
				initSubjects = posSubjects.get(cont);				
				if (posSubjects.size() == 1 || cont + 1 == posSubjects.size()) {
					endSubjects = commands.size()-1;
				} else {					
					endSubjects = posSubjects.get(cont+1)-1;					
				}
				Resource root = null, obj = null;
				Property pred = null;
				String strObj = null;
				Literal objLit = null;
				try {
					
				    br = new BufferedReader(new FileReader(sources.get(commands.get(initSubjects).get(1))));
				    br.readLine();				    
				    while ((line = br.readLine()) != null) { 
				    	ArrayList<String> strCommas = GenericFunctions.getFieldsCSV(line, csvSplitBy);				    	
				    	String[] datos = new String[strCommas.size()];
				    	for (int y=0; y < strCommas.size(); y++) {
				    		datos[y] = strCommas.get(y);
				    	}					    	
				    	for (int c = initSubjects; c <= endSubjects; c++) {					    		
				    		if (commands.get(c).get(0).equals("S")) {				    			
				    			root = m.createResource( prefixes.get(commands.get(c).get(3)) + datos[checkReferencePos(files,commands.get(c).get(1),commands.get(c).get(2))] );
				    			obj = m.createResource(prefixes.get(commands.get(c).get(3))+commands.get(c).get(4));
				    			m.add( root, RDF.type, obj);
				    		} else if (commands.get(c).get(0).equals("PO-RefLit")) {				    			
				    			pred = m.createProperty( prefixes.get(commands.get(c).get(1)) + commands.get(c).get(2));				    			
				    			objLit = m.createLiteral(datos[checkReferencePos(files,commands.get(c).get(3),commands.get(c).get(4))]);				    			
				    			m.add(root, pred, objLit);
				    		} else if (commands.get(c).get(0).equals("PO-ContainerRefURIsequence")) {				    			
				    			pred = m.createProperty( prefixes.get(commands.get(c).get(1)) + commands.get(c).get(2));				    			
				    			strObj = GenericFunctions.getSequenceline(datos[checkReferencePos(files,commands.get(c).get(5),commands.get(c).get(6))], sources.get(commands.get(c).get(5)), sources.get("#trips"), sources.get("#stop_times"));				    			
				    			if (strObj.length() > 0) {
				    				String[] listResults = strObj.split(",");
				    				if (commands.get(c).get(3).equals("SEQ")) {
				    					Seq results = m.createSeq();
				    					for (int a = 0; a < listResults.length; a++) {				    					
					    					Resource rs = m.createResource(prefixes.get(commands.get(c).get(4))+listResults[a]);				    					
					    					results.add(rs);
					    				}				    				
					    				m.add(root, pred, results);
				    				} else if (commands.get(c).get(3).equals("BAG")) {
				    					Bag results = m.createBag();
				    					for (int a = 0; a < listResults.length; a++) {				    					
					    					Resource rs = m.createResource(prefixes.get(commands.get(c).get(4))+listResults[a]);				    					
					    					results.add(rs);
					    				}				    				
					    				m.add(root, pred, results);
				    				} else {
				    					Alt results = m.createAlt();
				    					for (int a = 0; a < listResults.length; a++) {				    					
					    					Resource rs = m.createResource(prefixes.get(commands.get(c).get(4))+listResults[a]);				    					
					    					results.add(rs);
					    				}				    				
					    				m.add(root, pred, results);
				    				}				    								    					    				
				    			} else {
				    				Resource rs = m.createResource();
				    				m.add(root, pred, rs);
				    			} 
				    		} else if (commands.get(c).get(0).equals("PO-ContainerRefsequence")) {				    			
				    			pred = m.createProperty( prefixes.get(commands.get(c).get(1)) + commands.get(c).get(2));				    			
				    			strObj = GenericFunctions.getSequenceline(datos[checkReferencePos(files,commands.get(c).get(4),commands.get(c).get(5))], sources.get(commands.get(c).get(4)), sources.get("#trips"), sources.get("#stop_times"));				    			
				    			if (strObj.length() > 0) {
				    				String[] listResults = strObj.split(",");
				    				if (commands.get(c).get(3).equals("SEQ")) {
				    					Seq results = m.createSeq();
				    					for (int a = 0; a < listResults.length; a++) {				    					
					    					Literal lit = m.createLiteral(listResults[a]);				    					
					    					results.add(lit);
					    				}				    				
					    				m.add(root, pred, results);
				    				} else if (commands.get(c).get(3).equals("BAG")) {
				    					Bag results = m.createBag();
				    					for (int a = 0; a < listResults.length; a++) {				    					
				    						Literal lit = m.createLiteral(listResults[a]);				    					
					    					results.add(lit);
					    				}				    				
					    				m.add(root, pred, results);
				    				} else {
				    					Alt results = m.createAlt();
				    					for (int a = 0; a < listResults.length; a++) {				    					
				    						Literal lit = m.createLiteral(listResults[a]);				    					
					    					results.add(lit);
					    				}				    				
					    				m.add(root, pred, results);
				    				}				    								    					    				
				    			} else {
				    				Resource rs = m.createResource();
				    				m.add(root, pred, rs);
				    			} 
				    		} else if (commands.get(c).get(0).equals("PO-RefURI")) {
				    			pred = m.createProperty( prefixes.get(commands.get(c).get(1)) + commands.get(c).get(2));
				    			if(!datos[checkReferencePos(files,commands.get(c).get(4),commands.get(c).get(5))].equals("")) {
				    				obj = m.createResource(prefixes.get(commands.get(c).get(3))+datos[checkReferencePos(files,commands.get(c).get(4),commands.get(c).get(5))]);				    				
				    			} else {
				    				obj = m.createResource();				    				
				    			}
				    			m.add(root, pred, obj);	
				    		} else if (commands.get(c).get(0).equals("PO-URILiteral")) {
				    			pred = m.createProperty( prefixes.get(commands.get(c).get(1)) + commands.get(c).get(2));
				    			if (commands.get(c).get(4).length() == 0) {
				    				obj = m.createResource();
				    			} else {
				    				obj = m.createResource(prefixes.get(commands.get(c).get(3))+commands.get(c).get(4));
				    			}				    			
				    			m.add(root, pred, obj);
				    		} else if (commands.get(c).get(0).equals("PO-Literal")) {
				    			pred = m.createProperty( prefixes.get(commands.get(c).get(1)) + commands.get(c).get(2));
				    			objLit = m.createLiteral(commands.get(c).get(3));
				    			m.add(root, pred, objLit);
				    		} else if (commands.get(c).get(0).equals("PO-Enum")) {				    			
				    			pred = m.createProperty( prefixes.get(commands.get(c).get(1)) + commands.get(c).get(2));
				    			strObj = GenericFunctions.changeValueEnum(datos[checkReferencePos(files,commands.get(c).get(3),commands.get(c).get(4))], commands.get(c).get(5), commands.get(c).get(6));
				    			objLit = m.createLiteral(strObj);
								m.add(root, pred, objLit);							
				    		} else if (commands.get(c).get(0).equals("PO-QueryLiteral")) {				    			
				    			pred = m.createProperty( prefixes.get(commands.get(c).get(1)) + commands.get(c).get(2));
				    			strObj = GenericFunctions.getFieldQuery(datos[checkReferencePos(files,commands.get(c).get(3),commands.get(c).get(4))], commands.get(c).get(5), commands.get(c).get(6));				    			
				    			objLit = m.createLiteral(strObj);
				    			m.add(root, pred, objLit);				    			
				    		} else if (commands.get(c).get(0).equals("PO-QueryLiteralContainer")) {				    			
				    			pred = m.createProperty( prefixes.get(commands.get(c).get(1)) + commands.get(c).get(2));
				    			strObj = GenericFunctions.getFieldQuery(datos[checkReferencePos(files,commands.get(c).get(3),commands.get(c).get(4))], commands.get(c).get(5), commands.get(c).get(6));
				    			if (strObj.length() > 0) {
				    				String[] listResults = strObj.split(",");				    				
				    				if (commands.get(c).get(7).equals("SEQ")) {
				    					Seq results = m.createSeq();
					    				for (int a = 0; a < listResults.length; a++) {
					    					results.add(listResults[a]);
					    				}
					    				m.add(root, pred, results);
				    				} else if (commands.get(c).get(7).equals("BAG")) {
				    					Bag results = m.createBag();
					    				for (int a = 0; a < listResults.length; a++) {
					    					results.add(listResults[a]);
					    				}
					    				m.add(root, pred, results);
				    				} else {
				    					Alt results = m.createAlt();
					    				for (int a = 0; a < listResults.length; a++) {
					    					results.add(listResults[a]);
					    				}
					    				m.add(root, pred, results);
				    				}			    				
				    			} else {
				    				objLit = m.createLiteral(strObj);
				    				m.add(root, pred, objLit);
				    			}
				    		} else if (commands.get(c).get(0).equals("PO-QueryURIContainer")) {
				    			pred = m.createProperty( prefixes.get(commands.get(c).get(1)) + commands.get(c).get(2));
				    			strObj = GenericFunctions.getFieldQuery(datos[checkReferencePos(files,commands.get(c).get(3),commands.get(c).get(4))], commands.get(c).get(5), commands.get(c).get(6));
				    			if (strObj.length() > 0) {
				    				String[] listResults = strObj.split(",");
				    				if (commands.get(c).get(8).equals("SEQ")) {
				    					Seq results = m.createSeq();
				    					for (int a = 0; a < listResults.length; a++) {				    					
					    					Resource rs = m.createResource(prefixes.get(commands.get(c).get(7))+listResults[a]);				    					
					    					results.add(rs);
					    				}				    				
					    				m.add(root, pred, results);
				    				} else if (commands.get(c).get(8).equals("BAG")) {
				    					Bag results = m.createBag();
				    					for (int a = 0; a < listResults.length; a++) {				    					
					    					Resource rs = m.createResource(prefixes.get(commands.get(c).get(7))+listResults[a]);				    					
					    					results.add(rs);
					    				}				    				
					    				m.add(root, pred, results);
				    				} else {
				    					Alt results = m.createAlt();
				    					for (int a = 0; a < listResults.length; a++) {				    					
					    					Resource rs = m.createResource(prefixes.get(commands.get(c).get(7))+listResults[a]);				    					
					    					results.add(rs);
					    				}				    				
					    				m.add(root, pred, results);
				    				}				    								    					    				
				    			} else {
				    				Resource rs = m.createResource();
				    				m.add(root, pred, rs);
				    			} 
				    		} else if (commands.get(c).get(0).equals("PO-QueryURI")) {	
				    			pred = m.createProperty( prefixes.get(commands.get(c).get(1)) + commands.get(c).get(2));
				    			strObj = GenericFunctions.getFieldQuery(datos[checkReferencePos(files,commands.get(c).get(3),commands.get(c).get(4))], commands.get(c).get(5), commands.get(c).get(6));
				    			if (strObj.length() > 0) {				    				
					    			Resource rs = m.createResource(commands.get(c).get(7)+strObj);
				    				m.add(root, pred, rs);
				    			} else {
				    				Resource rs = m.createResource();
				    				m.add(root, pred, rs);
				    			}				    		
				    		} else if (commands.get(c).get(0).equals("PO-QueryEnum")) {				    			
				    			pred = m.createProperty( prefixes.get(commands.get(c).get(1)) + commands.get(c).get(2));
				    			strObj = GenericFunctions.getFieldQueryEnum(datos[checkReferencePos(files,commands.get(c).get(3),commands.get(c).get(4))], commands.get(c).get(5), commands.get(c).get(6), commands.get(c).get(7), commands.get(c).get(8));				    			
				    			objLit = m.createLiteral(strObj);
			    				m.add(root, pred, objLit);
				    		} else if (commands.get(c).get(0).equals("PO-QueryEnumContainer")) {				    			
				    			pred = m.createProperty( prefixes.get(commands.get(c).get(1)) + commands.get(c).get(2));
				    			strObj = GenericFunctions.getFieldQueryEnum(datos[checkReferencePos(files,commands.get(c).get(3),commands.get(c).get(4))], commands.get(c).get(5), commands.get(c).get(6), commands.get(c).get(7), commands.get(c).get(8));
				    			if (strObj.length() > 1) {
				    				String[] listResults = strObj.split(",");
				    				if (commands.get(c).get(9).equals("SEQ")) {
				    					Seq results = m.createSeq();
					    				for (int a = 0; a < listResults.length; a++) {
					    					results.add(listResults[a]);
					    				}
					    				m.add(root, pred, results);
				    				} else if (commands.get(c).get(9).equals("BAG")) {
				    					Bag results = m.createBag();
					    				for (int a = 0; a < listResults.length; a++) {
					    					results.add(listResults[a]);
					    				}
					    				m.add(root, pred, results);
				    				} else {
				    					Alt results = m.createAlt();
					    				for (int a = 0; a < listResults.length; a++) {
					    					results.add(listResults[a]);
					    				}
					    				m.add(root, pred, results);
				    				}		
				    			} else {
				    				objLit = m.createLiteral(strObj);
				    				m.add(root, pred, objLit);
				    			}
				    		} else if (commands.get(c).get(0).equals("PO-EnumWheelchair")) {				    			
				    			pred = m.createProperty( prefixes.get(commands.get(c).get(1)) + commands.get(c).get(2));
				    			String result = GenericFunctions.getWheelchair(datos[checkReferencePos(files,commands.get(c).get(3),commands.get(c).get(4))], datos[checkReferencePos(files,commands.get(c).get(3),commands.get(c).get(5))], datos[checkReferencePos(files,commands.get(c).get(3),commands.get(c).get(6))], sources.get(commands.get(c).get(3)));
				    			strObj = GenericFunctions.changeValueEnum(result, commands.get(c).get(7), commands.get(c).get(8));
				    			objLit = m.createLiteral(strObj);
								m.add(root, pred, objLit);				    		
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
						
			}			
		}
		try {
			FileWriter fw = new FileWriter(".\\output.rdf");			
			m.write(fw,"RDF/XML");
			fw.close();
		}catch(IOException e){
            System.out.println("Error E/S: "+e);
        }
		return errors;		
		
	}
	
	public boolean checkReference (Hashtable<String,String[]> files, String file, String field) {
		boolean contains = false;
		int j;
		if (files.get(file) != null) {
			for (j=0; j<files.get(file).length;j++) {				
				if (files.get(file)[j].equals(field)) {
					contains = true;							
					break;
				}					
			}
		}
		
		return contains;
	}
	
	public int checkReferencePos (Hashtable<String,String[]> files, String file, String field) {
		int contains = -1;
		int j;
		if (files.get(file) != null) {
			for (j=0; j<files.get(file).length;j++) {				
				if (files.get(file)[j].equals(field)) {
					contains = j;							
					break;
				}					
			}
		}		
		return contains;
	}
	
	public int numberMatchesString (String match, String text) {
		int counter = 0;
		while (text.indexOf(match) > -1) {
		      text = text.substring(text.indexOf(match)+match.length(),text.length());		      
		      counter++; 
		}
		return counter;
	}
	
	public ArrayList<String> setSubject (String aliasFile, String field, String prefixURI, String nameClass) {
		ArrayList<String> params = new ArrayList<String>();
		params.add("S");
		params.add(aliasFile);
		params.add(field);
		params.add(prefixURI);
		params.add(nameClass);
		
		return params;
	}
	
	public ArrayList<String> setEnum (String prefixPredicate, String nameProperty, String aliasFile, String field, String values, String enums) {
		ArrayList<String> params = new ArrayList<String>();
		params.add("PO-Enum");
		params.add(prefixPredicate);
		params.add(nameProperty);
		params.add(aliasFile);
		params.add(field);
		params.add(values);
		params.add(enums);

		return params;
	}	
	
	public ArrayList<String> setWheelchairBoarding (String prefixPredicate, String nameProperty, String aliasFile, String field, String valueLocationType, String valueParentStation, String values, String enums) {
		ArrayList<String> params = new ArrayList<String>();
		params.add("PO-EnumWheelchair");
		params.add(prefixPredicate);
		params.add(nameProperty);
		params.add(aliasFile);
		params.add(field);
		params.add(valueLocationType);
		params.add(valueParentStation);
		params.add(values);
		params.add(enums);

		return params;
	}
	
	public ArrayList<String> setContainerRefURIsequence (String prefixPredicate, String nameProperty, String typeContainer, String prefixObj, String varFile, String field ) {
		ArrayList<String> params = new ArrayList<String>();
		params.add("PO-ContainerRefURIsequence");
		params.add(prefixPredicate);
		params.add(nameProperty);
		params.add(typeContainer);
		params.add(prefixObj);
		params.add(varFile);
		params.add(field);
		
		return params;
	}
	public ArrayList<String> setContainerRefsequence (String prefixPredicate, String nameProperty, String typeContainer, String varFile, String field ) {
		ArrayList<String> params = new ArrayList<String>();
		params.add("PO-ContainerRefsequence");
		params.add(prefixPredicate);
		params.add(nameProperty);
		params.add(typeContainer);
		params.add(varFile);
		params.add(field);
		
		return params;
	}
	
	public ArrayList<String> setRefURI (String prefixPredicate, String nameProperty, String prefixObj, String varFile, String field ) {
		ArrayList<String> params = new ArrayList<String>();
		params.add("PO-RefURI");
		params.add(prefixPredicate);
		params.add(nameProperty);
		params.add(prefixObj);
		params.add(varFile);
		params.add(field);
		
		return params;
	}
	
	public ArrayList<String> setSequenceLine (String prefixPredicate, String nameProperty, String prefixObj, String varFile, String field ) {
		ArrayList<String> params = new ArrayList<String>();
		params.add("PO-SequenceLine");
		params.add(prefixPredicate);
		params.add(nameProperty);
		params.add(prefixObj);
		params.add(varFile);
		params.add(field);
		
		return params;
	}
	
	public ArrayList<String> setRefLit (String prefixPredicate, String nameProperty, String varFile, String field) {
		ArrayList<String> params = new ArrayList<String>();
		params.add("PO-RefLit");		
		params.add(prefixPredicate);
		params.add(nameProperty);						
		params.add(varFile);
		params.add(field);
		
		return params;
	}
	
	public ArrayList<String> setLiteral (String prefixPredicate, String nameProperty, String object) {
		ArrayList<String> params = new ArrayList<String>();
		params.add("PO-Literal");		
		params.add(prefixPredicate);
		params.add(nameProperty);						
		params.add(object);
		
		return params;
	}
	
	public ArrayList<String> setURILiteral (String prefixPredicate, String nameProperty, String prefixObj, String field) {
		ArrayList<String> params = new ArrayList<String>();
		params.add("PO-URILiteral");		
		params.add(prefixPredicate);
		params.add(nameProperty);						
		params.add(prefixObj);
		params.add(field);
		
		return params;
	}
	
	public ArrayList<String> setQueryLiteral ( String prefixPredicate, String nameProperty, String aliasFileParam, String valueRefSource, String matchFileField, String returnField) {
		ArrayList<String> params = new ArrayList<String>();
		params.add("PO-QueryLiteral");
		params.add(prefixPredicate);
		params.add(nameProperty);
		params.add(aliasFileParam);
		params.add(valueRefSource);
		params.add(matchFileField);
		params.add(returnField);				
		
		return params;
	}
	
	public ArrayList<String> setQueryLiteralContainer ( String prefixPredicate, String nameProperty, String aliasFileParam, String valueRefSource, String matchFileField, String returnField, String typeContainer) {
		ArrayList<String> params = new ArrayList<String>();
		params.add("PO-QueryLiteralContainer");
		params.add(prefixPredicate);
		params.add(nameProperty);
		params.add(aliasFileParam);
		params.add(valueRefSource);
		params.add(matchFileField);
		params.add(returnField);
		params.add(typeContainer);
		
		return params;
	}
	
	public ArrayList<String> setQueryURI ( String prefixPredicate, String nameProperty, String aliasFileParam, String valueRefSource, String matchFileField, String returnField, String resultURI) {
		ArrayList<String> params = new ArrayList<String>();
		params.add("PO-QueryURI");
		params.add(prefixPredicate);
		params.add(nameProperty);
		params.add(aliasFileParam);
		params.add(valueRefSource);
		params.add(matchFileField);		
		params.add(returnField);				
		params.add(resultURI);
		
		return params;
	}
	
	public ArrayList<String> setQueryURIContainer ( String prefixPredicate, String nameProperty, String aliasFileParam, String valueRefSource, String matchFileField, String returnField, String resultURI, String typeContainer) {
		ArrayList<String> params = new ArrayList<String>();
		params.add("PO-QueryURIContainer");
		params.add(prefixPredicate);
		params.add(nameProperty);
		params.add(aliasFileParam);
		params.add(valueRefSource);
		params.add(matchFileField);		
		params.add(returnField);				
		params.add(resultURI);
		params.add(typeContainer);
		
		return params;
	}
	
	public ArrayList<String> setQueryEnum ( String prefixPredicate, String nameProperty, String aliasFileParam, String valueRefSource, String matchFileField, String returnField, String values, String enums) {
		ArrayList<String> params = new ArrayList<String>();
		params.add("PO-QueryEnum");
		params.add(prefixPredicate);
		params.add(nameProperty);
		params.add(aliasFileParam);
		params.add(valueRefSource);
		params.add(matchFileField);
		params.add(returnField);				
		params.add(values);
		params.add(enums);
		
		return params;
	}
	
	public ArrayList<String> setQueryEnumContainer ( String prefixPredicate, String nameProperty, String aliasFileParam, String valueRefSource, String matchFileField, String returnField, String values, String enums, String typeContainer) {
		ArrayList<String> params = new ArrayList<String>();
		params.add("PO-QueryEnumContainer");
		params.add(prefixPredicate);
		params.add(nameProperty);
		params.add(aliasFileParam);
		params.add(valueRefSource);
		params.add(matchFileField);
		params.add(returnField);				
		params.add(values);
		params.add(enums);
		params.add(typeContainer);
		
		return params;
	}
}
