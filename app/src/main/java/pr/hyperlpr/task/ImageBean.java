package pr.hyperlpr.task;

public class ImageBean {
    public long totalTime;//耗时
    public int imageSize;  // 图片打小
    public String imagePath;
    public String resultString;

    @Override
    public String toString() {
        return "ImageBean{" +
                "totalTime=" + totalTime +
                ", imageSize=" + imageSize +
                ", imagePath='" + imagePath + '\'' +
                ", resultString='" + resultString + '\'' +
                '}';
    }
}
