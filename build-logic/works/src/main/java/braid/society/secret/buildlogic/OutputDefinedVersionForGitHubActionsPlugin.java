package braid.society.secret.buildlogic;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class OutputDefinedVersionForGitHubActionsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project
          .getTasks()
          .register("outputDefinedVersionForGitHubActions", OutputDefinedVersionForGitHubActionsTask.class);
    }
}
