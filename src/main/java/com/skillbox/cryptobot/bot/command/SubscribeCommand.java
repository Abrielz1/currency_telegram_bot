package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.client.entity.Subscribers;
import com.skillbox.cryptobot.repository.CryptoCurrencyRepository;
import com.skillbox.cryptobot.service.CryptoCurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.IOException;
import java.util.UUID;

/**
 * Обработка команды подписки на курс валюты
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscribeCommand implements IBotCommand {

    private final CryptoCurrencyService currencyService;

    private final CryptoCurrencyRepository currencyRepository;

    @Override
    public String getCommandIdentifier() {
        return "subscribe";
    }

    @Override
    public String getDescription() {
        return "Подписывает пользователя на стоимость биткоина";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {

        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());

        double priceOfBitCoin = 0.0d;

        try {
            priceOfBitCoin = currencyService.getBitcoinPrice();
        } catch (IOException e) {
            System.out.println("Oops no price for this time!");
        }

        String bitCoinWishedPriceRAW = message.getText();

        priceOfBitCoin = this.bitCoinPriceParser(priceOfBitCoin,
                bitCoinWishedPriceRAW);

        answer.setText("""
                Новая подписка создана на стоимость %f USD
                 /subscribe - получить стоимость биткоина
                """.formatted(priceOfBitCoin));

        Subscribers userToSubscribe = currencyRepository.findFirstByTelegramUserId(message.getFrom().getId())
                .orElse(this.createNewUser(message.getFrom().getId()));

        userToSubscribe.setUserSubscribedBitCoinPrice(priceOfBitCoin);

        currencyRepository.saveAndFlush(userToSubscribe);

        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error occurred in /subscribe command", e);
        }
    }

    private Double bitCoinPriceParser(Double currentBitcoinPrice, String text) {

        if (text.isBlank() || !text.contains("\\d")) {

            return currentBitcoinPrice;
        } else {

            return Double.parseDouble(text.replaceAll("[^0-9.]+", ""));
        }
    }

    private Subscribers createNewUser(Long userTelegramId) {

        new Subscribers();
        return Subscribers.builder()
                .userUUID(UUID.randomUUID())
                .telegramUserId(userTelegramId)
                .userSubscribedBitCoinPrice(null)
                .build();
    }
}