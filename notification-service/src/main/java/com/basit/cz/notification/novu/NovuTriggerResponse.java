package com.basit.cz.notification.novu;

import java.util.List;

public class NovuTriggerResponse {
    public boolean acknowledged;
    public String status;
    public List<String> error;
    public String transactionId;
}
