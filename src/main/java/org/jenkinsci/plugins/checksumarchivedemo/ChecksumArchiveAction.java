package org.jenkinsci.plugins.checksumarchivedemo;

import hudson.FilePath;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.Run;
import hudson.util.HttpResponses;
import jenkins.model.RunAction2;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ChecksumArchiveAction implements RunAction2 {

    private Map<String,String> fileChecksums = new HashMap<>();

    private transient Run run;

    public ChecksumArchiveAction() {

    }

    public void addFile(String relativePath, String checksum) {
        this.fileChecksums.put(relativePath, checksum);
    }

    public Set<String> getFileNames() {
        return Collections.unmodifiableSet(this.fileChecksums.keySet());
    }

    public String getChecksum(String file) {
        if (file == null || !fileChecksums.containsKey(file)) {
            throw new IllegalArgumentException(file + " has no checksum recorded");
        }
        return fileChecksums.get(file);
    }

    @Override
    public void onAttached(Run<?, ?> r) {
        this.run = r;
    }

    @Override
    public void onLoad(Run<?, ?> r) {
        this.run = r;
    }

    public Run getRun() {
        return run;
    }

    @Override
    public String getIconFileName() {
        return "document";
    }

    @Override
    public String getDisplayName() {
        return "Checksum Archive";
    }

    @Override
    public String getUrlName() {
        return "checksumArchive";
    }

    public File getRootDir() {
        return new File(run.getRootDir(), "checksumArchive");
    }

    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        DirectoryBrowserSupport dbs = new DirectoryBrowserSupport(this, new FilePath(getRootDir()), "Checksum Archive", "graph.gif", false);

        // serve the index page
        if (req.getRestOfPath().equals("")) {
            throw HttpResponses.redirectTo("index.html");
        }

        String fileName = req.getRestOfPath();
        if (fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }

        // if the file had its checksum recorded, route the request through the wrapper file
        if (fileChecksums.containsKey(fileName)) {
            throw HttpResponses.redirectTo("wrapper?filename=" + fileName);
        }
        dbs.generateResponse(req, rsp, this);
    }
}
