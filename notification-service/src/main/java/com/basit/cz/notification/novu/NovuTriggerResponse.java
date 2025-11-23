package com.basit.cz.notification.novu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NovuTriggerResponse {

    // flat response case
    public Boolean acknowledged;
    public String status;
    public String transactionId;
    public List<String> error;

    // handle { "data": { ... } } case
    @JsonProperty("data")
    private void unpackData(Data data) {
        if (data == null) return;
        // only overwrite if top-level fields are null
        if (this.acknowledged == null) this.acknowledged = data.acknowledged;
        if (this.status == null) this.status = data.status;
        if (this.transactionId == null) this.transactionId = data.transactionId;
        if (this.error == null) this.error = data.error;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        public Boolean acknowledged;
        public String status;
        public String transactionId;
        public List<String> error;
    }
}