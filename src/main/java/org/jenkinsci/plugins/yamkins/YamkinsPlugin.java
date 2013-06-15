package org.jenkinsci.plugins.yamkins;

import hudson.Launcher;
import hudson.Extension;
import hudson.tasks.*;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Yamkins plugin builder. When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked.
 * @author Cedric Meury
 * @author Kohsuke Kawaguchi
 */
public class YamkinsPlugin extends Notifier {

    private final String groupId;

    private boolean notifySuccess;
    private boolean notifyUnstable;
    private boolean notifyFailure;
    private boolean notifyBackToNormal;
    private boolean notifyAborted;
    private boolean notifyNotBuilt;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public YamkinsPlugin(String groupId,
                         boolean notifyAborted,
                         boolean notifyFailure,
                         boolean notifyNotBuilt,
                         boolean notifySuccess,
                         boolean notifyUnstable,
                         boolean notifyBackToNormal) {
        this.groupId = groupId;
        this.notifyAborted = notifyAborted;
        this.notifyFailure = notifyFailure;
        this.notifyNotBuilt = notifyNotBuilt;
        this.notifySuccess = notifySuccess;
        this.notifyUnstable = notifyUnstable;
        this.notifyBackToNormal = notifyBackToNormal;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }
    /**
     * Retrieve the Yammer group ID.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getGroupId() {
        return groupId;
    }

    public boolean isNotifySuccess() {
        return notifySuccess;
    }

    public boolean isNotifyUnstable() {
        return notifyUnstable;
    }

    public boolean isNotifyFailure() {
        return notifyFailure;
    }

    public boolean isNotifyBackToNormal() {
        return notifyBackToNormal;
    }

    public boolean isNotifyAborted() {
        return notifyAborted;
    }

    public boolean isNotifyNotBuilt() {
        return notifyNotBuilt;
    }

    public YammerService newYammerService() {
        return new YammerService(getDescriptor().getApiToken(), getGroupId());
    }

    public String getBuildServerUrl() {
        return getDescriptor().getBuildServerUrl();
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return true;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    /**
     * Descriptor for {@link YamkinsPlugin}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private String apiToken;
        private String buildServerUrl;

        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'groupId'.
         * @param value What the user has typed.
         * @return Returns 'ok' when the passed group ID was numeric.
         */
        @SuppressWarnings("UnusedDeclaration")
        public FormValidation doCheckGroupId(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a group ID.");
            if (!StringUtils.isNumeric(value))
                return FormValidation.warning("The group ID should be numeric.");
            return FormValidation.ok();
        }

        /**
         * Indicates that this builder can be used with all kinds of project types
         * by always returning {@code true}.
         * @param aClass project type
         * @return always returns {@code true}
         */
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        /**
         * Returns the human readable definition of this post-build step.
         * @return display name
         */
        public String getDisplayName() {
            return "Yammer Notification";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            apiToken = formData.getString("apiToken");
            buildServerUrl = formData.getString("buildServerUrl");
            save();
            return super.configure(req, formData);
        }

        /**
         * Returns the globally configured Yammer API token.
         * @return Yammer REST API token
         */
        public String getApiToken() {
            return apiToken;
        }

        /**
         * Returns the globally configured build server URL.
         * @return Jenkins build server URL
         */
        public String getBuildServerUrl() {
            return buildServerUrl;
        }
    }

}

