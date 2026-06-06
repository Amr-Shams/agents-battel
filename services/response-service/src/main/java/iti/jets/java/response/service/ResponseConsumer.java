package iti.jets.java.response.service;

import iti.jets.java.response.domain.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class ResponseConsumer {

    @Autowired
    private ResponseService responseService;

    @Bean
    public Consumer<ResponseMessage> responseInput() {
        return responseService::storeResponse;
    }
}
