package org.jenkinsci.plugins.checksumarchivedemo;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.io.FileUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

/**
 * Created by danielbeck on 15.06.2016.
 */
public class ChecksumArchivePublisher extends Publisher implements SimpleBuildStep {

    @DataBoundConstructor
    public ChecksumArchivePublisher() {
        // no-op, needed for data binding
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws InterruptedException, IOException {

        File archiveDir = new File(run.getRootDir(), "checksumArchive");

        File resourceDir = new File(archiveDir, "resources");
        resourceDir.mkdirs();
        FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("style.css"), new File(resourceDir, "style.css"));
        FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("script.js"), new File(resourceDir, "script.js"));
        FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("index.html"), new File(archiveDir, "index.html"));

        SafeArchiveServingRunAction caa = new SafeArchiveServingRunAction(archiveDir, "checksumArchive", "index.html", "clipboard", "Checksum Safe Archive");

        run.addAction(caa);
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        @Override
        public String getDisplayName() {
            return "Checksum Archive";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
