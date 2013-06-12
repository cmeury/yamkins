package meury.com.yamkins;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import hudson.tasks.Publisher;

import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Cedric
 * Date: 6/12/13
 * Time: 8:33 PM
 * To change this template use File | Settings | File Templates.
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
            if (publisher instanceof YamkinsNotifier) {
                return new ActiveNotifier((YamkinsNotifier) publisher);
            }
        }
        return new DisabledNotifier();
    }

}
