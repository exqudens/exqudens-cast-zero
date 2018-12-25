package exqudens.cast.api.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

public class HttpClient {

    public static final String GET = "GET";
    public static final String POST = "POST";

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_JSON_CHARSET_UTF_8 = APPLICATION_JSON + ";charset=UTF-8";

    public static final String USER_AGENT = "User-Agent";
    public static final String MOZILLA_5_0 = "Mozilla/5.0";

    public static Builder builder() {
        return new Builder();
    }

    public static void close(HttpURLConnection connection) {
        close(null, null, connection);
    }

    public static void close(OutputStream outputStream) {
        close(outputStream, null, null);
    }

    public static void close(InputStream inputStream) {
        close(null, inputStream, null);
    }

    public static void close(OutputStream outputStream, InputStream inputStream) {
        close(outputStream, inputStream, null);
    }

    public static void close(OutputStream outputStream, InputStream inputStream, HttpURLConnection connection) {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private String requestMethod;
    private String contentType;
    private String userAgent;
    private Integer connectTimeout;

    public Entry<Integer, String> request(String path) {
        InputStream clientInputStream = null;
        OutputStream clientOutputStream = null;
        try {
            clientOutputStream = new ByteArrayOutputStream();
            Integer responseCode = getResponseCode(path, null, clientOutputStream);
            byte[] byteArray = ByteArrayOutputStream.class.cast(clientOutputStream).toByteArray();
            String responseContent = new String(byteArray);
            return new SimpleEntry<>(responseCode, responseContent);
        } finally {
            close(clientOutputStream, clientInputStream);
        }
    }

    public Entry<Integer, String> requestWithBody(String path, String requestBody) {
        InputStream clientInputStream = null;
        OutputStream clientOutputStream = null;
        try {
            clientInputStream = new ByteArrayInputStream(requestBody.getBytes());
            clientOutputStream = new ByteArrayOutputStream();
            Integer responseCode = getResponseCode(path, clientInputStream, clientOutputStream);
            byte[] byteArray = ByteArrayOutputStream.class.cast(clientOutputStream).toByteArray();
            String responseContent = new String(byteArray);
            return new SimpleEntry<>(responseCode, responseContent);
        } finally {
            close(clientOutputStream, clientInputStream);
        }
    }

    private int getResponseCode(String url, InputStream clientInputStream, OutputStream clientOutputStream) {
        HttpURLConnection connection = null;

        InputStream serverInputStream = null;
        OutputStream serverOutputStream = null;

        try {
            connection = HttpURLConnection.class.cast(new URL(url).openConnection());
            connection.setRequestMethod(requestMethod);
            if (contentType != null) connection.setRequestProperty(CONTENT_TYPE, contentType);
            if (userAgent != null) connection.setRequestProperty(USER_AGENT, userAgent);
            if (connectTimeout != null) connection.setConnectTimeout(connectTimeout);
            connection.setDoOutput(true);

            if (clientInputStream != null) {
                serverOutputStream = connection.getOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = clientInputStream.read(buffer)) != -1) {
                    serverOutputStream.write(buffer, 0, len);
                }
                serverOutputStream.flush();
            }

            if (clientOutputStream != null) {
                serverInputStream = connection.getInputStream();
                if (serverInputStream == null) {
                    serverInputStream = connection.getErrorStream();
                }

                byte[] buffer = new byte[1024];
                int len;
                while ((len = serverInputStream.read(buffer)) != -1) {
                    clientOutputStream.write(buffer, 0, len);
                }
                clientOutputStream.flush();
            }

            return connection.getResponseCode();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            close(serverOutputStream, serverInputStream, connection);
        }
    }

    private HttpClient() {
        super();
    }

    public static class Builder {

        private final HttpClient httpClient;

        public Builder() {
            super();
            this.httpClient = new HttpClient();
        }

        public Builder requestMethod(String requestMethod) {
            httpClient.requestMethod = requestMethod;
            return this;
        }

        public Builder contentType(String contentType) {
            httpClient.contentType = contentType;
            return this;
        }

        public Builder userAgent(String userAgent) {
            httpClient.userAgent = userAgent;
            return this;
        }

        /**
         * @param connectTimeout in milliseconds
         * @return this
         */
        public Builder connectTimeout(Integer connectTimeout) {
            httpClient.connectTimeout = connectTimeout;
            return this;
        }

        public HttpClient build() {
            return httpClient;
        }

    }

}
