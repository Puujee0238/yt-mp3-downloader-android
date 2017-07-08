package besic.borna.youtubemp3downloader;

import android.os.AsyncTask;

public class CheckTask extends AsyncTask<String, Void, String>{

    CheckCallback callback;

    public CheckTask(CheckCallback callback){
        this.callback=callback;
    }

    @Override
    protected String doInBackground(String... strings) {
        for(String s : strings){
            return Request.get(s);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        this.callback.done(s);
    }
}
