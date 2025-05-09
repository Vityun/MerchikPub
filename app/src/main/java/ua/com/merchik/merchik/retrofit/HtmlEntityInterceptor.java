package ua.com.merchik.merchik.retrofit;

import okhttp3.Interceptor;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;


class HtmlEntityInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());

        ResponseBody responseBody = response.body();
        MediaType contentType = responseBody != null ? responseBody.contentType() : null;

        if (responseBody == null || contentType == null) {
            return response;
        }

        String mediaType = contentType.type();    // Например: "text", "application", "image"
        String subType = contentType.subtype();   // Например: "html", "json", "jpeg"

        // Пропускаем обработку, если это не текст или json
        if (!mediaType.equals("text") && !subType.equals("json")) {
            return response;
        }

        String bodyString = responseBody.string();

        if (bodyString.isEmpty()) {
            return response;
        }

        // Заменяем сущность
        String modifiedBodyString = bodyString.replace("&#039;", "'");

        ResponseBody newBody = ResponseBody.create(modifiedBodyString, contentType);
        return response.newBuilder().body(newBody).build();
    }
}

