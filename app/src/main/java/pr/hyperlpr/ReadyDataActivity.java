package pr.hyperlpr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import pr.hyperlpr.task.ImageExtractionService;
import pr.hyperlpr.task.callback.ProcessStateCallback;
import pr.hyperlpr.view.NumberProgressBar;


public class ReadyDataActivity extends AppCompatActivity {
    private NumberProgressBar mProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready);
        mProgressBar = (NumberProgressBar)findViewById(R.id.progressBar);

        Uri fileUri = getIntent().getParcelableExtra("filePath");
        Log.i("ReadyDataActivity" , fileUri + " ----> " + fileUri.getPath());
        new LoadFaceFloatAsyncTask(this,fileUri.getPath(), mProgressBar).execute();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProgressBar = null;
    }


    public class LoadFaceFloatAsyncTask extends AsyncTask<Void, Integer, Void> implements ProcessStateCallback {
        @SuppressLint("StaticFieldLeak")
        private Activity mContext;
        @SuppressLint("StaticFieldLeak")
        private NumberProgressBar mProgressBar;
        private String sourceImagePath = null;
        private boolean isLoadDataBaseSuccess;
        private boolean isComplete;
        private long startTime;
        private void setComplete() {
            isComplete = true;
        }



        public LoadFaceFloatAsyncTask(Activity context, String filePath, View view){
            this.mContext = context;
            mProgressBar = (NumberProgressBar) view;
            startTime = System.currentTimeMillis();
            sourceImagePath = filePath;
        }

        protected Void doInBackground(Void... voids) {

            ImageExtractionService.startService(mContext,sourceImagePath,this);

            while(!isComplete){
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

//            Intent intent = new Intent(mContext, ResultActivity.class);
//            mContext.startActivity(intent);
//            mContext.finish();
//            mContext = null;

            long endTime = System.currentTimeMillis();
            Log.i("totalTime", "load data use time :  " + (endTime - startTime));
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int currentCount = values[0];
            int totalCount  = values[1];
            int errorCount  = values[2];

            float progress = (float) ((errorCount + currentCount ) * 1.0 / totalCount) * 100;
            mProgressBar.setProgress((int) progress);
        }


        @Override
        public void OnCancelProgram() {
            setComplete();
        }

        @Override
        public void OnProgram(int currentCount, int totalCount, int errorCount) {
            //更新文件更新 时候的进度.
            publishProgress(currentCount , totalCount , errorCount);
        }

        @Override
        public void OnProgramComplete() {
            if (!isLoadDataBaseSuccess){
                isLoadDataBaseSuccess = true;
            }else{
                setComplete();
            }
        }
    }
}
