package com.mycompany.app;

import java.util.Hashtable;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

public class PreferenceManager {

	
	private Preferences preferences;

	public PreferenceManager() {
		preferences = new Preferences();
	}
	
	public void initialize() {
		// Update JavaCore options
		Hashtable<String, String> javaCoreOptions = JavaCore.getDefaultOptions();//JavaCore.getOptions();
		javaCoreOptions.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		javaCoreOptions.put(DefaultCodeFormatterConstants.FORMATTER_USE_ON_OFF_TAGS, DefaultCodeFormatterConstants.TRUE);
		JavaCore.setOptions(javaCoreOptions);
	}
	
	public Preferences getPreferences() {
		return preferences;
	}

}
