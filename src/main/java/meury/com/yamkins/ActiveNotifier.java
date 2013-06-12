package meury.com.yamkins;

import hudson.Launcher;
import hudson.Util;
import hudson.model.*;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.AffectedFile;
import hudson.scm.ChangeLogSet.Entry;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

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
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleted(AbstractBuild r) {
    }

    @Override
    public void finalized(AbstractBuild r) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private void notifyStart(AbstractBuild build, String message) {
        notifier.newYammerService().postMessage(message);
    }

    @Override
    public void completed(AbstractBuild r) {


        AbstractProject<?, ?> project = r.getProject();
//        HipChatNotifier.HipChatJobProperty jobProperty = project.getProperty(HipChatNotifier.HipChatJobProperty.class);
        Result result = r.getResult();

        YammerService service = notifier.newYammerService();
        AbstractBuild<?, ?> previousBuild = project.getLastBuild().getPreviousBuild();
        Result previousResult = (previousBuild != null) ? previousBuild.getResult() : Result.SUCCESS;
        String message = getBuildStatusMessage(r);
//        if ((result == Result.ABORTED && jobProperty.getNotifyAborted())
//                || (result == Result.FAILURE && jobProperty.getNotifyFailure())
//                || (result == Result.NOT_BUILT && jobProperty.getNotifyNotBuilt())
//                || (result == Result.SUCCESS && previousResult == Result.FAILURE && jobProperty.getNotifyBackToNormal())
//                || (result == Result.SUCCESS && jobProperty.getNotifySuccess())
//                || (result == Result.UNSTABLE && jobProperty.getNotifyUnstable())) {
//            service.postMessage(message); //, getBuildColor(r));
//        }


        Response response = service.postMessage(message);
        int status = response.getStatus();
        if(status == 201) {
            logger.info("Sent build message to Yammer group: " + message);
        } else {
            logger.warning("Unable to send message to Yammer group HTTP status code: " + status);
            logger.warning(response.readEntity(String.class));
        }

    }

    String getChanges(AbstractBuild r) {
        if (!r.hasChangeSetComputed()) {
            logger.info("No change set computed...");
            return null;
        }
        ChangeLogSet changeSet = r.getChangeSet();
        List<Entry> entries = new LinkedList<Entry>();
        Set<AffectedFile> files = new HashSet<AffectedFile>();
        for (Object o : changeSet.getItems()) {
            Entry entry = (Entry) o;
            logger.info("Entry " + o);
            entries.add(entry);
            files.addAll(entry.getAffectedFiles());
        }
        if (entries.isEmpty()) {
            logger.info("Empty change...");
            return null;
        }
        Set<String> authors = new HashSet<String>();
        for (Entry entry : entries) {
            authors.add(entry.getAuthor().getDisplayName());
        }
        MessageBuilder message = new MessageBuilder(notifier, r);
        message.append("Started by changes from ");
        message.append(StringUtils.join(authors, ", "));
        message.append(" (");
        message.append(files.size());
        message.append(" file(s) changed)");
        return message.toString();
//        return message.appendOpenLink().toString();
    }

    static String getBuildColor(AbstractBuild r) {
        Result result = r.getResult();
        if (result == Result.SUCCESS) {
            return "green";
        } else if (result == Result.FAILURE) {
            return "red";
        } else {
            return "yellow";
        }
    }

    String getBuildStatusMessage(AbstractBuild r) {
        MessageBuilder message = new MessageBuilder(notifier, r);
        message.appendStatusMessage();
        message.appendDuration();
        return message.toString();
//        return message.appendOpenLink().toString();
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
            if (r.isBuilding()) {
                return "Starting...";
            }
            Result result = r.getResult();
            Run previousBuild = r.getProject().getLastBuild().getPreviousBuild();
            Result previousResult = (previousBuild != null) ? previousBuild.getResult() : Result.SUCCESS;
            if (result == Result.SUCCESS && previousResult == Result.FAILURE) return "Back to normal";
            if (result == Result.SUCCESS) return "Success";
            if (result == Result.FAILURE) return "<b>FAILURE</b>";
            if (result == Result.ABORTED) return "ABORTED";
            if (result == Result.NOT_BUILT) return "Not built";
            if (result == Result.UNSTABLE) return "Unstable";
            return "Unknown";
        }

        public MessageBuilder append(String string) {
            message.append(string);
            return this;
        }

        public MessageBuilder append(Object string) {
            message.append(string.toString());
            return this;
        }

        private MessageBuilder startMessage() {
            message.append(build.getProject().getDisplayName());
            message.append(" - ");
            message.append(build.getDisplayName());
            message.append(" ");
            return this;
        }

//        public MessageBuilder appendOpenLink() {
//            String url = notifier.getBuildServerUrl() + build.getUrl();
//            message.append(" (<a href='").append(url).append("'>Open</a>)");
//            return this;
//        }

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
