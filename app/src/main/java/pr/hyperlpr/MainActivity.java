package pr.hyperlpr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import pr.hyperlpr.util.DeepAssetUtil;
import pr.hyperlpr.util.DeepCarUtil;
import pr.hyperlpr.util.DeepMediaFileUtil;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "hyperlpr";
    public long handle;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int SELECT_IMAGE_ACTIVITY_REQUEST_CODE = 200;
    private static final int CHOOSE_FILE_ACTIVITY_REQUEST_CODE = 300;

    private Bitmap bmp;
    //备份 bitmap
    private Bitmap originBitmap = bmp;
    private ImageView im;
    private Toolbar mToolbar;
    private boolean b2Recognition = true;
    private Uri fileUri;
    private static String filePath = null;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    public TextView resultbox;
    private boolean isLoadOpenCVSuccess = false;

    //需要 Camera
    String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};//在SDCard中创建与删除文件权限
    List<String> mPermissionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);//取消标题栏
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        checkPermission();
        initView();
        initEvent();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mToolbar != null){
            setSupportActionBar(mToolbar);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //初始化openCV

        if (!isLoadOpenCVSuccess && !OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (originBitmap != null && !originBitmap.isRecycled()){
            originBitmap.recycle();
            originBitmap = null;
        }

        if (bmp != null && !bmp.isRecycled()){
            bmp.recycle();
            bmp = null;
        }
    }

    private void initView() {
        im = (ImageView) findViewById(R.id.imageView);
        resultbox = (TextView)findViewById(R.id.textResult);
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
    }

    private void initEvent(){
        //双击识别.
        im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (b2Recognition) {
                    if (bmp != null)
                        new PlateAsyncTask().execute();
                } else {
                    bmp = originBitmap;
                    im.setImageBitmap(bmp);
                    resultbox.setText(null);
                    resultbox.setVisibility(View.INVISIBLE);
                }
                b2Recognition = !b2Recognition;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main , menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int selectId = item.getItemId();
        switch (selectId){
            case R.id.camera:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fileUri = DeepMediaFileUtil.getOutputMediaFileUri(1);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                break;
            case R.id.pic:
                Intent picIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                picIntent.setType("image/*" );
                startActivityForResult(picIntent, SELECT_IMAGE_ACTIVITY_REQUEST_CODE);
                break;
            case R.id.moreCheck:
                Intent sourceImageIntent  = new Intent(Intent.ACTION_GET_CONTENT);
                sourceImageIntent.setType("application/x-zip-compressed");
                sourceImageIntent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(sourceImageIntent , CHOOSE_FILE_ACTIVITY_REQUEST_CODE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 检查权限
     */
    private void checkPermission() {
        mPermissionList.clear();

         //判断哪些权限未授予
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);
            }
        }

        //请求权限
        if (!mPermissionList.isEmpty()) {
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA:
                Log.d("------->", "permission success deal！！！");
                break;
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE == requestCode) {
            if (RESULT_OK == resultCode) {
                if (data != null) {
                    if (data.hasExtra("data")) {
                        Bitmap thumbnail = data.getParcelableExtra("data");
                        im.setImageBitmap(thumbnail);
                    }
                }

                filePath = fileUri.getPath();
            }
        } else if (requestCode == SELECT_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && null != data) {
            fileUri = data.getData();
            im.setImageURI(fileUri);
            filePath = DeepMediaFileUtil.getPath(this, fileUri);
        }else if (requestCode == CHOOSE_FILE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && null != data){
            Uri zipImage  = data.getData();
            Intent intent = new Intent(MainActivity.this, ReadyDataActivity.class);
            intent.putExtra("filePath", zipImage);
            startActivity(intent);

        }
    }


    @SuppressLint("StaticFieldLeak")
    private class PlateAsyncTask extends AsyncTask<String, Integer, String> {
        private long startTime ;

        @Override
        protected String doInBackground(String... params) {
            startTime = System.currentTimeMillis();
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            Mat m = new Mat(width, height, CvType.CV_8UC4);
            Utils.bitmapToMat(bmp, m);
            if(width > 1000 || height > 1000){
                Size sz = new Size(600,800);
                Imgproc.resize(m,m,sz);
            }
            try {
                return  DeepCarUtil.SimpleRecognization(m.getNativeObjAddr(), handle);
            } catch (Exception e) {
                Log.d(TAG, "exception occured!");
                return null;
            }
        }

        @Override
        protected void onPostExecute(String license) {
            super.onPostExecute(license);
            long endTime = System.currentTimeMillis();
            Log.i(TAG, "total time is : " + (endTime - startTime));

            resultbox.setText(license);
            resultbox.setVisibility(View.VISIBLE);

            im.setImageBitmap(originBitmap);
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @SuppressLint("StaticFieldLeak")
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    //在加载openCV 成功后, 开始加载 hyperlpr so 文件
                    if (!isLoadOpenCVSuccess){
                        System.loadLibrary("hyperlpr");
                    }
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            handle = DeepAssetUtil.initRecognizer(MainActivity.this);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            if (filePath != null) {
                                bmp = BitmapFactory.decodeFile(filePath);
                                im.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        im.setImageBitmap(bmp);
                                    }
                                });
                                originBitmap = bmp;
                                if (bmp != null) {
                                    new PlateAsyncTask().execute();
                                }
                            }
                        }
                    }.execute();

                    isLoadOpenCVSuccess = true;
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };



}