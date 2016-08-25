package com.flowdock.jenkins;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.scm.ChangeLogSet.Entry;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jenkins.model.JenkinsLocationConfiguration;

public class ChatMessage extends FlowdockMessage {
    protected String externalUserName;
    private Set<String> userToNotify;

    public ChatMessage() {
        this.externalUserName = "Jenkins";
        this.userToNotify = new HashSet<String>();
    }

    public void setExternalUserName(String externalUserName) {
        this.externalUserName = externalUserName;
    }
    
    public void addUserToNotify(String username){
    	this.userToNotify.add(username);
    }

    public String asPostData() throws UnsupportedEncodingException {
        StringBuilder postData = new StringBuilder();
        StringBuilder newContent = new StringBuilder(content);
        if (this.userToNotify.size()>0){
        	for (String s:this.userToNotify){
        		newContent.append(" @" + s);
        	}
        }
        postData.append("content=").append(urlEncode(newContent.toString()));
        postData.append("&external_user_name=").append(urlEncode(externalUserName));
        postData.append("&tags=").append(urlEncode(removeWhitespace(tags)));
        return postData.toString();
    }

    public static ChatMessage fromBuild(AbstractBuild build, BuildResult buildResult, BuildListener listener) {
        ChatMessage msg = new ChatMessage();
        StringBuilder content = new StringBuilder();

        String projectName = "";
        String configuration = "";
        if(build.getProject().getRootProject() != build.getProject()) {
            projectName = build.getProject().getRootProject().getDisplayName();
            configuration = " on " + build.getProject().getDisplayName();
        } else {
            projectName = build.getProject().getDisplayName();
        }

        //Jenkins.getInstance().getRootUrl() always return null, use JenkinsLocationConfiguration instead
        //String rootUrl = Hudson.getInstance().getRootUrl();
        
        JenkinsLocationConfiguration globalConfig = new JenkinsLocationConfiguration();
        String rootUrl = globalConfig.getUrl();

        String buildLink = (rootUrl == null) ? null : rootUrl + build.getUrl();
        boolean hasLink = buildLink != null;
        String buildNo = build.getDisplayName().replaceAll("#", "");

        if(build.getResult() == Result.SUCCESS) {
            content.append(":white_check_mark:");
        }
        else if(build.getResult() == Result.UNSTABLE) {
            content.append(":heavy_exclamation_mark:");
        }
        else if(build.getResult() == Result.FAILURE) {
            content.append(":x:");
        }
        else if(build.getResult() == Result.ABORTED) {
            content.append(":no_entry_sign:");
        }
        else if(build.getResult() == Result.NOT_BUILT) {
            content.append(":o:");
        }
        if(hasLink) {
            content.append("[");
        }
        content.append(projectName + configuration).append(" build ").append(buildNo);
        content.append(" **").append(buildResult.getHumanResult()).append("**");
        if(hasLink) {
            content.append("]");
            content.append("(" + buildLink + ")");
        }
        
        msg.setContent(content.toString());
                
        return msg;
    }
}
