package braid.society.secret.buildlogic;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class OutputDefinedVersionForGitHubActionsTask extends DefaultTask {

    private final Property<String> version = getProject().getObjects().property(String.class).convention("0.0.1");
    private final Property<String> prefix = getProject().getObjects().property(String.class).convention("");

    @Input
    public Property<String> getVersion() {
        return version;
    }

    @Input
    public Property<String> getPrefix() {
        return prefix;
    }

    @TaskAction
    public void printVersion() {
        System.out.printf("::set-output name=version::%s%s%n", prefix.get(), version.get());
    }

}
