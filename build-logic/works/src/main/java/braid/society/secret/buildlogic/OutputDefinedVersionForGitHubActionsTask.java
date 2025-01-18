package braid.society.secret.buildlogic;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class OutputDefinedVersionForGitHubActionsTask extends DefaultTask {

    private final Property<String> version = getProject().getObjects().property(String.class);

    @Input
    public Property<String> getVersion() {
        return version;
    }

    @TaskAction
    public void printVersion() {
        System.out.printf("::set-output name=version::%s%n", version.get());
    }

}
