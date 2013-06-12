package meury.com.yamkins;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;

import javax.ws.rs.core.Response;
import java.util.logging.Logger;

/**
 * Analyzes the build result and sends a corresponding message to the group.
 */
@SuppressWarnings("rawtypes")
public class ActiveNotifier implements FineGrainedNotifier {

    private static final Logger logger = Logger.getLogger(ActiveNotifier.class.getName());

    private YamkinsNotifier notifier;

    public ActiveNotifier(YamkinsNotifier notifier) {
        super();
        this.notifier = notifier;
    }

    @Override
    public void started(AbstractBuild r) {
        // no-op
    }

    @Override
    public void deleted(AbstractBuild r) {
        // no-op
    }

    @Override
    public void finalized(AbstractBuild r) {
        // no-op
    }

    @Override
    public void completed(AbstractBuild r) {
        AbstractProject<?, ?> project = r.getProject();
        Result result = r.getResult();
        AbstractBuild<?, ?> previousBuild = project.getLastBuild().getPreviousBuild();
        Result previousResult = (previousBuild != null) ? previousBuild.getResult() : Result.SUCCESS;
        String message = getBuildStatusMessage(r);
        if ((result == Result.ABORTED && notifier.isNotifyAborted() )
                || (result == Result.FAILURE && notifier.isNotifyFailure())
                || (result == Result.NOT_BUILT && notifier.isNotifyNotBuilt())
                || (result == Result.SUCCESS && previousResult == Result.FAILURE && notifier.isNotifyBackToNormal())
                || (result == Result.SUCCESS && notifier.isNotifySuccess())
                || (result == Result.UNSTABLE && notifier.isNotifyUnstable())) {
            YammerService service = notifier.newYammerService();
            Response response = service.postMessage(message);
            int status = response.getStatus();
            if(status == 201) {
                logger.info("Sent build message to Yammer group: " + message);
            } else {
                logger.warning("Unable to send message to Yammer group HTTP status code: " + status);
                logger.warning(response.readEntity(String.class));
            }
        }
    }

    String getBuildStatusMessage(AbstractBuild r) {
        MessageBuilder message = new MessageBuilder(notifier, r);
        message.appendStatusMessage();
        message.appendDuration();
        return message.appendOpenLink().toString();
    }

    public static class MessageBuilder {
        private StringBuffer message;
        private YamkinsNotifier notifier;
        private AbstractBuild build;

        public MessageBuilder(YamkinsNotifier notifier, AbstractBuild build) {
            this.notifier = notifier;
            this.message = new StringBuffer();
            this.build = build;
            startMessage();
        }

        public MessageBuilder appendStatusMessage() {
            message.append(getStatusMessage(build));
            return this;
        }

        static String getStatusMessage(AbstractBuild r) {
            Result result = r.getResult();
            Run previousBuild = r.getProject().getLastBuild().getPreviousBuild();
            Result previousResult = (previousBuild != null) ? previousBuild.getResult() : Result.SUCCESS;
            if (result == Result.SUCCESS && previousResult == Result.FAILURE) return "Back to normal";
            if (result == Result.SUCCESS) return "Success";
            if (result == Result.FAILURE) return "FAILURE";
            if (result == Result.ABORTED) return "ABORTED";
            if (result == Result.NOT_BUILT) return "Not built";
            if (result == Result.UNSTABLE) return "Unstable";
            return "Unknown";
        }

        private MessageBuilder startMessage() {
            message.append(build.getProject().getDisplayName());
            message.append(" - ");
            message.append(build.getDisplayName());
            message.append(" ");
            return this;
        }

        public MessageBuilder appendOpenLink() {
            String url = notifier.getBuildServerUrl() + build.getUrl();
            message.append(" Link: ").append(url);
            return this;
        }

        public MessageBuilder appendDuration() {
            message.append(" after ");
            message.append(build.getDurationString());
            return this;
        }

        public String toString() {
            return message.toString();
        }
    }
}
