package com.burcak.mybank.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.burcak.mybank.entities.ProcessName;

import java.time.LocalDateTime;

public class Expenses {
    private ProcessName processName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private LocalDateTime dateTime;
    private double amount;

    public ProcessName getProcessName() {
        return processName;
    }

    public void setProcessName(ProcessName processName) {
        this.processName = processName;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
