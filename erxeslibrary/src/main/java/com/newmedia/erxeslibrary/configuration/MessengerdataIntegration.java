package com.newmedia.erxeslibrary.configuration;

import java.util.List;
import java.util.Map;

public class MessengerdataIntegration {
    public boolean isOnline;
    public String timezone;
    public List<String> supporterIds;
    public String knowledgeBaseTopicId;
    public String availabilityMethod;


//    public Map<String, Messages> messages;
//    public Messages messages;
    public class Messages{
        public String welcome,away,thank;
        public Greetings greetings;
    }
    public class Greetings{
        public String message,title;
    }
    public Map<String, Messages> messages;
    public Map<String, String> links;

    public Messages getMessages(String lan){
        return messages.get(lan);
    }
}
