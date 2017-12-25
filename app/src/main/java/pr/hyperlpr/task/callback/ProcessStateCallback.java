package pr.hyperlpr.task.callback;

/**
 * 抽象为进度, 可以使用在加载文件和 数据库数据.
 */
public interface ProcessStateCallback {
    void OnCancelProgram();

    void OnProgram(int currentCount, int totalCount, int errorCount);

    void OnProgramComplete();
}
