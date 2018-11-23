package com.testlabic.datenearu.ChatUtils;

/**
 * Created by root on 8/1/18.
 */

 public class ChatMessage {

    private String message;

    private String sentPhotoUrl;

    private String refKey;

    private String sentFrom;

    private String sentTo;

    private String sendersName;

    private Boolean messageDelivered;

    private String sendToName;

    private long sendingTime;
    
    public ChatMessage() {
    }

    public String getRefKey() {
        return refKey;
    }

    public ChatMessage(String message, String sentPhotoUrl,
                       String refKey, String sentFrom, String sentTo,
                       Boolean messageDelivered, long sendingTime, Boolean isACircle,
                       String sendersName, String sendToName) {
        this.message = message;
        this.sentPhotoUrl = sentPhotoUrl;
        this.refKey = refKey;
        this.sentFrom = sentFrom;
        this.sentTo = sentTo;
        this.messageDelivered = messageDelivered;
        this.sendingTime = sendingTime;
        this.sendersName = sendersName;
        this.sendToName = sendToName;
    }

    public String getSendToName() {
        return sendToName;
    }
    
    public String getSendersName() {
        return sendersName;
    }

    public String getMessage() {
        return message;
    }

    public String getSentPhotoUrl() {
        return sentPhotoUrl;
    }


    public String getSentFrom() {
        return sentFrom;
    }

    public String getSentTo() {
        return sentTo;
    }

    public Boolean getMessageDelivered() {
        return messageDelivered;
    }

    public long getSendingTime() {
        return sendingTime;
    }
}