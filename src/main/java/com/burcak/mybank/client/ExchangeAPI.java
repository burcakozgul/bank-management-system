package org.kodluyoruz.mybank.client;

import org.kodluyoruz.mybank.entities.Currency;
import org.kodluyoruz.mybank.entities.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ExchangeAPI {

    @Value("${exchange.url}")
    private String exchangeURL;

    @Autowired
    RestTemplate restTemplate;

    public Exchange exchange(Currency senderCurrency) {
        String generatedUrl = exchangeURL + senderCurrency;
        return restTemplate.getForObject(generatedUrl,Exchange.class);
    }
}
