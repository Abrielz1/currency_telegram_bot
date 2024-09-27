package com.skillbox.cryptobot.service;

import com.skillbox.cryptobot.client.BinanceClient;
import com.skillbox.cryptobot.repository.CryptoCurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoCurrencyService {

    private final AtomicReference<Double> price = new AtomicReference<>();

    private final BinanceClient client;

    private final CryptoCurrencyRepository cryptoCurrencyRepository;

    public double getBitcoinPrice() throws IOException {
        if (price.get() == null) {
            price.set(client.getBitcoinPrice());
        }
        return price.get();
    }
}
