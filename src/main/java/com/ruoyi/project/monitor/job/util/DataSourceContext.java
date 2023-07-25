package com.ruoyi.project.monitor.job.util;

import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author hyr
 * @Description 数据源上下文
 * @Date create in 2023/5/12 15:18
 */
public class DataSourceContext {


    private static ThreadLocal dataSourceContext = new DataSourceContextThreadLocal();

    private Map<String, DataSource> context = null;

    private Logger log;

    private boolean xaFlag = false;

    private DataSourceContext(Map context) {
        this.context = context;
    }

    private static class DataSourceContextThreadLocal extends ThreadLocal {
        protected Object initialValue() {
            return new DataSourceContext(new HashMap());
        }
    }

    public List<DataSource> getAllDataSource() {

        return new ArrayList(this.context.values());
    }

    public void clearAll() {
        this.context.clear();
    }


    public static DataSourceContext getContext() {
        DataSourceContext context = (DataSourceContext) dataSourceContext.get();
        if (context == null) {
            context = new DataSourceContext(new HashMap());
            setContext(context);
        }
        return context;
    }

    public void commitAll() throws SQLException {
        try {
            List<DataSource> dss = DataSourceContext.getContext().getAllDataSource();
            for (DataSource ds : dss) {
                Connection conn = null;
                try {
                    conn = DataSourceUtils.getConnection(ds);
                } catch (Throwable e) {
                    if (this.log != null) {
                        this.log.error(e);
                    } else {
                        e.printStackTrace();
                    }
                }
                if (conn != null && !conn.isClosed()) {
                    try {
                        conn.commit();
                    } catch (Throwable e) {
                        if (this.log != null) {
                            this.log.error(e);
                        } else {
                            e.printStackTrace();
                        }
                        try {
                            conn.rollback();
                        } catch (Throwable e2) {
                            if (this.log != null) {
                                this.log.error(e2);
                            } else {
                                e2.printStackTrace();
                            }
                        }
                    } finally {
                        try {
                            DataSourceUtils.releaseConnection(conn, ds);//释放链接
                        } catch (Throwable e2) {
                            if (this.log != null) {
                                this.log.error(e2);
                            } else {
                                e2.printStackTrace();
                            }
                        } finally {
                            try {
                                conn.close();
                            } catch (Throwable e3) {
                                if (this.log != null) {
                                    this.log.error(e3);
                                } else {
                                    e3.printStackTrace();
                                }
                            }
                        }
                        try {
                            TransactionSynchronizationManager.unbindResource(ds);//解除绑定
                        } catch (Throwable e2) {
                            if (this.log != null) {
                                this.log.error(e2);
                            } else {
                                e2.printStackTrace();
                            }
                        }
                    }

                }
            }
        } finally {
            this.clearAll();
        }

    }

    public void allRollBack() throws SQLException {
        try {
            List<DataSource> dss = DataSourceContext.getContext().getAllDataSource();
            for (DataSource ds : dss) {
                Connection conn = null;
                try {
                    conn = DataSourceUtils.getConnection(ds);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                if (conn != null && !conn.isClosed()) {
                    try {
                        conn.rollback();
                    } catch (Throwable e) {
                        if (this.log != null) {
                            this.log.error(e, e);
                        } else {
                            e.printStackTrace();
                        }
                    } finally {
                        try {
                            DataSourceUtils.releaseConnection(conn, ds);//释放链接
                        } catch (Throwable e2) {
                            if (this.log != null) {
                                this.log.error(e2, e2);
                            } else {
                                e2.printStackTrace();
                            }
                        }
                        try {
                            conn.close();
                        } catch (Throwable e3) {
                            if (this.log != null) {
                                this.log.error(e3, e3);
                            } else {
                                e3.printStackTrace();
                            }
                        }
                        try {
                            TransactionSynchronizationManager.unbindResource(ds);//解除绑定
                        } catch (Throwable e2) {
                            if (this.log != null) {
                                this.log.error("解除绑定异常", e2);
                            } else {
                                e2.printStackTrace();
                            }
                        }
                    }
                }
            }
        } finally {
            this.clearAll();
        }

    }


    public Map getContextMap() {
        return this.context;
    }

    public static void setContext(DataSourceContext context) {
        dataSourceContext.set(context);
    }


    public boolean isXaFlag() {
        return xaFlag;
    }


    /**
     * 设置使用多数据源标识
     * 如果 xaFlag为true 则使用xa多数据操作数据库 在代码中使用对应dataSourceCode获取数据源
     * 如果 xaFlag为false 则使用'dataSource'的获取数据源单一数据库，
     * 但是可以在事务开启前通过DataSourceRouter.route("ordJkrdb？");切换分库数据源 整个事务过程中只使用其中一个分库 代码中
     * 指定数据源编码不会生效
     *
     * @param xaFlag
     */
    public void setXaFlag(boolean xaFlag) {
        this.xaFlag = xaFlag;
    }

    public Logger getLog() {
        return log;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    public static void removeContext() {
        dataSourceContext.remove();
    }

}
