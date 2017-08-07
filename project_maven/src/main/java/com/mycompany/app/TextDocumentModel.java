package com.mycompany.app;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import javax.swing.text.BadLocationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
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
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.google.common.io.CharStreams;

public class TextDocumentModel {

	private static final long LABEL_FLAGS = JavaElementLabels.ALL_FULLY_QUALIFIED | JavaElementLabels.M_PRE_RETURNTYPE
	// | JavaElementLabels.M_PARAMETER_ANNOTATIONS
			| JavaElementLabels.M_PARAMETER_TYPES | JavaElementLabels.M_PARAMETER_NAMES | JavaElementLabels.M_EXCEPTIONS
			| JavaElementLabels.F_PRE_TYPE_SIGNATURE | JavaElementLabels.M_PRE_TYPE_PARAMETERS
			| JavaElementLabels.T_TYPE_PARAMETERS | JavaElementLabels.USE_RESOLVED;

	private static final long LOCAL_VARIABLE_FLAGS = LABEL_FLAGS & ~JavaElementLabels.F_FULLY_QUALIFIED
			| JavaElementLabels.F_POST_QUALIFIED;

	private static final long COMMON_SIGNATURE_FLAGS = LABEL_FLAGS & ~JavaElementLabels.ALL_FULLY_QUALIFIED
			| JavaElementLabels.T_FULLY_QUALIFIED | JavaElementLabels.M_FULLY_QUALIFIED;

	private static final String JDT_SCHEME = "jdt";

	private ICompilationUnit compilationUnit = null;
	private String[] compilationUnitContent = null;
	private String uri;

	public TextDocumentModel(String uri) throws JavaModelException {
		this.uri = uri;

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

	public Hover getHover(int line, int column) throws CoreException, IOException {
		if (this.compilationUnit == null)
			return null;

		if (!(this.compilationUnit instanceof ITypeRoot))
			return null;

		// Logical generalization to find the iJavaElement
		IJavaElement iJavaElement = getJavaElement(line, column);

		List<Either<String, MarkedString>> res = new LinkedList<>();
		Hover hover = new Hover();

		if (iJavaElement == null) {
			res.add(Either.forLeft(""));
			hover.setContents(res);
			return hover;

		}

		MarkedString signature = this.computeSignature(iJavaElement);
		if (signature != null) {
			res.add(Either.forRight(signature));
		}
		String javadoc = computeJavadocHover(iJavaElement);
		if (javadoc != null) {
			res.add(Either.forLeft(javadoc));
		}

		hover.setContents(res);
		return hover;
	}

	private MarkedString computeSignature(IJavaElement element) {
		if (element == null) {
			return null;
		}
		String elementLabel = null;
		if (element instanceof ILocalVariable) {
			elementLabel = JavaElementLabels.getElementLabel(element, LOCAL_VARIABLE_FLAGS);
		} else {
			elementLabel = JavaElementLabels.getElementLabel(element, COMMON_SIGNATURE_FLAGS);
		}

		return new MarkedString("java", elementLabel);
	}

	private String computeJavadocHover(IJavaElement element) throws CoreException, IOException {
		IMember member;
		if (element instanceof ITypeParameter) {
			member = ((ITypeParameter) element).getDeclaringMember();
		} else if (element instanceof IMember) {
			member = (IMember) element;
		} else if (element instanceof IPackageFragment) {
			Reader r = JavadocContentAccess.getContentReader((IMember) element, true);
			if (r == null) {
				return null;
			}
			return CharStreams.toString(r);
		} else {
			return null;
		}

		IBuffer buf = member.getOpenable().getBuffer();
		if (buf == null) {
			return null;
		}

		return buf.getContents();
	}

	public List<? extends Location> getDefinition(int line, int column)
			throws JavaModelException, org.eclipse.jface.text.BadLocationException {
		if (!(this.compilationUnit instanceof ITypeRoot))
			return null;

		IJavaElement element = getJavaElement(line, column);
		if (element == null) {
			return null;
		}

		ICompilationUnit compilationUnit = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
		IClassFile cf = (IClassFile) element.getAncestor(IJavaElement.CLASS_FILE);

		Location location;
		if (compilationUnit != null || (cf != null && cf.getSourceRange() != null)) {
			location = toLocation(element);
		} else if (element instanceof IMember && ((IMember) element).getClassFile() != null) {
			location = toLocation(element);
		} else {
			location = new Location();
			location.setRange(new Range());
		}

		return Arrays.asList(location);

	}

	// Get IJavaElemnt of the compilation unit
	private IJavaElement getJavaElement(int line, int column) throws JavaModelException {
		// if(this.compilationUnit == null)
		// return null;
		//
		//
		// if(!(this.compilationUnit instanceof ITypeRoot))
		// return null;
		//
		int offset = getOffset(line, column);

		ITypeRoot typeRoot = (ITypeRoot) compilationUnit;
		IJavaElement[] elements = null;
		if (offset > -1) {
			elements = typeRoot.codeSelect(offset, 0);
		} else {
			return null;
		}
		if (elements == null || elements.length == 0) {
			return null;
		}

		IJavaElement curr = null;
		if (elements.length != 1) {
			IPackageFragment packageFragment = (IPackageFragment) typeRoot.getParent();
			IJavaElement found = Stream.of(elements).filter(e -> e.equals(packageFragment)).findFirst().orElse(null);
			if (found == null) {

				curr = elements[0];
			} else {
				curr = found;
			}

		}

		return curr;
	}

	// Returns a location of an element java
	private Location toLocation(IJavaElement element)
			throws JavaModelException, org.eclipse.jface.text.BadLocationException {
		ICompilationUnit unit = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
		IClassFile cf = (IClassFile) element.getAncestor(IJavaElement.CLASS_FILE);
		if (unit == null && cf == null) {
			return null;
		}
		if (element instanceof ISourceReference) {
			ISourceRange nameRange = getNameRange(element);
			if (SourceRange.isAvailable(nameRange)) {
				if (cf == null) {
					String fileURI = unit.getResource().getRawLocationURI().toString();

					return new Location(fileURI, getRange(unit, nameRange.getOffset(), nameRange.getLength()));
					// return new Location(getFileURI(unit), toRange(unit, offset, length));
					// return toLocation(unit, nameRange.getOffset(), nameRange.getLength());
				} else {
					String packageName = cf.getParent().getElementName();
					String jarName = cf.getParent().getParent().getElementName();
					String uriString = null;
					try {
						uriString = new URI(JDT_SCHEME, "contents",
								"/" + jarName + "/" + packageName + "/" + cf.getElementName(), cf.getHandleIdentifier(),
								null).toASCIIString();
					} catch (URISyntaxException e) {

					}
					Range range = getRange(cf, nameRange.getOffset(), nameRange.getLength());
					return new Location(uriString, range);

					// return toLocation(cf, nameRange.getOffset(), nameRange.getLength());
				}
			}
		}
		return null;
	}

	private Range getRange(IOpenable openable, int offset, int length)
			throws JavaModelException, org.eclipse.jface.text.BadLocationException {

		int[] location = null;
		int[] endlocation = null;

		if (openable.getBuffer() != null) {

			if (openable.getBuffer() instanceof IDocument) {
				IDocument doc = (IDocument) openable.getBuffer();
				location = new int[] { doc.getLineOffset(offset), offset - doc.getLineOffset(offset) };

				endlocation = new int[] { doc.getLineOffset(offset + length),
						offset + length - doc.getLineOffset(offset) };

			}
		}

		if (location == null)
			location = new int[2];

		if (endlocation == null)
			endlocation = new int[2];

		Range range = new Range(new Position(), new Position());
		range.getStart().setLine(location[0]);
		range.getStart().setCharacter(location[1]);

		range.getEnd().setLine(location[0]);
		range.getEnd().setCharacter(location[1]);
		return range;
	}

	private ISourceRange getNameRange(IJavaElement element) throws JavaModelException {
		ISourceRange nameRange = null;
		if (element instanceof IMember) {
			IMember member = (IMember) element;
			nameRange = member.getNameRange();
			if ((!SourceRange.isAvailable(nameRange))) {
				nameRange = member.getSourceRange();
			}
		} else if (element instanceof ITypeParameter || element instanceof ILocalVariable) {
			nameRange = ((ISourceReference) element).getNameRange();
		} else if (element instanceof ISourceReference) {
			nameRange = ((ISourceReference) element).getSourceRange();
		}
		if (!SourceRange.isAvailable(nameRange) && element.getParent() != null) {
			nameRange = getNameRange(element.getParent());
		}
		return nameRange;
	}

	List<Location> findReferencesOf(int line, int column) throws CoreException {
		IJavaElement javaElement = getJavaElement(line, column);
		if (javaElement == null) {
			return Collections.emptyList();
		}

		SearchEngine engine = new SearchEngine();

		IJavaProject[] projects = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
		IJavaSearchScope javaSearchScope = SearchEngine.createJavaSearchScope(projects,
				IJavaSearchScope.SOURCES | IJavaSearchScope.APPLICATION_LIBRARIES);

		SearchPattern pattern = SearchPattern.createPattern(javaElement, IJavaSearchConstants.REFERENCES);
		
		NullProgressMonitor monitor = new NullProgressMonitor();
		
		
		List<Location> locations = new ArrayList<>();
		engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, javaSearchScope,
				new SearchRequestor() {

					@Override
					public void acceptSearchMatch(SearchMatch match) throws CoreException {
						Object object = match.getElement();
						if (object instanceof IJavaElement) {
							IJavaElement element = (IJavaElement) object;
							ICompilationUnit compilationUnit = (ICompilationUnit) element
									.getAncestor(IJavaElement.COMPILATION_UNIT);
							Location location = null;
							if (compilationUnit != null) {
								try {
									location = toLocation(compilationUnit);
								} catch (org.eclipse.jface.text.BadLocationException e) {
									e.printStackTrace();
								}
							} else {
								IClassFile classFile = (IClassFile) element.getAncestor(IJavaElement.CLASS_FILE);
								if (classFile != null && classFile.getSourceRange() != null) {
									try {
										location = toLocation(classFile);
									} catch (org.eclipse.jface.text.BadLocationException e) {
										e.printStackTrace();
									}
								}
							}
							if (location != null) {
								locations.add(location);
							}

						}

					}
				}, monitor);

		return locations;
	}

}
