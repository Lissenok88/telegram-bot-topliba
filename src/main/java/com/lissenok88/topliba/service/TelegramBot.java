package com.lissenok88.topliba.service;

import com.lissenok88.topliba.config.BotConfig;
import com.lissenok88.topliba.job.BookParser;
import com.lissenok88.topliba.model.Book;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private static final String COMMAND_START = "/start";
    private static final String SEARCH_BUTTON = "Найти книгу.";
    private static final String START_MESSAGE1 = "Это телеграмбот для поиска и скачивания книг с сайта.";
    private static final String START_MESSAGE2 = "[Topliba](https://topliba.com/)";
    private static final String START_MESSAGE3 = "Чтобы найти книгу нажмите кнопку \"Найти книгу\"";
    private static final String MESSAGE_SEARCH_BUTTON = "Напишите без ошибок название книги или имя автора.";
    private static final String SEARCH_MESSAGE = "Запрос принят в обработку: ожидайте";
    private static final String SEARCH_ERROR = "Искомая книга или автор не найден.";
    private static final String DOWNLOAD_FILE_ERROR = "Что-то пошло не так. Невозможно скачать файл.";
    private final Map<String, List<Book>> listRequest = new HashMap<>();
    final BotConfig config;

    public TelegramBot(BotConfig config) {
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.botName;
    }

    @Override
    public String getBotToken() {
        return config.token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.getMessage() != null && update.getMessage().hasText()) {
            parseMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            String str = update.getCallbackQuery().getData();
            parseButton(str, update.getCallbackQuery());
        }
    }

    private void parseMessage(Message getMessage) {
        MessageProcessing messageProcessing = new MessageProcessing();
        try {
            switch (getMessage.getText()) {
                case COMMAND_START -> execute(messageProcessing.message(getMessage, START_MESSAGE1 +
                        System.lineSeparator().repeat(2) + START_MESSAGE2 +
                        System.lineSeparator().repeat(2) + START_MESSAGE3));
                case SEARCH_BUTTON -> messageProcessing.message(getMessage, MESSAGE_SEARCH_BUTTON);
                default -> {
                    messageProcessing.message(getMessage, SEARCH_MESSAGE);
                    List<Book> foundBooks = new ArrayList<>(BookParser.parser(getMessage.getText()));
                    listRequest.put(getMessage.getText(), foundBooks);
                    if (!foundBooks.isEmpty()) {
                        execute(messageProcessing.messageListBooks(getMessage, foundBooks));
                    } else {
                        messageProcessing.message(getMessage, SEARCH_ERROR);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void parseButton(String selectButton, CallbackQuery callbackQuery) {
        MessageProcessing messageProcessing = new MessageProcessing();
        try {
            if (selectButton.contains("fb2")) {
                try {
                    execute(messageProcessing.messageDownloadFile(callbackQuery.getMessage(), callbackQuery.getData()));
                } catch (Exception e) {
                    execute(messageProcessing.message(callbackQuery.getMessage(), DOWNLOAD_FILE_ERROR));
                    log.error(e.getMessage());
                }
            } else if (selectButton.contains("->") || (selectButton.contains("<-"))) {
                execute(messageProcessing.editMessageListBooks(callbackQuery.getData(), callbackQuery.getMessage(), listRequest));
            } else {
                execute(messageProcessing.deleteMessage(callbackQuery.getMessage()));
                execute(messageProcessing.messageAboutBook(callbackQuery.getMessage().getChatId().toString(),
                        BookParser.fillElements(callbackQuery.getData())));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
