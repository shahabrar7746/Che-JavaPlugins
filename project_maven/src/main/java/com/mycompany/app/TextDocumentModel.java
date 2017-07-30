package com.mycompany.app;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

public class TextDocumentModel {

	
	private ICompilationUnit compilationUnit = null;
	private String[] compilationUnitContent = null;
	
	//Use the JDT to obtain information about a given java file.
	public TextDocumentModel(String uri) throws JavaModelException {

    	IFile[] resources = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(URI.create(uri));
    	
    	IJavaElement element = JavaCore.create(resources[0]);
    	
    	
    	if (element instanceof ICompilationUnit) {
    		compilationUnit = (ICompilationUnit)element;
    		
    		compilationUnitContent = compilationUnit.getBuffer().getContents().split("\n");
    		
    	}
	}

	//This method returns a list of possible CompletionItems.
	public Either<List<CompletionItem>, CompletionList> getCompletion(TextDocumentPositionParams position) throws JavaModelException {
		List<CompletionItem> completionItems;
		
		completionItems = this.computeCompletionContentAssist(position.getPosition().getLine(),position.getPosition().getCharacter());
	
		CompletionList list = new CompletionList();
		list.setItems(completionItems);
		return Either.forRight(list);
	}
	
	private List<CompletionItem> computeCompletionContentAssist(int line, int column) throws JavaModelException {
		
		List<CompletionItem> proposals = new ArrayList<>();
		
		if(compilationUnit == null)
			return proposals;
		
		int offset = getOffset(line, column);
		
		CompletionProposalCollector collector= new CompletionProposalCollector(compilationUnit);
		collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_REF, true);
		collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_IMPORT, true);
		collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.FIELD_IMPORT, true);

		collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF, CompletionProposal.TYPE_REF, true);
		collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF, CompletionProposal.TYPE_IMPORT, true);
		collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF, CompletionProposal.METHOD_IMPORT, true);

		collector.setAllowsRequiredProposals(CompletionProposal.CONSTRUCTOR_INVOCATION, CompletionProposal.TYPE_REF, true);

		collector.setAllowsRequiredProposals(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION, CompletionProposal.TYPE_REF, true);
		collector.setAllowsRequiredProposals(CompletionProposal.ANONYMOUS_CLASS_DECLARATION, CompletionProposal.TYPE_REF, true);

		collector.setAllowsRequiredProposals(CompletionProposal.TYPE_REF, CompletionProposal.TYPE_REF, true);

		if (offset >-1) {
			compilationUnit.codeComplete(offset, collector);
			for(IJavaCompletionProposal item : collector.getJavaCompletionProposals()){
				proposals.add(new CompletionItem(item.toString()));
			}
		}

		return proposals;
	}

	
	private int getOffset(int line, int column){
		int offset = 0;
		
		int i;
		for(i = 0; i<line && i < compilationUnitContent.length; i++){
			offset = offset + compilationUnitContent[i].length();
		}
		
		if(i != line)
			return -1;
		
		return offset + column;
	}
	

}
