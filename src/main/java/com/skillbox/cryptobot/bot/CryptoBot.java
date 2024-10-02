package com.skillbox.cryptobot.bot;

import com.skillbox.cryptobot.client.BinanceClient;
import com.skillbox.cryptobot.repository.CryptoCurrencyRepository;
import com.skillbox.cryptobot.service.CryptoCurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.List;

@Slf4j
@Service
public class CryptoBot extends TelegramLongPollingCommandBot {

    private final String botUsername;

    private final CryptoCurrencyRepository repository;

    private final CryptoCurrencyService service;

    private final BinanceClient client;

    public CryptoBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            List<IBotCommand> commandList,
            CryptoCurrencyRepository repository,
            CryptoCurrencyService service,
            BinanceClient client) {
        super(botToken);
        this.botUsername = botUsername;

        commandList.forEach(this::register);
        this.repository = repository;
        this.service = service;
        this.client = client;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
    }

}
