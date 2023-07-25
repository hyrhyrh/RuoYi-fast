package com.ruoyi.project.monitor.job.mapper;

import com.ruoyi.framework.aspectj.lang.annotation.DataSource;
import com.ruoyi.framework.aspectj.lang.enums.DataSourceType;
import com.ruoyi.project.monitor.job.domain.JiliangZhifu;

/**
 * @Author hyr
 * @Description 计量支付
 * @Date create in 2023/5/12 8:07
 */
public interface JiliangZhifuMapper {

    @DataSource(value = DataSourceType.SLAVE)
    int delJiliangZhifu();

    @DataSource(value = DataSourceType.SLAVE)
    int insertJiliangZhifu(JiliangZhifu obj);

    @DataSource(value = DataSourceType.SLAVEC)
    int delJlzFmonth();

    @DataSource(value = DataSourceType.SLAVEC)
    int insertJlzFmonth(JiliangZhifu obj);

    @DataSource(value = DataSourceType.SLAVE_GANSU)
    int delJiliangZhifuGanSu();

    @DataSource(value = DataSourceType.SLAVE_GANSU)
    int insertJiliangZhifuGanSu(JiliangZhifu obj);

    @DataSource(value = DataSourceType.SLAVE_GANSU)
    int delJlzFmonthGanSu();

    @DataSource(value = DataSourceType.SLAVE_GANSU)
    int insertJlzFmonthGanSu(JiliangZhifu obj);
}
