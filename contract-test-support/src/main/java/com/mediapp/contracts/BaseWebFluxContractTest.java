package com.mediapp.contracts;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import io.restassured.module.webtestclient.RestAssuredWebTestClient;

/**
 * Base class for reactive services contract tests.
 */
@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("contract-test")
public abstract class BaseWebFluxContractTest {

    @Autowired
    protected WebTestClient webTestClient;

    @BeforeEach
    void configureRestAssured() {
        RestAssuredWebTestClient.webTestClient(webTestClient);
    }
}
