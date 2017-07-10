package com.mycompany.app;

import java.awt.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

public class TextDocumentModel {
	
	// Definition of DocumentText line
	public static abstract class TextLine {
		final int lineNumber;
		final String lineText;
		

		public TextLine(int lineNumber, String lineText) {
			this.lineNumber = lineNumber;
			this.lineText = lineText;
		}

    }	
	
	public static class ClassDefinition extends TextLine{
		final String className;
		
		public ClassDefinition (int lineNumber,String lineText,String className){
			super(lineNumber,lineText);
			this.className=className;
		}
		

	}
	
	public static class MethodsDefinition extends TextLine{
		final String methodsName;
		
		public MethodsDefinition(int lineNumber, String lineText,String methodsName){
			super(lineNumber,lineText);
			this.methodsName=methodsName;
		}
		
		
	}
	
	public static class VariableDefinition extends TextLine {
		final String variableName;
		final String variableValue;
		
		public VariableDefinition(int lineNumber,String lineText, String variableName, String value) {
			super(lineNumber, lineText);
			this.variableName = variableName;
			variableValue = value;
		}
	}
	//to store info of the code that do not belong to ClassDefinition, MethodsDefinition or VariableDefinition
	public static class UndefinedContent extends TextLine{
		public UndefinedContent(int lineNumber, String lineText){
			super(lineNumber, lineText);
		}
		
	}

private final ArrayList<TextLine> lines = new ArrayList<>();

// Separation of the DocumentText in lines 
//Contructor can trow IOExceotuib on usage of Reader and BufferReader.
public TextDocumentModel(String documentText) throws IOException{

	try (
			Reader reader= new StringReader(documentText);
			BufferedReader buffereReader = new BufferedReader(reader);
		) {
			String lineText;
			int lineNumber = 0;
			
			while ((lineText = buffereReader.readLine()) != null) {
				if (lineText.contains("class")){
					String[] content = lineText.split(" ");
					int index;
					for(index=0;index<content.length;index ++){
						if (content[index].equals("class"))
							break;	
					}
				
					String className=content[index+1];
				    ClassDefinition classDefinition = new ClassDefinition (lineNumber, lineText, className);
					lines.add(classDefinition);
				}
				else {
					if(lineText.contains("(") && lineText.contains(")") && lineText.contains("{")){
						 int index = lineText.indexOf("(");
						 String splited = lineText.substring(0, index);
						 String[] content = splited.split(" ");
						 
						 String methodsName = content[content.length-1];
						 
						 MethodsDefinition methodsDefinition = new MethodsDefinition(lineNumber, lineText, methodsName);
						 lines.add(methodsDefinition);
					}
					else{
						if(lineText.contains("=")){
							String[] content = lineText.split("=");
							
							String name = content[0];
							String value = content[1];
							
							VariableDefinition variableDefinitio = new VariableDefinition(lineNumber, lineText, name, value);
							lines.add(variableDefinitio);
						}
						else{
							UndefinedContent undefinedContent = new UndefinedContent(lineNumber, lineText);
							lines.add(undefinedContent);
						}
							
					}
					
				}
			}
				
	}
}
		





}

