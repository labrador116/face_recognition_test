package ru.facereg.facerecognitiontest.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import ru.facereg.facerecognitiontest.ProcessingService;
import ru.facereg.facerecognitiontest.R;
import ru.facereg.facerecognitiontest.utils.IUploadPhotoArchiveToServer;
import ru.facereg.facerecognitiontest.utils.Packager;

/**
 * @author Markin Andrey on 20.08.2018.
 */
public class ProcessingActivity extends AppCompatActivity {
    public static final String SERVICE_RESULT = "service_result";
    public static final String BROADCAST_FILTER = "ru.facereg.facerecognitiontest.activities.ProcessingActivity.ProcessingBroadcastReceiver";

    private ProgressBar mProgressBar;
    private TextView mProcess;
    private TextView mProcessingResult;
    private TextView mSendingResult;
    private ProcessingBroadcastReceiver mReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.processing_activity);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProcess = (TextView) findViewById(R.id.process);
        mProcess.setText("...Image Processing");
        mProcessingResult = (TextView) findViewById(R.id.processing_result);
        mSendingResult = (TextView) findViewById(R.id.sending_result);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReceiver = new ProcessingBroadcastReceiver();
        IntentFilter filter = new IntentFilter(BROADCAST_FILTER);
        registerReceiver(mReceiver, filter);
        Intent intent = new Intent(this, ProcessingService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        Intent intent = new Intent(this, ProcessingService.class);
        stopService(intent);
    }

    public void uploadArchive(File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("application/zip"), file);
        String id = "0";
        RequestBody reqId = RequestBody.create(MultipartBody.FORM, id);
        String jwt = "JWT eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6InRlc3RAdGVzdC50ZXN0IiwiY3MiOiI1OTUwOWVhMzZjYzAwMDAwIiwiaWF0IjoxNTM0NDE4NjUyLCJhdWQiOiIqLmxvY2FsaG9zdCIsImlzcyI6Im1zLXVzZXJzIn0.UfxJBd3i8j7BJUgoA5EOQsJ30Iy0CDXNPSW52Vc6d1E";

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(2, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://fh-dev.makeomatic.ru/")
                .client(client)
                .build();

        IUploadPhotoArchiveToServer uploadService = retrofit.create(IUploadPhotoArchiveToServer.class);
        Call<ResponseBody> call = uploadService.upload(reqId, jwt, requestFile);
        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mSendingResult.setText("Request sent successfully");
                    mSendingResult.setTextColor(Color.GREEN);
                    mSendingResult.setVisibility(View.VISIBLE);
                } else {
                    mSendingResult.setText("Request failed");
                    mSendingResult.setTextColor(Color.RED);
                    mSendingResult.setVisibility(View.VISIBLE);
                }
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                mSendingResult.setText("Request failed");
                mSendingResult.setTextColor(Color.RED);
                mSendingResult.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    public class ProcessingBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra(SERVICE_RESULT);
            switch (result) {
                case ProcessingService.SUCCESS_RESULT:
                    mProcessingResult.setText("Processing Successful");
                    mProcessingResult.setTextColor(Color.GREEN);
                    mProcessingResult.setVisibility(View.VISIBLE);
                    break;
                case ProcessingService.FAILED_RESULT:
                    mProcessingResult.setText("Processing Failed");
                    mProcessingResult.setTextColor(Color.RED);
                    mProcessingResult.setVisibility(View.VISIBLE);
                    break;
            }
            new AsyncTask<Void, Void, File>() {

                @Override
                protected File doInBackground(Void... voids) {
                    String storageDir = Environment.getExternalStorageDirectory().toString();
                    File folder = new File(storageDir + File.separator + "face_img");
                    List<File> fileList = Arrays.asList(folder.listFiles());
                    File zipFile = new File(storageDir + File.separator + "img_pack.zip");
                    try {
                        Packager.packZip(zipFile, fileList);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return zipFile;
                }

                @Override
                protected void onPostExecute(File file) {
                    mProcess.setText("...Sending to server");
                    ProcessingActivity.this.uploadArchive(file);
                }
            }.execute();
        }
    }
}
