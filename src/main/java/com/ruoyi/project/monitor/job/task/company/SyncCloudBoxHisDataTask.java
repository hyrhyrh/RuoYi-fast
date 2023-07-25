package com.ruoyi.project.monitor.job.task.company;

import java.lang.reflect.Array;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.project.monitor.job.util.DataSourceContext;
import com.ruoyi.project.monitor.job.util.DataSourceManager;
import com.ruoyi.project.monitor.job.util.ParamKeyValue;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.ListUtils;

/**
 * @Author hyr
 * @Description 云盒历史GPS轨迹
 * @Date create in 2023/7/21 10:59
 */
@Service("cloudBoxHisData")
public class SyncCloudBoxHisDataTask {
    private JdbcTemplate jdbcTemplate;

    public void execute() {
        DataSourceContext.getContext().setXaFlag(true);
        jdbcTemplate = new JdbcTemplate(DataSourceManager.instance().get(ParamKeyValue.DB_SLAVE_COMPANY));

        String sql = "select distinct sn from cloud_box_info where 1 = 1";
        List<String> mapList = jdbcTemplate.queryForList(sql, new String[] {}, String.class);
        long endTime = System.currentTimeMillis();
        for (String s : mapList) {
            Map dataMap = new HashMap();
            dataMap.put("sn", s);
            dataMap.put("startTime", endTime - 3600000); // 减去1小时（60分钟 * 60秒 * 1000毫秒）
            dataMap.put("endTime", endTime);
            String method = "findHisGps";
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
            String url = "http://cloudboxapi.cehome.com/oapi/cloudbox/findHisGps?sign=" + sign;
            String result = HttpUtils.sendPost(url, reqParam);
            Map apiRsp = (Map) JSON.parse(result);
            if (MapUtils.isEmpty(apiRsp)) {
                continue;
            }
            List list = (JSONArray) apiRsp.get("data");
            if (ListUtils.isEmpty(list)) {
                continue;
            }
            Iterator<Object> iterator = list.iterator();
            while (iterator.hasNext()) {
                Map next = (Map) iterator.next();
                saveCloudBoxHisInfo(next);
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

    private void saveCloudBoxHisInfo(Map map) {
        String sn = MapUtils.getString(map, "sn");
        String lat = MapUtils.getString(map, "lat");
        String lon = MapUtils.getString(map, "lon");
        String speed = MapUtils.getString(map, "speed");
        String direction = MapUtils.getString(map, "direction");
        String uploadTime = MapUtils.getString(map, "uploadTime");
        String sql = "INSERT INTO cloud_box_his_info(sn,lat,lon,speed,direction,uploadTime) VALUES(?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, new String[] {
            sn, lat, lon, speed, direction, uploadTime
        });
    }
}
