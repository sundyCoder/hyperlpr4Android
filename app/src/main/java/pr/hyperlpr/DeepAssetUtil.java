package pr.hyperlpr;

/**
 * Created by A1838 on 12/1/2017.
 */

import android.content.Context;
import android.content.res.Resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class DeepAssetUtil {
    public static final String TAG = "DeepAssetUtil";
    public static void CopyOneFile(String path,String dir, Resources Resid){
        File file=new File(String.format(dir+"/"+path));
        if(!file.exists())
        {
            try {
                copyFileFromAssetsToSDCard(path,dir+"/"+ path,Resid);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }
    private static void copyFileFromAssetsToSDCard(String resname, String sdpath, Resources Resid) throws Throwable {
        InputStream is =Resid.getAssets().open(resname);
        OutputStream os = new FileOutputStream(sdpath);
        byte data[] = new byte[1024];
        int len;
        while ((len = is.read(data)) > 0) {
            os.write(data, 0, len);
        }
        is.close();
        os.close();
    }
    public static void CopyAssets(Context context,String assetDir, String dir) {
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
                if (0 != assetDir.length())
                    in = context.getAssets().open(assetDir + "/" + fileName);
                else
                    in = context.getAssets().open(fileName);
                OutputStream out = new FileOutputStream(outFile);

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}