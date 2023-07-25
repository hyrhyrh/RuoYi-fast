package com.ruoyi.project.monitor.job.task.pingrong;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.exception.base.BaseException;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.project.monitor.job.util.DataSourceContext;
import com.ruoyi.project.monitor.job.util.DataSourceManager;
import com.ruoyi.project.monitor.job.util.ParamKeyValue;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Author hyr
 * @Description 考勤统计查询
 * @Date create in 2023/7/12 9:14
 */
@Service("projectStatistics")
public class SyncProjectStatisticsTask {

    private JdbcTemplate jdbcTemplate;
    private static Map<String, String> bdMap = new HashMap<>();
    private static Map<String, String> biaoDuanMap = new HashMap<>();

    static {
        bdMap.put("2c08aa2670264ab2a09f381c15144c59", "1");
        bdMap.put("ad0d3cfacb914ac5a98505978041c1c3", "2");
        bdMap.put("0478368d052e4b12bd43abf7e3310477", "5");

        biaoDuanMap.put("2c08aa2670264ab2a09f381c15144c59", "一标段");
        biaoDuanMap.put("ad0d3cfacb914ac5a98505978041c1c3", "二标段");
        biaoDuanMap.put("0478368d052e4b12bd43abf7e3310477", "五标段");
    }

    public void execute() {
        DataSourceContext.getContext().setXaFlag(true);
        jdbcTemplate = new JdbcTemplate(DataSourceManager.instance().get(ParamKeyValue.DB_SLAVE));
        String pwd = "032c38c79eed91a790479b075686dbc4"; // 密钥
        String secret = Base64Utils.encodeToString(pwd.getBytes(StandardCharsets.UTF_8));
        String url = "http://che.gongyoumishu.com:8083/webapi/credential?appId=6c0a4649c38f4baf91b13656dc8284dd&secret=" + secret +
                "&grantType=client_credential";
        Map<String, String> reqMap = new HashMap<>();
        // 先获取token
        String request = HttpUtils.sendPost(url, JSONObject.toJSONString(reqMap));
        Map rsp = (Map) JSON.parse(request);
        if (!rsp.containsKey("data")) {
            return;
        }
        if (rsp.get("data") instanceof String) {
            String rspDataMsg = (String) rsp.get("data");
            throw new BaseException(rspDataMsg);
        }
        Map rspData = (Map) rsp.get("data");
        String token = MapUtils.getString(rspData, "token");
        String timestamp = String.valueOf(new Date().getTime());
        String str = "/private/sign/getProjectStatistics/v1?appId=6c0a4649c38f4baf91b13656dc8284dd&tokenSign=" + token + "&timestamp=" + timestamp; // 待加密字符串
        String tokenSign = DigestUtils.md5Hex(str.getBytes(StandardCharsets.UTF_8));

        String tempUrl = "http://che.gongyoumishu.com:8083/private/sign/getProjectStatistics/v1?appId=6c0a4649c38f4baf91b13656dc8284dd&tokenSign=" + tokenSign +
                "&timestamp=" + timestamp;
        String sql = "delete from t_project_statistics where 1 = 1";
        jdbcTemplate.update(sql);
        // 中交一公局广西平容高速公路项目1标段、2标段、5标段
        List<String> arrayList = Arrays.asList("2c08aa2670264ab2a09f381c15144c59", "ad0d3cfacb914ac5a98505978041c1c3", "0478368d052e4b12bd43abf7e3310477");
        for (String s : arrayList) {
            reqMap.clear();
            reqMap.put("projectId", s);
            reqMap.put("signDate", getDate());
            String grkqStr = HttpUtils.sendPost(tempUrl, JSONObject.toJSONString(reqMap));
            Map rspMap = (Map) JSON.parse(grkqStr);
            if (!rspMap.containsKey("data") || rspMap.get("data") instanceof String) {
                return;
            }
            Map data = (Map) rspMap.get("data");
            data.put("projectId", s);
            saveProjectStatistics(data);
        }
    }

    public void saveProjectStatistics(Map map) {
        String bd = bdMap.get(MapUtils.getString(map, "projectId"));
        String biaoduan = biaoDuanMap.get(MapUtils.getString(map, "projectId"));
        String roster = MapUtils.getInteger(map, "roster") + "";
        String attendance = MapUtils.getInteger(map, "attendance") + "";
        String leave = MapUtils.getInteger(map, "leave") + "";
        String avgTime = MapUtils.getString(map, "avgTime");
        String attendanceRate = MapUtils.getDouble(map, "attendanceRate") + "";
        String adjustAttendanceRate = MapUtils.getDouble(map, "adjustAttendanceRate") + "";
        String sql = "insert into t_project_statistics (bd,biaoduan,roster,attendance,leave_num,avgTime,attendanceRate,adjustAttendanceRate) values (?,?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql, new String[]{bd, biaoduan, roster, attendance, leave, avgTime, attendanceRate, adjustAttendanceRate});
    }

    private String getDate() {
        // 创建日期格式化对象，设置日期格式为"yyyy-MM-dd"
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        // 获取当前时间
        Date currentDate = new Date();
        // 使用日期格式化对象将当前时间格式化为指定格式
        String formattedDate = dateFormat.format(currentDate);
        return formattedDate;
    }

}
