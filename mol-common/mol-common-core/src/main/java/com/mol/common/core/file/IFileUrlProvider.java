package com.mol.common.core.file;

import java.util.Set;

/**
 * 文件 URL 提供者接口
 * <p>
 * 作用：各个业务模块（如报修、用户）实现此接口，
 * 告诉清理任务哪些文件正在被使用，不能删除。
 * </p>
 */
public interface IFileUrlProvider {
    /**
     * 获取当前模块正在使用的所有文件 URL (或文件名)
     * @return 文件名集合
     */
    Set<String> getUsedFileUrls();
}