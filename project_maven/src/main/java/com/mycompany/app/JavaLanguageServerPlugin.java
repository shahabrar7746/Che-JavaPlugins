package com.mycompany.app;

import org.eclipse.lsp4j.services.LanguageServer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class JavaLanguageServerPlugin implements BundleActivator {

	public static final JavaLanguageServerPlugin INSTACE = new JavaLanguageServerPlugin();
	
	private JavaLanguageServerPlugin(){
	
		
	}
	
	
	
	private LanguageServer languageServer = null;
	private BundleContext  bundleContext = null; 
	private ProjectsManager projectsManager = null;
	
	public ProjectsManager getProjectsManager() {
		return this.projectsManager;
	}
	
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		
		this.bundleContext = bundleContext;
		languageServer = new ImplLanguageServer();
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}
	

	public LanguageServer getLanguageServer()
	{
		return this.languageServer;
	}
	
	
}

