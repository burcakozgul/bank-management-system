package org.kodluyoruz.mybank.controllers;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.kodluyoruz.mybank.models.ShoppingRequest;
import org.kodluyoruz.mybank.services.CreditCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@WebMvcTest(CreditCardController.class)
class CreditCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreditCardController creditCardController;

    private final Gson gson = new Gson();

    @Test
    void shoppingByCreditCard() throws Exception {
        ShoppingRequest request = new ShoppingRequest();
        request.setCardHolderName("burcak");
        request.setCardNumber(5864254156987523L);
        request.setCvvNumber(512);
        request.setExpiredMonth(2);
        request.setExpiredYear(2023);
        request.setAmount(10);

        String inputInJson = gson.toJson(request);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/creditCard/shopping")
                .content(inputInJson)
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}