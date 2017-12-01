package pr.hyperlpr;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


import java.util.ArrayList;
import java.util.List;
import java.io.*;


public class MainActivity extends Activity {
    private static final String TAG = "hyperlpr";
    public long handle;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int SELECT_IMAGE_ACTIVITY_REQUEST_CODE = 200;
    private static final String sdcarddir = "/sdcard/" + DeepCarUtil.ApplicationDir;
    private Bitmap bmp;
    private Bitmap Originbitmap = bmp;
    private ImageView im;
    private ImageButton buttonCamera;
    private ImageButton buttonFolder;
    private EditText et;
    private boolean b2Recognition = true;
    private Uri fileUri;
    private static String filePath = null;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    String[] permissions = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};//在SDCard中创建与删除文件权限
    List<String> mPermissionList = new ArrayList<>();

    private class plateTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            Mat m = new Mat(bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC4);
            double new_w = bmp.getWidth()*0.5;
            double new_h = bmp.getHeight()*0.5;
            Size sz = new Size(new_w,new_h);
            Utils.bitmapToMat(bmp, m);
            Imgproc.resize(m,m,sz);
            try {
                long startClock = System.currentTimeMillis();
                String license = DeepCarUtil.SimpleRecognization(m.getNativeObjAddr(), handle);
                long diff = System.currentTimeMillis() - startClock;
                //Utils.matToBitmap(m, bmp);
                Message msg = new Message();
                Bundle b = new Bundle();
                b.putString("license", license);
                b.putParcelable("bitmap", bmp);
                msg.what = 1;
                msg.setData(b);
                mHandler.sendMessage(msg);
            } catch (Exception e) {
                Log.d(TAG, "exception occured!");
            }
            return null;
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    System.loadLibrary("hyperlpr");
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            //initFile();
                            initRecognizer();
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            if (filePath != null) {
                                bmp = BitmapFactory.decodeFile(filePath);
                            } else {
                                bmp = BitmapFactory.decodeFile(sdcarddir + "/" + DeepCarUtil.demoImgPath);
                            }
                            im.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    im.setImageBitmap(bmp);
                                }
                            }, 10);
                            Originbitmap = bmp;
                            if (bmp != null)
                                new plateTask().execute();
                        }
                    }.execute();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//取消标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        checkPermission();
        initData();
    }

    private void initData() {
        im = (ImageView) findViewById(R.id.imageView);
        et = (EditText) findViewById(R.id.editText);
        buttonCamera = (ImageButton) findViewById(R.id.buttonCamera);
        buttonFolder = (ImageButton) findViewById(R.id.buttonFolder);
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fileUri = DeepMediaFileUtil.getOutputMediaFileUri(1);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        });
        buttonFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                intent.setType("image/*" );
                startActivityForResult(intent, SELECT_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        });
        im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (b2Recognition) {
                    if (bmp != null)
                        new plateTask().execute();
                } else {
                    bmp = Originbitmap;
                    im.setImageBitmap(bmp);
                    et.setText("");
                }
                b2Recognition = !b2Recognition;
            }
        });
    }

    /**
     * 检查权限
     */
    private void checkPermission() {
        mPermissionList.clear();
        /**
         * 判断哪些权限未授予
         */
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        /**
         * 判断是否为空
         */
        if (mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了

        } else {//请求权限方法
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
                } else {
                    filePath = fileUri.getPath();
                }
            }
        } else if (requestCode == SELECT_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && null != data) {
            fileUri = data.getData();
            filePath = DeepMediaFileUtil.getPath(this, fileUri);
        }
    }


    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Bundle b = msg.getData();
                    String str = b.getString("license");
                    et.setText(b.getString("license"));
                    im.setImageBitmap((Bitmap) b.getParcelable("bitmap"));
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void copyFilesFromAssets() {
        DeepAssetUtil.CopyAssets(this, DeepCarUtil.ApplicationDir, sdcarddir);
    }

    public void initRecognizer()
    {
        String cascade_filename = sdcarddir + File.separator + DeepCarUtil.cascade_filename;
        String finemapping_prototxt = sdcarddir + File.separator + DeepCarUtil.finemapping_prototxt;
        String finemapping_caffemodel = sdcarddir + File.separator + DeepCarUtil.finemapping_caffemodel;
        String segmentation_prototxt = sdcarddir + File.separator + DeepCarUtil.segmentation_prototxt;
        String segmentation_caffemodel = sdcarddir + File.separator + DeepCarUtil.segmentation_caffemodel;
        String character_prototxt = sdcarddir + File.separator + DeepCarUtil.character_prototxt;
        String character_caffemodel = sdcarddir + File.separator + DeepCarUtil.character_caffemodel;
        copyFilesFromAssets();

        handle  =  DeepCarUtil.InitPlateRecognizer(
                cascade_filename,
                finemapping_prototxt,finemapping_caffemodel,
                segmentation_prototxt,segmentation_caffemodel,
                character_prototxt,character_caffemodel
        );
    }
}