package pr.hyperlpr.task;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import pr.hyperlpr.Globals;
import pr.hyperlpr.task.callback.ProcessStateCallback;
import pr.hyperlpr.task.callback.ProgramImageCallback;
import pr.hyperlpr.util.DeepAssetUtil;
import pr.hyperlpr.util.Utils;

public class ImageExtractionService extends IntentService implements ProgramImageCallback {
    private static final String MODEL_SOURCE_IMAGE_PATH = "model_source_image";
    private static final String SERVER_ACTION = "unzip";
    private static final int BLOCK_SIZE = 1024;
    private String sourceImagePath ;
    private int imageCount;
    private int callbackCount;
    private int errorCount;
    private ExecutorService processImageExecutor;
    private static ProcessStateCallback mProcessStateListener;
    //模仿 AsyncTask 里面声明线程池的做法.
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

//    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final int CORE_POOL_SIZE = Math.max(1, Math.min(CPU_COUNT - 1, 1));


    private long startTime;
    public ImageExtractionService() {
        super("ImageExtractionService");
        imageCount = 0;
        callbackCount = 0;

        Log.e("processImageTime" , "CORE_POOL_SIZE " + CORE_POOL_SIZE + "---> "+ CPU_COUNT);

        processImageExecutor = Executors.newFixedThreadPool(CORE_POOL_SIZE);
    }

    public static void startService(Context context , String sourceImagePath,
                                    ProcessStateCallback callback){
        mProcessStateListener = callback;
        Intent intent = new Intent(context, ImageExtractionService.class);
        intent.setAction(SERVER_ACTION);
        intent.putExtra(MODEL_SOURCE_IMAGE_PATH , sourceImagePath);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null){
            String action = intent.getAction();
            if (SERVER_ACTION.equals(action)){
                sourceImagePath = intent.getStringExtra(MODEL_SOURCE_IMAGE_PATH);
                Log.i("ReadyDataActivity" , " ----> " + sourceImagePath);
                handlerUnZip();
            }
        }
    }

    private void handlerUnZip() {
        long unZipStartTime = startTime = System.currentTimeMillis();

        ZipInputStream zipInputStream = null;
        String mCurrentImagePath = null;
        try{
            //创建准备存储的位置.
            File imagesRoot = Utils.getOrCreateExternalModelsRootDirectory(this);

            //加载系统路径文件.
            File imageZipFile = new File(sourceImagePath);
            FileInputStream fis = new FileInputStream(imageZipFile);
            zipInputStream = new ZipInputStream(fis);

            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null){
                ++imageCount;
                mCurrentImagePath = zipEntry.getName();
                File imageFile = new File(imagesRoot, mCurrentImagePath);
                if (imageFile.isDirectory()){
                    --imageCount;
                    doCreateFolder(imagesRoot);
                }else {
                    //检测文件后缀名.
                    if (Utils.checkFileExtension(imageFile)){
                        if (!imageFile.exists()){
                            doCreateFile(imageFile, zipInputStream);
                        }

                        long handle = DeepAssetUtil.initRecognizer(this);
                        Log.i("processImage" , "handle --> " + handle);
                        ProcessImageTask task = new ProcessImageTask(imageFile.getAbsolutePath(), this ,handle);
                        processImageExecutor.execute(task);
                    }else{
                        --imageCount;
                    }
                }
                zipInputStream.closeEntry();
            }

            if (imageCount == 0 && mProcessStateListener != null){
                mProcessStateListener.OnProgramComplete();
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if (zipInputStream != null) {
                    zipInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        long unZipEndTime = System.currentTimeMillis();
        Log.i("unzip time : ", String.valueOf(unZipEndTime - unZipStartTime));
    }

    private void doCreateFile(File imageFile, ZipInputStream zipInputStream) throws IOException {
        FileOutputStream fos = new FileOutputStream(imageFile);
        byte[] buffer = new byte[BLOCK_SIZE];
        int len = -1;
        while ((len = zipInputStream.read(buffer)) != -1){
            fos.write(buffer, 0, len);
        }
        fos.close();
    }

    private void doCreateFolder(File imageRoot) throws IOException {
        if ( !imageRoot.mkdirs()){
            throw new IOException("Unable to create model root directory: " +
                    imageRoot.getAbsolutePath());
        }
    }

    @Override
    public void onProcessImageComplete(boolean isError, ImageBean imageBean) {
        synchronized (ImageExtractionService.class){
            ++callbackCount;
        }

        Log.i("processImage" , "error " + errorCount + " callback--> " + callbackCount  +  " total--> " +  imageCount);
        if (isError){
            ++errorCount;
            Log.e("processImage", imageBean.toString());
            Globals.errorMap.put(imageBean.imagePath ,imageBean.resultString );
        }

        callbackProgram();

        if (!isError) {
            Log.i("processImage", imageBean.toString());
            Globals.successMap.put(imageBean.imagePath ,imageBean.resultString );
        }

        if (callbackCount == imageCount){
            Log.i("processImage" , "process Image complete");
//            callbackComplete();
        }
    }

    private void callbackProgram(){
        if (mProcessStateListener != null){
            mProcessStateListener.OnProgram(callbackCount, imageCount, 0);
        }
    }

    private void callbackComplete(){
        long endTime = System.currentTimeMillis();
        mProcessStateListener.OnProgramComplete();
        Log.e("processImageTime" , "time : "+ String.valueOf(endTime - startTime) + "----imageCount " + callbackCount );
        Log.e("processImageTime" , "error Process count " + errorCount);
    }

}
