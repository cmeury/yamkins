package org.jenkinsci.plugins.yamkins;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class ReplyToMessageIdJobProperty extends JobProperty<AbstractProject<?, ?>> {


        String replyToMessageId;

        @DataBoundConstructor
        public ReplyToMessageIdJobProperty(String messageId) {
            replyToMessageId = messageId;
        }

        public String getReplyToMessageId() {
            return replyToMessageId;
        }

        public void setReplyMessageId(String messageId) {
            this.replyToMessageId = messageId;
    }

        @Override
        public JobPropertyDescriptor getDescriptor() {
            return DESCRIPTOR;
        }

        @Extension
        public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();



    public static class DescriptorImpl extends JobPropertyDescriptor {

        public DescriptorImpl() {
            super(ReplyToMessageIdJobProperty.class);
            load();
        }

        @Override
        public String getDisplayName() {
            return "Sets the message id of the Yammer message Jenkins posted to in case of failing or unstable build";
        }

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return AbstractProject.class.isAssignableFrom(jobType);
        }

        @Override
        public JobProperty<?> newInstance(StaplerRequest request, JSONObject formData) {
            return request.bindJSON(ReplyToMessageIdJobProperty.class, formData);
        }
    }
 }
