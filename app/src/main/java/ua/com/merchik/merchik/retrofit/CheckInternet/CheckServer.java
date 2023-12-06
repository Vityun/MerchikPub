package ua.com.merchik.merchik.retrofit.CheckInternet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.RetrofitResponse.ServerConnection;
import ua.com.merchik.merchik.dialogs.BlockingProgressDialog;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class CheckServer {

    public enum ServerConnect {
        DEFAULT, WITH_PHOTO
    }

    public static void isServerConnected(Context context, ServerConnect serverConnect, Integer mode, Clicks.clickStatusMsg click) {
        RequestBody mod = RequestBody.create(MediaType.parse("text/plain"), "ping");
        RequestBody time = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(System.currentTimeMillis()));
        MultipartBody.Part body;

        switch (serverConnect) {
            case WITH_PHOTO -> body = getPhotoBody(context);
            default -> body = null;
        }

        BlockingProgressDialog blockingProgressDialog = new BlockingProgressDialog(context, "Перевіряю з'єднання із сервером", "Зачекайте будь-ласка, йде перевірка з'єднання із сервером");
        if (mode != null){
            blockingProgressDialog.show();
        }

        retrofit2.Call<ServerConnection> call = RetrofitBuilder.getRetrofitInterface().PING_SERVER(mod, time, body);
        call.enqueue(new Callback<ServerConnection>() {
            @Override
            public void onResponse(Call<ServerConnection> call, Response<ServerConnection> response) {
                try {
                    blockingProgressDialog.dismiss();
                    if (response.isSuccessful()) {
                        if (response.body() != null){
                            if (response.body().getState()){
                                click.onSuccess("OK");
                            }else {
                                click.onFailure("response.body().getState(): " + response.body().getState());
                            }
                        }else {
                            click.onFailure("response.body(): " + response.body());
                        }
                    }else {
                        click.onFailure("response.isSuccessful(): " + response.isSuccessful());
                    }
                }catch (Exception e){
                    blockingProgressDialog.dismiss();
                    click.onFailure("Exception e: " + e);
                }
            }

            @Override
            public void onFailure(Call<ServerConnection> call, Throwable t) {
                blockingProgressDialog.dismiss();
                click.onFailure("Throwable t: " + t);
            }
        });
    }

    private static MultipartBody.Part getPhotoBody(Context context) {
        MultipartBody.Part body;

        String imageFileName = "TEST_PHOTO_SERV";
        String root = Environment.getExternalStorageDirectory().toString() + "/Merchik/Test";
        File myDir = new File(root);

        String fname = imageFileName + ".jpg";
        File image = new File(myDir, fname);
        @SuppressLint("ResourceType") InputStream inputStream = context.getResources().openRawResource(R.drawable.test_server_photo);
        OutputStream out;
        try {
            out = new FileOutputStream(image);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        byte[] buf = new byte[1024];
        int len;
        while (true) {
            try {
                if (!((len = inputStream.read(buf)) > 0)) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                out.write(buf, 0, len);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            out.close();
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), image);
        body = MultipartBody.Part.createFormData("image", image.getName(), requestFile);
        return body;
    }
}
