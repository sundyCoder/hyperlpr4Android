package pr.hyperlpr.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import pr.hyperlpr.task.callback.ProgramImageCallback;
import pr.hyperlpr.util.DeepCarUtil;

public class ProcessImageTask implements Runnable {

    private static final String TAG = ProcessImageTask.class.getSimpleName();
    private String imagePath;
    private String imageShortPath;
    private long startTime;


    private ProgramImageCallback mListener;
    private long handle;

    public ProcessImageTask(String imagePath, ProgramImageCallback listener, long handle){
        this.mListener = listener;
        this.imagePath = imagePath;
        this.handle = handle;
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        ImageBean imageBean = new ImageBean();
        String[] paths = imagePath.split(File.separator);
        imageShortPath = paths[paths.length -1];

        imageBean.imagePath = imagePath;
        final Bitmap image = BitmapFactory.decodeFile(imagePath);
        if (image == null){
            if (mListener != null){
                imageBean.totalTime = System.currentTimeMillis() - startTime;
                mListener.onProcessImageComplete(true, imageBean);
            }
            return;
        }

        imageBean.imageSize = image.getByteCount();
        int width = image.getWidth();
        int height = image.getHeight();
        Mat m = new Mat(width, height, CvType.CV_8UC4);
        Utils.bitmapToMat(image, m);
        if(width > 1000 || height > 1000){
            Size sz = new Size(600,800);
            Imgproc.resize(m,m,sz);
        }
        try {
            OpenCVLoader.initDebug();
            String result =  DeepCarUtil.SimpleRecognization(m.getNativeObjAddr(), handle);
            if (mListener != null) {
                imageBean.resultString = result;
                Log.i(TAG, "source path ---> " + imageShortPath);
                imageBean.totalTime = System.currentTimeMillis() - startTime;
                if (!TextUtils.isEmpty(result) && imageShortPath.contains(result)) {
                    mListener.onProcessImageComplete(false, imageBean);
                } else {
                    mListener.onProcessImageComplete(true, imageBean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "exception occured!");
            if (mListener != null){
                imageBean.totalTime = System.currentTimeMillis() - startTime;
                mListener.onProcessImageComplete(true,imageBean);
            }
        }
    }

}
