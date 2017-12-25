package pr.hyperlpr.task.callback;

import pr.hyperlpr.task.ImageBean;

public interface ProgramImageCallback {

    void onProcessImageComplete(boolean isError, ImageBean imageBean);
}
