package com.mycompany.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EitherTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;


public class ImplTextDocumentService implements TextDocumentService {
	
	// Dictionary creation for store DocumentText
	private final Map<String, DocumentText> documents = new HashMap<String, DocumentText>();
	
	private final ImplLanguageServer implLanguageServer;
	public ImplTextDocumentService( ImplLanguageServer implLanguageServer) {
		this.implLanguageServer = implLanguageServer;
	}


	
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
			TextDocumentPositionParams position) {
		String url = position.getTextDocument().getUri();
		if (!documents.containsKey(url)){
			return null;		
		}
		
		TextDocumentModel model = documents.get(url).getTextDocumentModel();
		
		return CompletableFuture.supplyAsync((Supplier<Either<List<CompletionItem>, CompletionList>>) () -> model.getCompletion(position));
	}
	

	public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
		
		return null;
	}

	public CompletableFuture<Hover> hover(TextDocumentPositionParams position) {
		return CompletableFuture.supplyAsync(() -> {
			String uri = position.getTextDocument().getUri();
			DocumentText documentText = documents.get(uri);
			TextDocumentModel textDocumentModel = documentText.getTextDocumentModel();
			Hover hover = new Hover();

			int linePosition = position.getPosition().getLine();
			String lineText = textDocumentModel.getTextAtLine(linePosition);

			List<Either<String, MarkedString>> contents = new ArrayList<>();

			Either<String, MarkedString> content = Either.forLeft(lineText);

			contents.add(content);

			hover.setContents(contents);

			return hover;
		});
	}

	public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams position) {
		// TODO Auto-generated method stub
		return null;
	}

	public CompletableFuture<List<? extends Location>> definition(TextDocumentPositionParams position) {
	    DocumentText documentText= documents.get(position.getTextDocument().getUri());
	    TextDocumentModel textDocumentModel= documentText.getTextDocumentModel();
	    
	    
	   
	    
		
		return null;
	}

	public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
		
		DocumentText documentText= documents.get(params.getTextDocument().getUri());
	    TextDocumentModel textDocumentModel= documentText.getTextDocumentModel();
		// TODO Auto-generated method stub
		return null;
	}

	public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams position) {
		// TODO Auto-generated method stub
		return null;
	}

	public CompletableFuture<List<? extends SymbolInformation>> documentSymbol(DocumentSymbolParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	public CompletableFuture<List<? extends Command>> codeAction(CodeActionParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
		// TODO Auto-generated method stub
		return null;
	}

	public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
		// TODO Auto-generated method stub
		return null;
	}
	
    // Get the url of the parameters and check if it does not exist 
	// If it exists, it updates the TextDocumentItem
	// Otherwise a TextDocumentItem is added to the Map
	
	public void didOpen(DidOpenTextDocumentParams params) {
		
		String url = params.getTextDocument().getUri();
		
		if (url!=null){
			if (documents.containsKey(url)){
				DocumentText doc =documents.get(url);
				doc.setText(params.getTextDocument().getText());
			}
			
			else{
				DocumentText documentText = new DocumentText(url, params.getTextDocument().getText());
				documents.put(url, documentText);
			}
		}
		
		
		
	}
	
    //Apply the changes to documents
	public void didChange(DidChangeTextDocumentParams params) {
		
		String url = params.getTextDocument().getUri();
		
		List<TextDocumentContentChangeEvent> contentChanges = params.getContentChanges();
		
		for(TextDocumentContentChangeEvent change : contentChanges){
			if(documents.containsKey(url)){
				documents.get(url).setText(change.getText());
			}
			else{
				DocumentText documentText = new DocumentText(url,change.getText());
				documents.put(url, documentText);
			}
				
		}
		
	
		
	}
	
    // Remove one url
	public void didClose(DidCloseTextDocumentParams params) {
		
		String url = params.getTextDocument().getUri();
		if (url!=null){
			if (documents.containsKey(url)){
				documents.remove(url);
			}
		}
	
		
	}

 
	public void didSave(DidSaveTextDocumentParams params) {
		
		String url = params.getTextDocument().getUri();
		
		if (url!=null){
			if (documents.containsKey(url)){
				documents.get(url).setText(params.getText());
			}
			
			else{
				DocumentText documentText = new DocumentText(url, params.getText());
				documents.put(url, documentText);
			}
		}
		
	}

}

// Stores the url and text of a TextDocument
class DocumentText {
	String url;
	String text;
	TextDocumentModel textDocumentModel;
	public  DocumentText(String url, String text){
		this.url = url;
		this.text = text;
		try {
			this.textDocumentModel= new TextDocumentModel(text);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void setText (String text){
		this.text = text;	
	}
	public String getText()
	{
		return text;
	}
	public TextDocumentModel getTextDocumentModel(){
		return textDocumentModel;
	}
	
	
}
