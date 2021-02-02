package org.kodluyoruz.mybank.models;

public class PayLoanFromAtmRequest {
    private Long creditCardId;
    private double amount;

    public Long getCreditCardId() {
        return creditCardId;
    }

    public void setCreditCardId(Long creditCardId) {
        this.creditCardId = creditCardId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
