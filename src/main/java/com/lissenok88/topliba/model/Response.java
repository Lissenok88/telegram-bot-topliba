package com.lissenok88.topliba.model;

import org.jsoup.nodes.Document;

public class Response {
    private int statusCode;
    private String body;
    private Document document;
    private boolean success;
    private RuntimeException ex;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public RuntimeException getEx() {
        return ex;
    }

    public void setEx(RuntimeException ex) {
        this.ex = ex;
    }
}
