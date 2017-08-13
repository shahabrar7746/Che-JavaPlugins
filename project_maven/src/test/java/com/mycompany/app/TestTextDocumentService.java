package com.mycompany.app;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.services.LanguageServer;
import org.junit.Test;

import junit.framework.Assert;

public class TestTextDocumentService {
	
	static String fileContent = "class  Teste{\n int number;\n Teste() {\n number = 5;\n}\n}";
	
	public void checkHover(LanguageServer languageServer) throws IOException, InterruptedException, ExecutionException {
		languageServer.initialize(new InitializeParams());
		TextDocumentItem doc = new TextDocumentItem();
		File file = File.createTempFile("file", ".java");
		file.deleteOnExit();
		doc.setUri(file.toURI().toString());
		doc.setText(fileContent);
		languageServer.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(doc, doc.getText()));
		
		TextDocumentIdentifier id = new TextDocumentIdentifier(doc.getUri());
		CompletableFuture<Hover> hover = languageServer.getTextDocumentService().hover(new TextDocumentPositionParams(id, doc.getUri(), new Position(3, 2)));
		Assert.assertEquals("Verte", hover.get().getContents().get(0).getLeft());
		
		hover = languageServer.getTextDocumentService().hover(new TextDocumentPositionParams(id, doc.getUri(), new Position(0, 0)));
		Assert.assertEquals("Verte", hover.get().getContents().get(0).getLeft());
	}
	

	@Test
	public void checkHoverLocal() throws IOException, InterruptedException, ExecutionException {
		checkHover(new ImplLanguageServer());
	}


}
