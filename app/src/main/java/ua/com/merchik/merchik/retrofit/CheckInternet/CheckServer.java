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
import java.util.Arrays;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ServerConnection;
import ua.com.merchik.merchik.dialogs.BlockingProgressDialog;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class CheckServer {

    public enum ServerConnect {
        DEFAULT, WITH_PHOTO
    }

    private static BlockingProgressDialog blockingProgressDialog;
    public static void isServerConnected(Context context, ServerConnect serverConnect, Integer mode, Clicks.clickStatusMsg click) {
        try {
            Globals.writeToMLOG("INFO", "synchronizationSignal/isServerConnected/", "mode: " + mode);
            Globals.writeToMLOG("INFO", "synchronizationSignal/isServerConnected/", "serverConnect: " + serverConnect);

            RequestBody mod = RequestBody.create(MediaType.parse("text/plain"), "ping");
            RequestBody time = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(System.currentTimeMillis()));
            MultipartBody.Part body;

            switch (serverConnect) {
                case WITH_PHOTO -> body = getPhotoBody(context);
                default -> body = null;
            }


            if (mode != null){
                blockingProgressDialog = new BlockingProgressDialog(context, "Перевіряю з'єднання із сервером", "Зачекайте будь-ласка, йде перевірка з'єднання із сервером");
                blockingProgressDialog.show();
                Globals.writeToMLOG("INFO", "synchronizationSignal/isServerConnected/", "blockingProgressDialog.show()");
            }

            retrofit2.Call<ServerConnection> call = RetrofitBuilder.getRetrofitInterface().PING_SERVER(mod, time, body);
            call.enqueue(new Callback<ServerConnection>() {
                @Override
                public void onResponse(Call<ServerConnection> call, Response<ServerConnection> response) {
                    try {
                        if (blockingProgressDialog != null && blockingProgressDialog.isShowing()){
                            Globals.writeToMLOG("INFO", "synchronizationSignal/isServerConnected/onResponse/", "blockingProgressDialog.dismiss()");
                            blockingProgressDialog.dismiss();
                        }

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
                        if (blockingProgressDialog != null && blockingProgressDialog.isShowing()){
                            Globals.writeToMLOG("INFO", "synchronizationSignal/isServerConnected/onResponse/E", "blockingProgressDialog.dismiss()");
                            blockingProgressDialog.dismiss();
                        }

                        Globals.writeToMLOG("ERROR", "synchronizationSignal/isServerConnected/onResponse", "Exception e: " + e);
                        Globals.writeToMLOG("ERROR", "synchronizationSignal/isServerConnected/onResponse", "Exception e..: " + Arrays.toString(e.getStackTrace()));

                        click.onFailure("Exception e: " + e);
                    }
                }

                @Override
                public void onFailure(Call<ServerConnection> call, Throwable t) {
                    try {
                        if (blockingProgressDialog != null && blockingProgressDialog.isShowing()){
                            Globals.writeToMLOG("INFO", "synchronizationSignal/isServerConnected/onFailure/", "blockingProgressDialog.dismiss()");
                            blockingProgressDialog.dismiss();
                        }
                        click.onFailure("Throwable t: " + t);
                    }catch (Exception e){
                        if (blockingProgressDialog != null && blockingProgressDialog.isShowing()){
                            Globals.writeToMLOG("INFO", "synchronizationSignal/isServerConnected/onFailure/E", "blockingProgressDialog.dismiss()");
                            blockingProgressDialog.dismiss();
                        }

                        Globals.writeToMLOG("ERROR", "synchronizationSignal/isServerConnected/onFailure", "Exception e: " + e);
                        Globals.writeToMLOG("ERROR", "synchronizationSignal/isServerConnected/onFailure", "Exception e..: " + Arrays.toString(e.getStackTrace()));

                        click.onFailure("Exception e: " + e);
                    }
                }
            });
        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "synchronizationSignal/isServerConnected", "Exception e: " + e);
            Globals.writeToMLOG("ERROR", "synchronizationSignal/isServerConnected", "Exception e..: " + Arrays.toString(e.getStackTrace()));
        }
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
