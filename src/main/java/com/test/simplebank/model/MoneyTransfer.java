package com.test.simplebank.model;

import java.math.BigDecimal;

public class MoneyTransfer {
    private Long senderId;
    private Long recipientId;
    private BigDecimal value;

    public MoneyTransfer(Long senderId, Long recipientId, BigDecimal value) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.value = value;
    }

    public MoneyTransfer() {
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
