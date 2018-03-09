package pr.hyperlpr;

import android.os.Environment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class Globals {

    public static final String ApplicationDir = "hyperlpr";
    public static final String cascade_filename  =  "cascade.xml";
    public static final String finemapping_prototxt  =  "HorizonalFinemapping.prototxt";
    public static final String finemapping_caffemodel  = "HorizonalFinemapping.caffemodel";
    public static final String segmentation_prototxt =  "Segmentation.prototxt";
    public static final String segmentation_caffemodel =  "Segmentation.caffemodel";
    public static final String character_prototxt =  "CharacterRecognization.prototxt";
    public static final String character_caffemodel=  "CharacterRecognization.caffemodel";

    public static final String SDCARD_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +  Globals.ApplicationDir; //解压文件存放位置


    //key: 解析结果  value: 对应的文件.
    public static Map<String, String > successMap = new HashMap<>();
    public static Map<String, String > errorMap = new HashMap<>();

}
