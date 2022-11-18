package com.lissenok88.topliba.job;

import com.lissenok88.topliba.model.Book;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ToplibaParser {
    private static final String urlBase = "https://topliba.com/?q=";
    private boolean needStop;
    private static String key;
    private int positionCounter = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(ToplibaParser.class);

    public static List<Book> parser(String message) {
        ArrayList<Book> list = new ArrayList<>();
        try {
            ToplibaParser parser = new ToplibaParser();
            key = message.replace(" ", "%20");
            list = new ArrayList<>(parser.startParser());
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
        return list;
    }

    private ArrayList<Book> startParser() {
        int page = 0;
        ArrayList<Book> parsedData = new ArrayList<>();
        while (true) {
            String pageUrl = nextPageUrl(page);
            page++;
            ArrayList<Book> elements = getElements(pageUrl);
            parsedData.addAll(elements);
            if (elements.isEmpty()) {
                break;
            }
        }
        return parsedData;
    }

    private String nextPageUrl(int page) {
        return urlBase + key + "&p=" + ++page;
    }

    private ArrayList<Book> getElements(String pageUrl) {
        if (positionCounter > 50) {
            needStop = true;
        }
        if (needStop) {
            return new ArrayList<>();
        }
        int position = positionCounter;
        Document doc = HttpConnector.getHtml(pageUrl);
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
        Book bookInformation = new Book();
        Document doc = HttpConnector.getHtml(url, (response) -> {
            if (response.getStatusCode() == 200) {
                return true;
            }
            return false;
        });
        bookInformation.setTitle(doc.getElementsByClass("book-title").first().text());
        bookInformation.setDescription(doc.getElementsByClass("description").first().text());
        String danger = doc.getElementsByClass("alert-danger").text();
        if (!danger.equals("")) {
            bookInformation.setFragment(danger + " \n\r Данный файл скачать невозможно.");
            bookInformation.setUrlFb2("");
        } else {
            String el = doc.getElementsByClass("alert-info").text();
            int index = el.indexOf("Доступен");
            if (index != -1) {
                bookInformation.setFragment("Скачать ознакомительный фрагмент");
            } else bookInformation.setFragment("Скачать файл полностью");
            String urlFb2 = doc.getElementsByAttributeValue("rel", "nofollow").first().attr("href");
            if (urlFb2 != null) {
                if (!urlFb2.contains("http"))
                    urlFb2 = "https://topliba.com" + urlFb2;
            } else
                urlFb2 = "";
            bookInformation.setUrlFb2(urlFb2);
        }
        return bookInformation;
    }
}
