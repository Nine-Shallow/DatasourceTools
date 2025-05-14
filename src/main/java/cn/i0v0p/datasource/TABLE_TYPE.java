package cn.i0v0p.datasource;

/**
 * 定义数据库中的典型表类型
 */
public enum TABLE_TYPE {
    // 数据库表类型
    TABLE("普通表"),
    VIEW("视图"),
    SYSTEM_TABLE("系统表"),
    GLOBAL_TEMPORARY("全局临时表"),
    LOCAL_TEMPORARY("本地临时表"),
    ALIAS("别名"),
    SYNONYM("同义词");

    // 类型的描述信息
    private final String description;

    /**
     * 构造函数
     * @param description 类型的描述信息
     */
    TABLE_TYPE(String description) {
        this.description = description;
    }

    /**
     * 获取类型的描述信息
     * @return 描述信息
     */
    public String getDescription() {
        return description;
    }
}
