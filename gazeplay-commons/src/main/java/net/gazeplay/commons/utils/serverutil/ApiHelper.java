package net.gazeplay.commons.utils.serverutil;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ApiHelper {

    public HttpURLConnection createHttpRequest(URL uri, String requestType) throws IOException {
        HttpURLConnection con = (HttpURLConnection) uri.openConnection();
        con.setRequestMethod(requestType);
        con.setDoOutput(true);
        con.setRequestProperty("User-Agent", "Java client");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        return con;
    }

    public void postRequest(HttpURLConnection connection, String urlParameters) throws IOException {
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        try (var wr = new DataOutputStream(connection.getOutputStream())) {
            wr.write(postData);
        }
        StringBuilder content;

        try (
            Reader isr = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
            var br = new BufferedReader(isr)) {

            String line;
            content = new StringBuilder();

            while ((line = br.readLine()) != null) {
                content.append(line);
                content.append(System.lineSeparator());
            }
        }
        System.out.println(content.toString());
    }

    public void postFileToServer(File file, String fileName) throws IOException {
        String postEndpoint = "http://localhost:8080/uploadFile?name=" + fileName;
        HttpClient httpclient = HttpClientBuilder.create().build();
        HttpPost httppost = new HttpPost(postEndpoint);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addPart("file", new FileBody(file));

        HttpEntity entity = builder.build();

        httppost.setEntity(entity);

        HttpResponse response = httpclient.execute(httppost);

        try (
            Reader isr = new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr)
        ) {
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
            }

            StringBuilder result = new StringBuilder();

            String line = "";

            while ((line = br.readLine()) != null) {
                result.append(line);
            }

            br.close();
            System.out.println("Response : \n" + result);
        }
    }
}
