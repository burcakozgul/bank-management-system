package org.kodluyoruz.mybank.models;

import org.kodluyoruz.mybank.entities.ProcessName;

import java.time.LocalDateTime;

public class Expenses {
    private ProcessName processName;
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
