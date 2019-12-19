package org.eclipse.buildship.sample.enc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.InitializationContext;
import org.eclipse.buildship.core.ProjectConfigurator;
import org.eclipse.buildship.core.ProjectContext;


public class EncodingConfigurator implements ProjectConfigurator {

    private Map<File, String> encoding;

    @Override
    public void init(InitializationContext context, IProgressMonitor monitor) {
        SubMonitor progress = SubMonitor.convert(monitor, 3);
        String initScriptPath = createInitScript(Activator.getBundleLocation(), progress.newChild(1));
        this.encoding = collectJavaPluginInfo(context.getGradleBuild(), initScriptPath, progress.newChild(1));
    }

    private static String createInitScript(File pluginLocation, IProgressMonitor monitor) {
        String initScriptContent = ""
                + "\n initscript {"
                + "\n     repositories {"
                + "\n         maven {"
                + "\n             url new File('" + pluginLocation.getAbsolutePath() + "/custom-model/repo').toURI().toURL()"
                + "\n         }"
                + "\n     }"
                + "\n"
                + "\n     dependencies {"
                + "\n         classpath 'org.eclipse.buildship.sample:plugin:1.0'"
                + "\n     }"
                + "\n }"
                + "\n"
                + "\n allprojects {"
                + "\n    apply plugin: org.eclipse.buildship.sample.enc.EncodingReaderPlugin"
                + "\n }";

        File initScript = new File(System.getProperty("java.io.tmpdir"), "buildship-encoding-init.gradle");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(initScript));
            writer.write(initScriptContent);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store init script");
        }

        return initScript.getAbsolutePath();
    }

    private static Map<File, String> collectJavaPluginInfo(GradleBuild gradleBuild, String initScriptPath, IProgressMonitor monitor) {
        try {
            return gradleBuild.withConnection(connection -> {
                GradleEncodingInfo model = connection.model(GradleEncodingInfo.class).withArguments("--init-script", initScriptPath).get();
                return model.getJavaCompileEncoding();
            }, monitor);
        } catch (Exception e) {
            Activator.getInstance().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Failed to query custom model", e));
            return Collections.emptyMap();
        }
    }

    @Override
    public void configure(ProjectContext context, IProgressMonitor monitor) {
        IProject project = context.getProject();
        IPath location = project.getLocation();
        if (location == null) {
            return;
        }
        String enc = this.encoding.get(location.toFile());
        if (enc == null) {
            return;
        }

        try {
            project.setDefaultCharset(enc, monitor);
        } catch (CoreException e) {
            Activator.getInstance().getLog().log(e.getStatus());
        }
    }

    @Override
    public void unconfigure(ProjectContext context, IProgressMonitor monitor) {
    }
}
