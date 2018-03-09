package pr.hyperlpr.util;

//caffe 初始化信息的配置 信息
public class DeepCarUtil {

    //提供初始化 识别的 JNI 接口
    public static native long InitPlateRecognizer(String casacde_detection,
                                                  String finemapping_prototxt, String finemapping_caffemodel,
                                                  String segmentation_prototxt, String segmentation_caffemodel,
                                                  String charRecognization_proto, String charRecognization_caffemodel);
    //简单识别接口
    public  static native String SimpleRecognization(long  inputMat, long object);
    public  static native long ReleasePlateRecognizer(long  object);
}
