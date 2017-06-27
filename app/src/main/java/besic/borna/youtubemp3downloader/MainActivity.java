package besic.borna.youtubemp3downloader;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {

    String checkBaseUrl = "http://api.convert2mp3.cc/check.php?v=";

    TextView tv;
    Handler handler;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();

        tv = (TextView) findViewById(R.id.label);
        handler = new Handler(getMainLooper());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Downloading MP3...");
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String youtubeUrlString = intent.getStringExtra(Intent.EXTRA_TEXT);
                String[] urlTokens = youtubeUrlString.split("/");
                String videoId = urlTokens[urlTokens.length-1];
                if (youtubeUrlString != null) {
                    download(checkBaseUrl+videoId);
                }
            }
        }
    }

    private void download(final String url){
        Log.w("DOWNLOAD", url);
        CheckTask checkTask = new CheckTask(new CheckCallback() {
            @Override
            public void done(String status) {
                String[] statusTokens = status.split("\\|");
                //for (String t : statusTokens) Log.w("TOKEN", t);

                Runnable reschedule = new Runnable() {
                    @Override
                    public void run() {
                        download(url);
                    }
                };

                switch (statusTokens[0]){
                    case "OK":
                        String server = statusTokens[1];
                        String id = statusTokens[2];
                        String name = statusTokens[3];
                        String downloadUrl = "http://dl"+server+".downloader.space/dl.php?id="+id;

                        final DownloadTask downloadTask = new DownloadTask(MainActivity.this, MainActivity.this, name+".mp3", progressDialog);

                        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                downloadTask.cancel(true);
                            }
                        });

                        downloadTask.execute(downloadUrl);
                        break;
                    case "ERROR":
                        if(statusTokens[1].compareTo("PENDING")==0){
                            MainActivity.this.handler.postDelayed(reschedule, 2000);
                            tv.setText(R.string.converting);
                        }
                        else MainActivity.this.finish();
                        break;
                    case "DOWNLOAD":
                        MainActivity.this.handler.postDelayed(reschedule, 2000);
                        tv.setText(R.string.converting);
                        break;
                }
            }
        });
        checkTask.execute(url);
    }

    void requestPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) { // explanation

            } else { // no explanation
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 42);
            }
        }
    }
}
