package pr.hyperlpr.util;
import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import pr.hyperlpr.Globals;


public class DeepAssetUtil {
   private  static void CopyAssets(Context context, String assetDir, String dir) {
        String[] files;
        try {
            // 获得Assets一共有几多文件
            files =context.getAssets().list(assetDir);
        } catch (IOException e1) {
            return;
        }
        File mWorkingPath = new File(dir);
        // 如果文件路径不存在
        if (!mWorkingPath.exists()) {
            // 创建文件夹
            if (!mWorkingPath.mkdirs()) {
                // 文件夹创建不成功时调用
            }
        }

        for (int i = 0; i < files.length; i++) {
            try {
                // 获得每个文件的名字
                String fileName = files[i];
                // 根据路径判断是文件夹还是文件
                if (!fileName.contains(".")) {
                    if (0 == assetDir.length()) {
                        CopyAssets(context,fileName, dir + fileName + "/");
                    } else {
                        CopyAssets(context,assetDir + "/" + fileName, dir + "/" + fileName + "/");
                    }
                    continue;
                }
                File outFile = new File(mWorkingPath, fileName);
                if (outFile.exists())
                    continue;
                InputStream in = null;
                if (0 != assetDir.length()) {
                    in = context.getAssets().open(assetDir + "/" + fileName);
                }
                else {
                    in = context.getAssets().open(fileName);
                }

                OutputStream out = new FileOutputStream(outFile);
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static  void copyFilesFromAssets(Context context) {
        DeepAssetUtil.CopyAssets(context, Globals.ApplicationDir, Globals.SDCARD_DIR);
    }

    //初始化识别资源
    public static long initRecognizer(Context context)
    {
        String cascade_filename = Globals.SDCARD_DIR + File.separator + Globals.cascade_filename;
        String finemapping_prototxt = Globals.SDCARD_DIR + File.separator + Globals.finemapping_prototxt;
        String finemapping_caffemodel = Globals.SDCARD_DIR + File.separator + Globals.finemapping_caffemodel;
        String segmentation_prototxt = Globals.SDCARD_DIR + File.separator + Globals.segmentation_prototxt;
        String segmentation_caffemodel = Globals.SDCARD_DIR + File.separator + Globals.segmentation_caffemodel;
        String character_prototxt = Globals.SDCARD_DIR + File.separator + Globals.character_prototxt;
        String character_caffemodel = Globals.SDCARD_DIR + File.separator + Globals.character_caffemodel;
        copyFilesFromAssets(context);
        //调用JNI 加载资源函数.
        return  DeepCarUtil.InitPlateRecognizer(
                cascade_filename,
                finemapping_prototxt,finemapping_caffemodel,
                segmentation_prototxt,segmentation_caffemodel,
                character_prototxt,character_caffemodel
        );
    }
}