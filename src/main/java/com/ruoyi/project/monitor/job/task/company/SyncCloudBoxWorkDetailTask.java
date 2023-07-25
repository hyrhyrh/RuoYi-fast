package com.ruoyi.project.monitor.job.task.company;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.project.monitor.job.util.DataSourceContext;
import com.ruoyi.project.monitor.job.util.DataSourceManager;
import com.ruoyi.project.monitor.job.util.ParamKeyValue;

/**
 * @Author hyr
 * @Description 云盒工时明细
 * @Date create in 2023/7/22 10:59
 */
@Service("cloudBoxWorkDetail")
public class SyncCloudBoxWorkDetailTask {
    private JdbcTemplate jdbcTemplate;

    public void execute() {
        DataSourceContext.getContext().setXaFlag(true);
        jdbcTemplate = new JdbcTemplate(DataSourceManager.instance().get(ParamKeyValue.DB_SLAVE_COMPANY));

        String delSql = "delete from cloud_box_work_detail where 1 = 1";
        jdbcTemplate.update(delSql);
        String sql = "select distinct sn from cloud_box_info where 1 = 1";
        List<String> mapList = jdbcTemplate.queryForList(sql, new String[] {}, String.class);
        for (String sn : mapList) {
            Map dataMap = new HashMap();
            dataMap.put("sn", sn);
            String method = "findWorkHourDetail";
            String apiKey = "TIEJIAO1ZV3S8LJIISICN2SSB7D0WJS2B1NJRT";
            String apisecret = "3VG9MRSU823NEBT2B2C1EETSC0QSGZZG";
            String data = JSONObject.toJSONString(dataMap);
            String secretData = data + apisecret;
            String code = DigestUtils.md5Hex(secretData.getBytes());
            String timestamp = String.valueOf(new Date().getTime());
            String queryString = "apiKey=" + apiKey + "&method=" + method + "&timeStamp=" + timestamp;
            String md5 = DigestUtils.md5Hex(queryString.getBytes());
            String sign = DigestUtils.md5Hex(md5 + apisecret);
            String reqParam = getPublicParam(data, method, code, timestamp);
            String url = "http://cloudboxapi.cehome.com/oapi/cloudbox/findWorkHourDetail?sign=" + sign;
            String result = HttpUtils.sendPost(url, reqParam);
            Map apiRsp = (Map) JSON.parse(result);
            if (MapUtils.isEmpty(apiRsp)) {
                continue;
            }
            try {
                Map rspData = (Map) apiRsp.get("data");
                String totleHour = MapUtils.getString(rspData, "totleHour");
                if (Objects.nonNull(rspData) && rspData.containsKey("details")) {
                    JSONArray list = (JSONArray) rspData.get("details");
                    Iterator<Object> iterator = list.iterator();
                    while (iterator.hasNext()) {
                        Map next = (Map) iterator.next();
                        next.put("totleHour", totleHour);
                        next.put("sn", sn);
                        saveWorkHourDetail(next);
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getPublicParam(String data, String method, String code, String timestamp) {
        Map map = new HashMap();
        map.put("code", code);
        map.put("data", data);
        map.put("org", "/1055/1056/1057");
        map.put("apiKey", "TIEJIAO1ZV3S8LJIISICN2SSB7D0WJS2B1NJRT");
        map.put("timeStamp", timestamp);
        map.put("method", method);
        return JSONObject.toJSONString(map);
    }

    private void saveWorkHourDetail(Map map) {
        String sn = MapUtils.getString(map, "sn");
        String totalHour = MapUtils.getString(map, "totleHour");
        String startTime = MapUtils.getString(map, "startTime");
        String endTime = MapUtils.getString(map, "endTime");
        String workhour = MapUtils.getString(map, "workhour");
        String dateRange = MapUtils.getString(map, "dateRange");
        String working = MapUtils.getString(map, "working");
        String sql = "INSERT INTO cloud_box_work_detail(sn,totalHour,startTime,endTime,workhour,dateRange,working) VALUES(?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, new String[] {
            sn, totalHour, startTime, endTime, workhour, dateRange, working
        });
    }
}
