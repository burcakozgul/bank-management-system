package org.kodluyoruz.mybank.models;

import java.util.ArrayList;
import java.util.List;

public class GetCreditCardReceiptResponse {
    List<Expenses> expenses;

    public List<Expenses> getExpenses() {
        if (expenses ==null){
            return expenses = new ArrayList<>();
        }
        return expenses;
    }

    public void setExpenses(List<Expenses> expenses) {
        this.expenses = expenses;
    }
}
