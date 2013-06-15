package meury.com.yamkins;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.tasks.Publisher;

import java.util.Map;

/**
 * Set up a post build listener.
 */
@Extension
public class YamkinsListener extends RunListener<AbstractBuild> {

    @Override
    public void onCompleted(AbstractBuild r, TaskListener listener) {
        getNotifier(r.getProject()).completed(r);
        super.onCompleted(r, listener);
    }

    @SuppressWarnings("unchecked")
    FineGrainedNotifier getNotifier(AbstractProject project) {
        Map<Descriptor<Publisher>, Publisher> map = project.getPublishersList().toMap();
        for (Publisher publisher : map.values()) {
            if (publisher instanceof YamkinsPlugin) {
                return new ActiveNotifier((YamkinsPlugin) publisher);
            }
        }
        return new DisabledNotifier();
    }

}
