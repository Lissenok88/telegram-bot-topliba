package com.lissenok88.topliba.service;

import com.lissenok88.topliba.config.BotConfig;
import com.lissenok88.topliba.job.ToplibaParser;
import com.lissenok88.topliba.model.Book;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.HashMap;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private static final String COMMAND_START = "/start";
    private static final String SEARCH_BUTTON = "Найти книгу.";
    private static final String START_MESSAGE1 = "Это телеграмбот для поиска и скачивания книг с сайта.";
    private static final String START_MESSAGE2 = "[Topliba](https://topliba.com/)";
    private static final String START_MESSAGE3 = "Чтобы найти книгу нажмите кнопку \"Найти книгу\"";
    private static final String MESSAGE_SEARCH_BUTTON = "Напишите без ошибок название книги или имя автора.";
    private static final String SEARCH_MESSAGE = "Ищем книги по запросу: ";
    private static final String SEARCH_ERROR = "Искомая книга или автор не найден.";
    private final HashMap<String, ArrayList<Book>> listRequest = new HashMap<>();
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
        MessageProcessing messageProcessing = new MessageProcessing(config);
        switch (getMessage.getText()) {
            case COMMAND_START -> messageProcessing.message(getMessage, START_MESSAGE1 +
                    System.lineSeparator().repeat(2) + START_MESSAGE2 +
                    System.lineSeparator().repeat(2) + START_MESSAGE3);
            case SEARCH_BUTTON -> messageProcessing.message(getMessage, MESSAGE_SEARCH_BUTTON);
            default -> {
                messageProcessing.message(getMessage, SEARCH_MESSAGE + getMessage.getText());
                ArrayList<Book> foundBooks = new ArrayList<>(ToplibaParser.parser(getMessage.getText()));
                listRequest.put(getMessage.getText(), foundBooks);
                if (!foundBooks.isEmpty()) {
                    messageProcessing.messageListBooks(getMessage, foundBooks);
                } else {
                    messageProcessing.message(getMessage, SEARCH_ERROR);
                }
            }
        }
    }

    private void parseButton(String selectButton, CallbackQuery callbackQuery) {
        MessageProcessing messageProcessing = new MessageProcessing(config);
        if (selectButton.contains("fb2")) {
            messageProcessing.messageDownloadFile(callbackQuery.getMessage(), callbackQuery.getData());
        } else if (selectButton.contains("->") || (selectButton.contains("<-"))) {
            messageProcessing.editMessageListBooks(callbackQuery.getData(), callbackQuery.getMessage(), listRequest);
        } else {
            messageProcessing.messageAboutBook(callbackQuery.getMessage().getChatId().toString(),
                    ToplibaParser.fillElements(callbackQuery.getData()));
        }
    }
}
