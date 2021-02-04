package org.kodluyoruz.mybank.entities;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class CreditCardReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ProcessName processName;
    private Long creditCardId;
    private LocalDateTime date;
    private double amount;

    public Long getCreditCardId() {
        return creditCardId;
    }

    public void setCreditCardId(Long creditCardId) {
        this.creditCardId = creditCardId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProcessName getProcessName() {
        return processName;
    }

    public void setProcessName(ProcessName processName) {
        this.processName = processName;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
