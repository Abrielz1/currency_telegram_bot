package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.client.entity.Subscribers;
import com.skillbox.cryptobot.repository.CryptoCurrencyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Обработка команды вывод информации об имеющейся подписке на курс валюты
 */

@Slf4j
@Service
@AllArgsConstructor
public class GetSubscriptionCommand implements IBotCommand {

    private final CryptoCurrencyRepository currencyRepository;

    @Override
    public String getCommandIdentifier() {
        return "get_subscription";
    }

    @Override
    public String getDescription() {
        return "Возвращает текущую подписку";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {

        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());

       Subscribers userFromDB = currencyRepository.findFirstByTelegramUserId(message.getFrom().getId()).get();

        if (currencyRepository.existsByTelegramUserId(message.getFrom().getId())
                && userFromDB.getUserSubscribedBitCoinPrice() != null) {

            answer.setText("Вы подписаны на стоимость биткоина %f USD"
                    .formatted(userFromDB.getUserSubscribedBitCoinPrice()));

        } else {

            answer.setText("Активные подписки отсутствуют");
        }

        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error occurred in /subscribe command", e);
        }
    }
}