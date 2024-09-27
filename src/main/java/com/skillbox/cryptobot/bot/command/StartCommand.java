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
import java.util.UUID;

/**
 * Обработка команды начала работы с ботом
 */

@Slf4j
@Service
@AllArgsConstructor
public class StartCommand implements IBotCommand {

    private final CryptoCurrencyRepository repository;

    @Override
    public String getCommandIdentifier() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "Запускает бота";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());

        answer.setText("""
                Привет! Данный бот помогает отслеживать стоимость биткоина.
                Поддерживаемые команды:
                 /get_price - получить стоимость биткоина
                """);

        try {

            if (!repository.existsByAndTelegramUserId(message.getFrom().getId())) {
                repository.save(this.createNewSubscriber(message));
            }

            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error occurred in /start command", e);
        }
    }

    private Subscribers createNewSubscriber(Message message) {

        new Subscribers();
        return Subscribers.builder()
                .userUUID(UUID.randomUUID())
                .telegramUserId(message.getFrom().getId())
                .userSubscribedBitCoinPrice(null)
                .build();
    }
}