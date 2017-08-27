package com.mycompany.app;

import java.util.Hashtable;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.lsp4j.services.LanguageServer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.google.gson.JsonSyntaxException;

public class JavaLanguageServerPlugin implements BundleActivator {

	//singleton pattern, having a single instance of this object.
	public static final JavaLanguageServerPlugin INSTACE = new JavaLanguageServerPlugin();
	public static final String PLUGIN_ID = "Java Language Server";
	
	//disable public constructor due to singleton pattern.
	private JavaLanguageServerPlugin(){
		ImplLanguageServer implLanguageServer = new ImplLanguageServer();
		this.languageServer = implLanguageServer;
		
		preferenceManager = new PreferenceManager();
		initializeJDTOptions();
		projectsManager = new ProjectsManager(preferenceManager);
		logInfo(getClass()+" is started");
		
		preferenceManager =  new PreferenceManager();
	}
	
	
	
	private LanguageServer languageServer = null;
	private BundleContext  bundleContext = null; 
	private ProjectsManager projectsManager = null;
	private PreferenceManager preferenceManager = null;
	
	public ProjectsManager getProjectsManager() {
		return this.projectsManager;
	}
	
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		
		
		
		
		this.bundleContext = bundleContext;
		
	
		// TODO Auto-generated method stub
		
	}
	private void initializeJDTOptions() {
		// Update JavaCore options
		Hashtable<String, String> javaCoreOptions = JavaCore.getOptions();
		javaCoreOptions.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);

 		JavaCore.setOptions(javaCoreOptions);
	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}
	

	public LanguageServer getLanguageServer()
	{
		return this.languageServer;
	}

	public  void logException(String message, Exception e) {
		// TODO Auto-generated method stub
		
	}

	public void logInfo(String string) {
		// TODO Auto-generated method stub
		
	}

	public PreferenceManager getPreferencesManager() {
			return preferenceManager ;

	}

	public void log(IStatus status) {
		// TODO Auto-generated method stub
		
	}

	
}

