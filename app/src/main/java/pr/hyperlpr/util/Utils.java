package pr.hyperlpr.util;

import android.content.Context;

import java.io.File;
import java.io.IOException;

public class Utils {


    public static File getOrCreateExternalModelsRootDirectory(Context context) throws IOException {
        //获取外部文件夹路径, 通过 model,
        final File faceLockRoot = context.getExternalFilesDir("faceLock");

        if (faceLockRoot == null) {
            throw new IOException("Unable to access application external storage.");
        }

        if (!faceLockRoot.isDirectory() && !faceLockRoot.mkdir()) {
            throw new IOException("Unable to create model root directory: " +
                    faceLockRoot.getAbsolutePath());
        }
        return faceLockRoot;
    }

    private static String getFileExtension(final String filePath) {
        int lastPoi = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);
        if (lastPoi == -1 || lastSep >= lastPoi) return "";
        return filePath.substring(lastPoi + 1);
    }

    public static boolean checkFileExtension(File filePath){
        String imageSuffix = getFileExtension(filePath.getAbsolutePath()).toLowerCase();
        return "jpg".equals(imageSuffix) || "jpeg".equals(imageSuffix) || "png".equals(imageSuffix);
    }
}
