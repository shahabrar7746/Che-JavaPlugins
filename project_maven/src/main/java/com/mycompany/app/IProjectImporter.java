package com.mycompany.app;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IProjectImporter {

	void initialize(File rootFolder);

	boolean applies(IProgressMonitor monitor) throws InterruptedException, CoreException;

	void importToWorkspace(IProgressMonitor monitor) throws InterruptedException, CoreException;

	void reset();
}