package com.lissenok88.topliba.job;

import com.lissenok88.topliba.model.Book;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BookParser {
    private static final Logger log = LoggerFactory.getLogger(BookParser.class);
    private static final String urlBase = "https://topliba.com/?q=";
    private static String key;
    private boolean needStop;
    private int positionCounter = 1;


    public static List<Book> parser(String message) {
        List<Book> list = new ArrayList<>();
        try {
            BookParser bookParser = new BookParser();
            key = message.replace(" ", "%20");
            list = new ArrayList<>(bookParser.startParser());
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return list;
    }

    private List<Book> startParser() {
        int page = 0;
        List<Book> parseData = new ArrayList<>();
        while (true) {
            String pageUrl = nextPageUrl(page);
            page++;
            List<Book> elements = getElements(pageUrl);
            parseData.addAll(elements);
            if (elements.isEmpty()) {
                break;
            }
        }
        return parseData;
    }

    private String nextPageUrl(int page) {
        return urlBase + key + "&p=" + ++page;
    }

    private List<Book> getElements(String pageUrl) {
        if (positionCounter > 50) {
            needStop = true;
        }
        if (needStop) {
            return new ArrayList<>();
        }
        int position = positionCounter;
        Document doc = getHtml(pageUrl);
        Elements elements = doc.getElementsByClass("media-body");
        List<Book> element = elements.stream().map(q -> {
            Book bookInformation = new Book();
            Element titleElement1 = q.getElementsByClass("book-title").first();
            if (titleElement1 != null) {
                String element1 = "";
                element1 = titleElement1.getElementsByTag("a").text();
                Element titleElement2 = q.getElementsByClass("book-author").first();
                String element2 = "";
                if (titleElement2 != null)
                    element2 = titleElement2.getElementsByTag("a").text();
                bookInformation.setPosition(positionCounter++);
                bookInformation.setTitle(element1 + " - " + element2);
                String url = titleElement1.attr("href");
                bookInformation.setUrl(url);
            }
            return bookInformation;
        }).toList();
        element = element.stream().filter(q -> q.getPosition() > 0).toList();
        return (position != positionCounter) ? new ArrayList<>(element) : new ArrayList<>();
    }

    public static Book fillElements(String url) {
        Book book = new Book();
        Document doc = getHtml(url);
        book.setTitle(doc.getElementsByClass("book-title").first().text());
        book.setDescription(doc.getElementsByClass("description").first().text());
        String danger = doc.getElementsByClass("alert-danger").text();
        if (!danger.equals("")) {
            book.setFragment(danger + " \n\r Данный файл скачать невозможно.");
            book.setUrlFb2("");
        } else {
            String el = doc.getElementsByClass("alert-info").text();
            int index = el.indexOf("Доступен");
            if (index != -1) {
                book.setFragment("Скачать ознакомительный фрагмент");
            } else book.setFragment("Скачать файл полностью");
            String urlFb2 = doc.getElementsByAttributeValue("rel", "nofollow").first().attr("href");
            if (urlFb2 != null) {
                if (!urlFb2.contains("http"))
                    urlFb2 = "https://topliba.com" + urlFb2;
            } else
                urlFb2 = "";
            book.setUrlFb2(urlFb2);
        }
        return book;
    }

    private static Document getHtml(String url) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url)
                    .userAgent("Mozilla")
                    .timeout(5000)
                    .referrer("https://google.com")
                    .get();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return doc;
    }
}
