package com.jwetherell.quick_response_code.result2.Connect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jwetherell.quick_response_code.R;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import java.io.File;


public class ProgressBarDownload extends Activity {
    Button download;
    TextView downloadCount;
    ProgressBar progressBar;

    Future<File> downloading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        final String link = intent.getStringExtra("link");
        Log.i("link ",link);
        // Enable global Ion logging
        Ion.with(getApplicationContext())
                .load(link)
                .setLogging("MyLogs", Log.DEBUG)
                .asJsonObject();

        setContentView(R.layout.progress);

        download = (Button)findViewById(R.id.download);
        downloadCount = (TextView)findViewById(R.id.download_count);
        progressBar = (ProgressBar)findViewById(R.id.progress);

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (downloading != null && !downloading.isCancelled()) {
                    resetDownload();
                    return;
                }

                download.setText("Cancel");
                // this is a 180MB zip file to test with
                downloading = Ion.with(ProgressBarDownload.this)
                .load(link)
                    // attach the percentage report to a progress bar.
                    // can also attach to a ProgressDialog with progressDialog.
                    .progressBar(progressBar)
                    // callbacks on progress can happen on the UI thread
                    // via progressHandler. This is useful if you need to update a TextView.
                    // Updates to TextViews MUST happen on the UI thread.
                    .progressHandler(new ProgressCallback() {
                        @Override
                        public void onProgress(int downloaded, int total) {
                            downloadCount.setText("" + downloaded + " / " + total);
                        }
                    })
                    // write to a file
                    .write(getFileStreamPath("zip-" + System.currentTimeMillis() + ".zip"))
                    // run a callback on completion
                    .setCallback(new FutureCallback<File>() {
                        @Override
                        public void onCompleted(Exception e, File result) {
                            resetDownload();
                            if (e != null) {
                                Toast.makeText(ProgressBarDownload.this, "Error downloading file", Toast.LENGTH_LONG).show();
                                return;
                            }
                            Toast.makeText(ProgressBarDownload.this, "File upload complete", Toast.LENGTH_LONG).show();
                        }
                    });
            }
        });
    }

    void resetDownload() {
        // cancel any pending download
        downloading.cancel();
        downloading = null;

        // reset the ui
        download.setText("Download");
        downloadCount.setText(null);
        progressBar.setProgress(0);
    }
}
