package com.lissenok88.topliba.job;

import com.lissenok88.topliba.model.Response;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class HttpConnector {
    private static final String userAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0";
    private static HttpClientContext context = HttpClientContext.create();

    public static Document getHtml(String url){
        return getHtml(url, null);
    }

    public static Document getHtml(String url, ResponseHandler successHandler){
        Response response = HttpConnector.get(url, successHandler);
        return Jsoup.parse(response.getBody());
    }

    private static CloseableHttpClient getNewClient() {
        return HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom().
                        setCookieSpec(CookieSpecs.STANDARD).build()
                ).build();
    }

    public static Response get(String url, ResponseHandler successHandler) {
        if (successHandler == null) {
            return getInternal(url);
        }
        Response response = null;
        for (int i = 0; i < 10; i++) {
            Response result = getInternal(url);
            if (successHandler.handleResponse(result)) {
                return result;
            }
        }
        return response;
    }

    private static Response getInternal(String url) {
        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", userAgent);
        Response responseModel = new Response();
        responseModel.setSuccess(true);
        try (
                CloseableHttpClient client = getNewClient();
                CloseableHttpResponse response = client.execute(request, context)
        ) {
            responseModel.setStatusCode(response.getStatusLine().getStatusCode());
            InputStream responseStream = response.getEntity().getContent();

            responseModel.setBody(new BufferedReader(new InputStreamReader(responseStream)).lines()
                    .parallel().collect(Collectors.joining("\n")));
        } catch (ClientProtocolException e) {
            responseModel.setSuccess(false);
            responseModel.setEx(new RuntimeException(e));
        } catch (IOException e) {
            responseModel.setSuccess(false);
            responseModel.setEx(new RuntimeException(e));
        }
        Document document = Jsoup.parse(responseModel.getBody());
        responseModel.setDocument(document);

        return responseModel;
    }
}
