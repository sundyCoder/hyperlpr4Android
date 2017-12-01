package pr.hyperlpr;

public class DeepCarUtil {

    public static final String demoImgPath = "demo.jpg";
    public static final String ApplicationDir = "hyperlpr";

    public static final String cascade_filename  =  "cascade.xml";
    public static final String finemapping_prototxt  =  "HorizonalFinemapping.prototxt";
    public static final String finemapping_caffemodel  = "HorizonalFinemapping.caffemodel";
    public static final String segmentation_prototxt =  "Segmentation.prototxt";
    public static final String segmentation_caffemodel =  "Segmentation.caffemodel";
    public static final String character_prototxt =  "CharacterRecognization.prototxt";
    public static final String character_caffemodel=  "CharacterRecognization.caffemodel";

    static native long InitPlateRecognizer(String casacde_detection,
                                           String finemapping_prototxt,String finemapping_caffemodel,
                                           String segmentation_prototxt,String segmentation_caffemodel,
                                           String charRecognization_proto,String charRecognization_caffemodel);

    static native String SimpleRecognization(long  inputMat,long object);
    static native long ReleasePlateRecognizer(long  object);
}
