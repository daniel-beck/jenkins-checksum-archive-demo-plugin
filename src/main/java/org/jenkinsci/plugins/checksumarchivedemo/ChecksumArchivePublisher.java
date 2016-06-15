package org.jenkinsci.plugins.checksumarchivedemo;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

        ChecksumArchiveAction caa = new ChecksumArchiveAction();
        run.addAction(caa);

        File archiveDir = new File(run.getRootDir(), "checksumArchive");
        archiveDir.mkdirs();

        // files we can serve as-is, with Content-Security-Policy headers, can just be copied over
        FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("style.css"), new File(archiveDir, "style.css"));
        FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("script.js"), new File(archiveDir, "script.js"));

        // get the text of the file that is to be served through the wrapper file to safely circumvent Content-Security-Policy
        StringWriter sw = new StringWriter();
        IOUtils.copy(this.getClass().getResourceAsStream("index.html"), sw);
        String text = sw.toString();
        FileUtils.copyInputStreamToFile(new ByteArrayInputStream(text.getBytes()), new File(archiveDir, "index.html"));

        try {
            // Files we serve through the checksum wrapper should have their checksum recorded here
            caa.addFile("index.html", calculateChecksum(text));
        } catch (NoSuchAlgorithmException nsa) {
            // SHA-1 is guaranteed to exist
        }
    }

    private String calculateChecksum(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        sha1.update(text.getBytes("UTF-8"));
        return Util.toHexString(sha1.digest());
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
