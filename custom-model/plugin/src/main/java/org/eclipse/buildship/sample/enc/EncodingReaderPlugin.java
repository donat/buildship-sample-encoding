package org.eclipse.buildship.sample.enc;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

public class EncodingReaderPlugin implements Plugin<Project> {

    private final ToolingModelBuilderRegistry registry;

    @Inject
    public EncodingReaderPlugin(ToolingModelBuilderRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void apply(Project project) {
        this.registry.register(new CustomToolingModelBuilder());
    }

    private static class CustomToolingModelBuilder implements ToolingModelBuilder {

        @Override
        public boolean canBuild(String modelName) {
            return modelName.equals(GradleEncodingInfo.class.getName());
        }

        @Override
        public Object buildAll(String modelName, Project rootProject) {
            Map<File, String> projectEncoding = new HashMap<>();

            for (Project project : rootProject.getAllprojects()) {
                String encoding = System.getProperty("file.encoding", "UTF-8");

                TaskCollection<Task> tasks = project.getTasks().matching(new Spec<Task>() {

                    @Override
                    public boolean isSatisfiedBy(Task task) {
                        return task instanceof JavaCompile;
                    }
                });

                for (Task task : tasks) {
                    JavaCompile compile = (JavaCompile) task;
                    String compileEncoding = compile.getOptions().getEncoding();
                    if (compileEncoding != null) {
                        encoding = compileEncoding;
                        break;
                    }
                }

                projectEncoding.put(project.getProjectDir(), encoding);
            }

            return new GradleEncodingInfoImpl(projectEncoding);
        }
    }
}
