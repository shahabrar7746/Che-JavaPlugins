package com.mycompany.app;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import javax.swing.text.BadLocationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.JavadocContentAccess;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.google.common.io.CharStreams;

public class TextDocumentModel {


	private static final long LABEL_FLAGS=
			JavaElementLabels.ALL_FULLY_QUALIFIED
			| JavaElementLabels.M_PRE_RETURNTYPE
			//| JavaElementLabels.M_PARAMETER_ANNOTATIONS
			| JavaElementLabels.M_PARAMETER_TYPES
			| JavaElementLabels.M_PARAMETER_NAMES
			| JavaElementLabels.M_EXCEPTIONS
			| JavaElementLabels.F_PRE_TYPE_SIGNATURE
			| JavaElementLabels.M_PRE_TYPE_PARAMETERS
			| JavaElementLabels.T_TYPE_PARAMETERS
			| JavaElementLabels.USE_RESOLVED;

	private static final long LOCAL_VARIABLE_FLAGS= LABEL_FLAGS & ~JavaElementLabels.F_FULLY_QUALIFIED | JavaElementLabels.F_POST_QUALIFIED;

	private static final long COMMON_SIGNATURE_FLAGS = LABEL_FLAGS & ~JavaElementLabels.ALL_FULLY_QUALIFIED
			| JavaElementLabels.T_FULLY_QUALIFIED | JavaElementLabels.M_FULLY_QUALIFIED;
	
	
	private ICompilationUnit compilationUnit = null;
	private String[] compilationUnitContent = null;

	public TextDocumentModel(String uri) throws JavaModelException {

		IFile[] resources = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(URI.create(uri));

		IJavaElement element = JavaCore.create(resources[0]);

		if (element instanceof ICompilationUnit) {
			compilationUnit = (ICompilationUnit) element;

			compilationUnitContent = compilationUnit.getBuffer().getContents().split("\n");

		}
	}

	private int getOffset(int line, int column) {
		int offset = 0;

		int i;
		for (i = 0; i < line && i < compilationUnitContent.length; i++) {
			offset = offset + compilationUnitContent[i].length();
		}

		if (i != line)
			return -1;

		return offset + column;
	}

	public Either<List<CompletionItem>, CompletionList> getCompletion(TextDocumentPositionParams position)
			throws JavaModelException {
		List<CompletionItem> completionItems;

		completionItems = this.computeCompletitionContentAssist(position.getPosition().getLine(),
				position.getPosition().getCharacter());

		CompletionList list = new CompletionList();
		list.setItems(completionItems);
		return Either.forRight(list);
	}

	private List<CompletionItem> computeCompletitionContentAssist(int line, int column) throws JavaModelException {

		List<CompletionItem> proposals = new ArrayList<>();

		if (compilationUnit == null)
			return proposals;

		int offset = getOffset(line, column);

		CompletionProposalCollector collector = new CompletionProposalCollector(compilationUnit);
		collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_REF, true);
		collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_IMPORT, true);
		collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.FIELD_IMPORT, true);

		collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF, CompletionProposal.TYPE_REF, true);
		collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF, CompletionProposal.TYPE_IMPORT, true);
		collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF, CompletionProposal.METHOD_IMPORT, true);

		collector.setAllowsRequiredProposals(CompletionProposal.CONSTRUCTOR_INVOCATION, CompletionProposal.TYPE_REF,
				true);

		collector.setAllowsRequiredProposals(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION,
				CompletionProposal.TYPE_REF, true);
		collector.setAllowsRequiredProposals(CompletionProposal.ANONYMOUS_CLASS_DECLARATION,
				CompletionProposal.TYPE_REF, true);

		collector.setAllowsRequiredProposals(CompletionProposal.TYPE_REF, CompletionProposal.TYPE_REF, true);

		if (offset > -1) {
			compilationUnit.codeComplete(offset, collector);
			for (IJavaCompletionProposal item : collector.getJavaCompletionProposals()) {
				proposals.add(new CompletionItem(item.toString()));
			}
		}

		return proposals;
	}

	public Hover getHover(int line, int column) throws CoreException, IOException{
		if(this.compilationUnit == null)
			return null;
		
		
		if(!(this.compilationUnit instanceof ITypeRoot))
			return null;
		
		int offset = getOffset(line, column);
		
		ITypeRoot typeRoot = (ITypeRoot)compilationUnit;
		IJavaElement[] elements = null;
		if(offset > -1){			
			elements = typeRoot.codeSelect(offset, 0);
		}
		else{
			return null;
		}
		List<Either<String, MarkedString>> res = new LinkedList<>();
		Hover hover = new Hover();
		
		if(elements == null || elements.length == 0) {
			res.add(Either.forLeft(""));
			hover.setContents(res);
			return hover;
		}
		
		
		
		IJavaElement curr = null;
		if (elements.length != 1) {
			IPackageFragment packageFragment = (IPackageFragment) typeRoot.getParent();
			IJavaElement found = Stream.of(elements).filter(e -> e.equals(packageFragment)).findFirst().orElse(null);
			if (found == null) {
				// this would be a binary package fragment
				curr = elements[0];
			} else {
				curr = found;
			}
	
		}
		MarkedString signature = this.computeSignature(curr);
		if (signature != null) {
			res.add(Either.forRight(signature));
		}
		String javadoc = computeJavadocHover(curr);
		if (javadoc != null) {
			res.add(Either.forLeft(javadoc));
		}

		hover.setContents(res);
		return hover;
	} 
	
	
	private MarkedString computeSignature(IJavaElement element)  {
		if (element == null) {
			return null;
		}
		String elementLabel = null;
		if (element instanceof ILocalVariable) {
			elementLabel = JavaElementLabels.getElementLabel(element,LOCAL_VARIABLE_FLAGS);
		} else {
			elementLabel = JavaElementLabels.getElementLabel(element,COMMON_SIGNATURE_FLAGS);
		}
	
		return new MarkedString("java", elementLabel);
	}


	private String computeJavadocHover(IJavaElement element) throws CoreException, IOException {
		IMember member;
		if (element instanceof ITypeParameter) {
			member= ((ITypeParameter) element).getDeclaringMember();
		} else if (element instanceof IMember) {
			member= (IMember) element;
		} else if (element instanceof IPackageFragment) {
			Reader r = JavadocContentAccess.getContentReader((IMember) element, true);
			if(r == null ) {
				return null;
			}
			return CharStreams.toString(r);
		} else {
			return null;
		}
	
		IBuffer buf= member.getOpenable().getBuffer();
		if (buf == null) {
			return null;
		}
	
		return buf.getContents();
	}

}
