package meury.com.yamkins;

import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Yamkins plugin builder. When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked.
 * @author Cedric Meury
 * @author Kohsuke Kawaguchi
 */
public class YamkinsBuilder extends Builder {

    private final String groupId;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public YamkinsBuilder(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Retrieve the Yammer group ID.
     */
    public String getGroupId() {
        return groupId;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        YammerService service = new YammerService(getDescriptor().getApiToken());
        String message = "Build " + build.getNumber() + " in progress";
        Response response = service.postMessage(message, groupId);
        int status = response.getStatus();
        if(status == 201) {
            listener.getLogger().println("Sent message to Yammer group ID " + groupId + ": " + message);
            return true;
        } else {
            listener.getLogger().println("Unable to send message to Yammer group ID " + groupId + ", HTTP status code: " + status);
            listener.getLogger().println(response.readEntity(String.class));
            return false;
        }
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link YamkinsBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private String apiToken;

        /**
         * Performs on-the-fly validation of the form field 'groupId'.
         *
         * @param value What the user has typed.
         * @return Returns 'ok' when the passed group ID was numeric.
         */
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
         *
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
            save();
            return super.configure(req, formData);
        }

        /**
         * Returns the globally configured Yammer API token.
         *
         * @return Yammer REST API token
         */
        public String getApiToken() {
            return apiToken;
        }

    }

}

