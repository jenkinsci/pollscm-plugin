package org.jenkinsci.plugins.pollscm;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Descriptor.FormException;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.triggers.SCMTrigger;
import hudson.triggers.Trigger;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import javax.annotation.Nonnull;

import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class SCMPollPublisher
        extends Notifier
        implements SimpleBuildStep {
    @DataBoundConstructor
    public SCMPollPublisher() {
        super();
    }

    public boolean needsToRunAfterFinalized() {
        return true;
    }

    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener taskListener)
            throws InterruptedException, IOException {
        AbstractProject project = (AbstractProject) run.getParent();

        Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
        for (Thread thread : map.keySet()) {
            if (thread.getName().equals("SCM polling for " + project.toString())) {
                taskListener.getLogger().println("SCM polling already in progress, not triggering again");
                return;
            }
        }

        taskListener.getLogger().println(run.getResult());

        Trigger trigger = project.getTrigger(SCMTrigger.class);
        if (trigger == null) {
            taskListener.getLogger().println("SCM polling not enabled, not triggering");
        } else {
            taskListener.getLogger().println("Triggering SCM polling");
            trigger.run();
        }
    }

    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static final class DescriptorImpl
            extends BuildStepDescriptor<Publisher> {
        public String getDisplayName() {
            return "Poll SCM";
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public boolean configure(StaplerRequest staplerRequest, JSONObject json)
                throws FormException {
            save();
            return true;
        }
    }
}
