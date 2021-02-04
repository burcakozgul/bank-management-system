package org.kodluyoruz.mybank.models;

public class CreateCreditCardResponse {
    private double spendingLimit;
    private double balance;
    private long cardNumber;
    private int cvvNumber;
    private String cardHolder;
    private int expiredMonth;
    private int expiredYear;

    public double getSpendingLimit() {
        return spendingLimit;
    }

    public void setSpendingLimit(double spendingLimit) {
        this.spendingLimit = spendingLimit;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public long getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(long cardNumber) {
        this.cardNumber = cardNumber;
    }

    public int getCvvNumber() {
        return cvvNumber;
    }

    public void setCvvNumber(int cvvNumber) {
        this.cvvNumber = cvvNumber;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    public int getExpiredMonth() {
        return expiredMonth;
    }

    public void setExpiredMonth(int expiredMonth) {
        this.expiredMonth = expiredMonth;
    }

    public int getExpiredYear() {
        return expiredYear;
    }

    public void setExpiredYear(int expiredYear) {
        this.expiredYear = expiredYear;
    }
}
