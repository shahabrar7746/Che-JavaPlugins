package com.mycompany.app;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
//import org.eclipse.jdt.ls.core.internal.ActionableNotification;
//import org.eclipse.jdt.ls.core.internal.JDTUtils;
//import org.eclipse.jdt.ls.core.internal.JavaClientConnection.JavaLanguageClient;
//import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
//import org.eclipse.jdt.ls.core.internal.ProjectUtils;
//import org.eclipse.jdt.ls.core.internal.ServiceStatus;
//import org.eclipse.jdt.ls.core.internal.StatusFactory;
//import org.eclipse.jdt.ls.core.internal.preferences.PreferenceManager;
//import org.eclipse.jdt.ls.core.internal.preferences.Preferences.FeatureStatus;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.TextDocumentIdentifier;

public class ProjectsManager {
	
	public static final String DEFAULT_PROJECT_NAME= "jdt.ls-java-project";
	private PreferenceManager preferenceManager;

	public enum CHANGE_TYPE { CREATED, CHANGED, DELETED};

	public ProjectsManager(PreferenceManager manager) {
		this.preferenceManager = manager;
	}
	
	public IStatus initializeProjects(final String projectPath, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		try {
			deleteInvalidProjects(subMonitor.split(5));
			createJavaProject(getDefaultProject(), subMonitor.split(10));
			if (projectPath != null) {
				File userProjectRoot = new File(projectPath);
				IProjectImporter importer = getImporter(userProjectRoot, subMonitor.split(20));
				if (importer != null) {
					importer.importToWorkspace(subMonitor.split(70));
				}
			}

			return Status.OK_STATUS;
		} catch (InterruptedException e) {
			JavaLanguageServerPlugin.INSTACE.logInfo("Import cancelled");
			return Status.CANCEL_STATUS;
		} catch (Exception e) {
			JavaLanguageServerPlugin.INSTACE.logException("Problem importing to workspace", e);
			
			return  StatusFactory.newErrorStatus("Import failed: " + e.getMessage(), e);
		}
	}

	private void deleteInvalidProjects(IProgressMonitor monitor) {
		IProject[] projects = getWorkspaceRoot().getProjects();
		for (IProject project : projects) {
			if (project.exists()) {
				try {
					project.getDescription();
				} catch (CoreException e) {
					JavaLanguageServerPlugin.INSTACE.logInfo("The '" + project.getName() + "' is invalid.");
					try {
						project.delete(true, monitor);
					} catch (CoreException e1) {
						JavaLanguageServerPlugin.INSTACE.logException(e1.getMessage(), e1);
					}
				}
			}
		}
	}

	private static IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}


	private IProjectImporter getImporter(File rootFolder, IProgressMonitor monitor) throws InterruptedException, CoreException {
		Collection<IProjectImporter> importers = importers();
		SubMonitor subMonitor = SubMonitor.convert(monitor, importers.size());
		for (IProjectImporter importer : importers) {
			importer.initialize(rootFolder);
			if (importer.applies(subMonitor.split(1))) {
				return importer;
			}
		}
		return null;
	}

	public IProject getDefaultProject() {
		return getWorkspaceRoot().getProject(DEFAULT_PROJECT_NAME);
	}

	private Collection<IProjectImporter> importers() {
		return Arrays.asList(/*new GradleProjectImporter(), new MavenProjectImporter(), */new EclipseProjectImporter());
	}

	public IProject createJavaProject(IProject project, IProgressMonitor monitor) throws CoreException, OperationCanceledException, InterruptedException {
		if (project.exists()) {
			return project;
		}
		JavaLanguageServerPlugin.INSTACE.logInfo("Creating the default Java project");
		//Create project
		project.create(monitor);
		project.open(monitor);

		//Turn into Java project
		IProjectDescription description = project.getDescription();
		description.setNatureIds(new String[] { JavaCore.NATURE_ID });
		project.setDescription(description, monitor);
		IJavaProject javaProject = JavaCore.create(project);

		//Add build output folder
		IFolder output = project.getFolder("bin");
		if (!output.exists()) {
			output.create(true, true, monitor);
		}
		javaProject.setOutputLocation(output.getFullPath(), monitor);

		//Add source folder
		IFolder source = project.getFolder("src");
		if (!source.exists()) {
			source.create(true, true, monitor);
		}
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(source);
		IClasspathEntry src =JavaCore.newSourceEntry(root.getPath());

		//Find default JVM
		IClasspathEntry jre = JavaRuntime.getDefaultJREContainerEntry();

		//Add JVM to project class path
		javaProject.setRawClasspath(new IClasspathEntry[]{jre, src} , monitor);

		JavaLanguageServerPlugin.INSTACE.logInfo("Finished creating the default Java project");
		return project;
	}

}
