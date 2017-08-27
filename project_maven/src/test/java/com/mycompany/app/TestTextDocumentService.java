package com.mycompany.app;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.internal.Workbench;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

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
	public void checkHoverLocal() throws Exception {

		JavaLanguageServerPlugin.INSTACE.start(null);

		JavaLanguageServerPlugin.INSTACE.getProjectsManager().initializeProjects(null, new NullProgressMonitor());
		
		checkHover(JavaLanguageServerPlugin.INSTACE.getLanguageServer());
		

	}

	
	public static IProject getCurrentProject(){    
        ISelectionService selectionService =     
            Workbench.getInstance().getActiveWorkbenchWindow().getSelectionService();    

        ISelection selection = selectionService.getSelection();    

        IProject project = null;    
        if(selection instanceof IStructuredSelection) {    
            Object element = ((IStructuredSelection)selection).getFirstElement();    

            if (element instanceof IResource) {    
                project= ((IResource)element).getProject();    
            } else if (element instanceof PackageFragmentRootContainer) {    
                IJavaProject jProject =     
                    ((PackageFragmentRootContainer)element).getJavaProject();    
                project = jProject.getProject();    
            } else if (element instanceof IJavaElement) {    
                IJavaProject jProject= ((IJavaElement)element).getJavaProject();    
                project = jProject.getProject();    
            }    
        }     
        return project;    
    }
	
	
	

}
