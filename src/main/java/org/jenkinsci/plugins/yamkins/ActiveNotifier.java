package org.jenkinsci.plugins.yamkins;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;

import javax.ws.rs.core.Response;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Analyzes the build result and sends a corresponding message to the group.
 */
@SuppressWarnings("rawtypes")
public class ActiveNotifier implements FineGrainedNotifier {

    private static final Logger logger = Logger.getLogger(ActiveNotifier.class.getName());

    private YamkinsPlugin notifier;

    public ActiveNotifier(YamkinsPlugin notifier) {
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
    public void completed(AbstractBuild build) {
        AbstractProject<?, ?> project = build.getProject();
        Result result = build.getResult();
        AbstractBuild<?, ?> previousBuild = project.getLastBuild().getPreviousBuild();
        Result previousResult = (previousBuild != null) ? previousBuild.getResult() : Result.SUCCESS;


        if ((result == Result.ABORTED && notifier.isNotifyAborted() )
                || (result == Result.FAILURE && notifier.isNotifyFailure())
                || (result == Result.NOT_BUILT && notifier.isNotifyNotBuilt())
                || (result == Result.SUCCESS && previousResult == Result.FAILURE && notifier.isNotifyBackToNormal())
                || (result == Result.SUCCESS && notifier.isNotifySuccess())
                || (result == Result.UNSTABLE && notifier.isNotifyUnstable())) {

            YammerService service = notifier.newYammerService();

            Response response = null;

            if(notifier.getDescriptor().useOpenGraphMessageFormat()) {
                // maybe null
                String replyToMessageId = getReplyToMesage(build.getProject());

                URL buildUrl = createBuildUrl(notifier.getBuildServerUrl(), build.getUrl());
                Map<OpenGraph, String> ogMap = createOpenGraphProperties(notifier, build);

                response = service.postMessage(buildUrl, ogMap, replyToMessageId);
            }else {
                String message = getBuildStatusMessage(build);
                response = service.postMessage(message);
            }

            int status = response.getStatus();


            if(status == 201) {
                logger.info("Sent build message to Yammer group: ");

                String id = extractMessageId(response);

                try {
                    ReplyToMessageIdJobProperty p = (ReplyToMessageIdJobProperty) project.getProperty(ReplyToMessageIdJobProperty.class);
                    if (p == null) {
                        p = new ReplyToMessageIdJobProperty("");
                    }

                    if(result == Result.SUCCESS)
                        p.setReplyMessageId("");
                    else
                        p.setReplyMessageId(id);

                    project.addProperty(p);
                    project.save();

                } catch (IOException e) {
                   logger.throwing(ActiveNotifier.class.getName(), "completed(AbstractBuild)", e);
                }
            } else {
                logger.warning("Unable to send message to Yammer group HTTP status code: " + status);
                logger.warning(response.readEntity(String.class));
            }
        }
    }

    private String extractMessageId(Response response) {

        String location = response.getHeaderString("Location");
        logger.info(location);
        return location.substring(location.lastIndexOf("/")+1, location.length());
    }

    /**
     * @param project projectconfiguration with optional replyToMessageId
     * @return replyToMessageId if configured in project, otherwise null
     */
    private String getReplyToMesage(AbstractProject project) {

        ReplyToMessageIdJobProperty property = (ReplyToMessageIdJobProperty) project.getProperty(ReplyToMessageIdJobProperty.class);

        return property != null? property.getReplyToMessageId(): null;
    }

    protected String getBuildStatusMessage(AbstractBuild r) {
        MessageBuilder message = new MessageBuilder(notifier, r);
        message.appendStatusMessage();
        message.appendDuration();
        return message.appendOpenLink().toString();
    }

    private URL createBuildUrl(String buildServerUrl, String buildUrl) {
        try {
            return new URL (buildServerUrl+buildUrl);
        } catch (MalformedURLException e) {
            logger.info("createBuildUrl failed - "+e.getMessage());
            return null;
        }
    }


    private Map<OpenGraph, String> createOpenGraphProperties(YamkinsPlugin plugin, AbstractBuild build) {

        Map<OpenGraph, String> map = new HashMap<OpenGraph, String>();
        map.put(OpenGraph.TITLE, createTitle(build));

        if(notifier.getDescriptor().getAttachOpenGraphImage())
            map.put(OpenGraph.IMAGE, createDummyImageUrl(build));

        map.put(OpenGraph.DESCRIPTION, build.getDurationString()+" - "+build.getTimestampString());

        return map;
    }


    private String createDummyImageUrl(AbstractBuild build) {

        Color background = build.getResult().color.getBaseColor();
        Color forground = Color.WHITE;

        String text = MessageBuilder.getStatusMessage(build);

        if(build.getResult() == Result.SUCCESS) {
           background = Color.GREEN;
           forground = Color.BLACK;
        }
        else if (build.getResult() == Result.UNSTABLE) {
            forground = Color.BLACK;
            background = Color.YELLOW;
        }

        return createDummyImageUrl(text, 300, 300, forground, background);
    }

    private  String createTitle(AbstractBuild build) {
        StringBuilder builder = new StringBuilder();
        builder.append(build.getProject().getDisplayName()).append(" - ");
        builder.append(build.getDisplayName()).append(" : ").append(MessageBuilder.getStatusMessage(build));
        return builder.toString();

    }

    private  String createDummyImageUrl(String text, int width, int height, Color foreground, Color background){
        StringBuilder builder = new StringBuilder("http://dummyimage.com/");
        builder.append(width).append("x").append(height).append("/");
        builder.append(Integer.toHexString(background.getRGB()).substring(2)).append("/");
        builder.append(Integer.toHexString(foreground.getRGB()).substring(2)).append(".png");
        builder.append("&text=").append(URLEncoder.encode(text));
        return builder.toString();
    }


    public static class MessageBuilder {
        private StringBuffer message;
        private YamkinsPlugin notifier;
        private AbstractBuild build;


        public MessageBuilder(YamkinsPlugin aNotifier, AbstractBuild aBuild) {
            notifier = aNotifier;
            build = aBuild;
            message = new StringBuffer();
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
            message.append(" - ").append(url);
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
