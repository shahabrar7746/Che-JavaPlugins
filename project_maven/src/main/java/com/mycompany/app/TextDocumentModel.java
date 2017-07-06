package com.mycompany.app;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;

public class TextDocumentModel {
	
	// Definition of DocumentText line
	public static class TextLine {
		final int line;
		final String documentText;
		
		
		public TextLine(int line, String documentText) {
			this.line = line;
			this.documentText = documentText;
		}

    }	



	public static class Route extends TextLine {
		final String name;
		public Route(int line, String documentText, String name) {
			super(line, documentText);
			this.name = name;
		}
	}
	public static class VariableDefinition extends TextLine {
		final String variableName;
		final String variableValue;
		
		public VariableDefinition(int lineNumber,String documentText, String variableName, String value) {
			super(lineNumber,documentText);
			this.variableName = variableName;
			variableValue = value;
		}

		
	}

// Separation of the DocumentText in lines 
public TextDocumentModel(String documentText){

	try (
			Reader reader= new StringReader(documentText);
			BufferedReader buffereReader = new BufferedReader(reader);
		) {
			String lineText;
			int lineNumber = 0;
			while ((lineText = buffereReader.readLine()) != null) {
				
			}
	}
		





}

