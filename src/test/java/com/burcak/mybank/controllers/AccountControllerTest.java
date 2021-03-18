package com.burcak.mybank.controllers;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import com.burcak.mybank.models.MoneyTransferRequest;
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
@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountController accountController;

    private final Gson gson = new Gson();

    @Test
    void moneyTransfer() throws Exception {
        MoneyTransferRequest request = new MoneyTransferRequest();
        request.setReceiverIban("TR123456789123456789123456");
        request.setSenderIban("TR123456789123456789123457");
        request.setAmount(80);

        String inputInJson = gson.toJson(request);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/account/moneyTransfer")
                .content(inputInJson)
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}