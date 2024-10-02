package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.bot.CryptoBot;
import com.skillbox.cryptobot.client.entity.Subscribers;
import com.skillbox.cryptobot.repository.CryptoCurrencyRepository;
import com.skillbox.cryptobot.service.CryptoCurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Обработка команды подписки на курс валюты и создание автоматической скачки цены bitCoin и её рассылки
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscribeCommand implements IBotCommand {

    private final CryptoCurrencyService currencyService;

    private final CryptoCurrencyRepository currencyRepository;

    private final CryptoBot bot;

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
            this.alarm();
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

    @Scheduled(fixedRate = 2, timeUnit = TimeUnit.MINUTES)
    private void scheduledGetterCurrentBitcoinPrice() {
        this.subscribersChecker(this.getterBitCoinPrice());
    }

    private Double getterBitCoinPrice() {
        try {
            return currencyService.getBitcoinPrice();
        } catch (IOException e) {
            System.out.println("No price new for now");
        }
        return 0.0d;
    }

    private List<Subscribers> subscribersChecker(Double currentBitCoinPrice) {
        List<Subscribers> list = currencyRepository.findAll();

        return list.stream()
                .filter(user -> user.getUserSubscribedBitCoinPrice() > currentBitCoinPrice)
                .toList();
    }

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
    private void alarm() {
        this.subscribersChecker(this.getterBitCoinPrice())
                .forEach(subscribers ->
                        this.alarmUserOfLowerBitCoinPrice(subscribers.getTelegramUserId()));
    }

    private void alarmUserOfLowerBitCoinPrice(Long userTelegramId) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(userTelegramId));
        message.setText("Пора покупать");
        this.executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            bot.execute(message);
        } catch (RuntimeException | TelegramApiException e) {
            e.printStackTrace();
        }
    }
}