package com.ruoyi.project.monitor.job.util;

import com.ruoyi.common.utils.spring.SpringUtils;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @Author hyr
 * @Description 数据源管理器
 * @Date create in 2023/5/12 15:16
 */
public class DataSourceManager {

    protected static DataSourceManager instance;

    public synchronized static DataSourceManager instance() {
        if (instance == null) {
            instance = new DataSourceManager();
        }
        return instance;
    }

    public synchronized DataSource get(String code) {
        DataSource ds = null;
        if (DataSourceContext.getContext().isXaFlag()) {
            ds = SpringUtils.getBean(code);
            DataSourceContext.getContext().getContextMap().put(code, ds);
            Connection conn = DataSourceUtils.getConnection(ds);
            try {
                if (conn.getAutoCommit()) {
                    conn.setAutoCommit(false);//去除自动提交事务
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            ds = SpringUtils.getBean(ParamKeyValue.DB_MASTER);
        }
        return ds;
    }

    /**
     * 获取指定数据源的链接
     *
     * @param code
     * @return
     */
    public Connection getConnection(String code) {
        DataSource ds = this.get(code);
        Connection conn = DataSourceUtils.getConnection(ds);
        return conn;
    }

}
