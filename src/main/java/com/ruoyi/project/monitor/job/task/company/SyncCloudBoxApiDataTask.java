package com.ruoyi.project.monitor.job.task.company;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
 * @Description 铁道云盒开放接口
 * @Date create in 2023/7/19 16:41
 */
@Service("cloudBoxApiDataTask")
public class SyncCloudBoxApiDataTask {

    private JdbcTemplate jdbcTemplate;

    public void execute() {
        DataSourceContext.getContext().setXaFlag(true);
        jdbcTemplate = new JdbcTemplate(DataSourceManager.instance().get(ParamKeyValue.DB_SLAVE_COMPANY));

        String sql = "delete from cloud_box_info where 1 = 1";
        jdbcTemplate.update(sql);
        Map dataMap = new HashMap();
        dataMap.put("pageIndex", "1");
        dataMap.put("pageSize", "100");
        String method = "findBox";
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
        String url = "http://cloudboxapi.cehome.com/oapi/cloudbox/findBox?sign=" + sign;
        String result = HttpUtils.sendPost(url, reqParam);
        Map apiRsp = (Map) JSON.parse(result);
        Map rspData = (Map) apiRsp.get("data");
        if (MapUtils.isEmpty(rspData)) {
            return;
        }
        JSONArray list = (JSONArray) rspData.get("datas");
        Iterator<Object> iterator = list.iterator();
        while (iterator.hasNext()) {
            Map next = (Map) iterator.next();
            saveCloudBoxInfo(next);
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

    private void saveCloudBoxInfo(Map map) {
        String id = MapUtils.getString(map, "id");
        String imei = MapUtils.getString(map, "imei");
        String sn = MapUtils.getString(map, "sn");
        String activateStatus = MapUtils.getString(map, "activateStatus");
        String source = MapUtils.getString(map, "source");
        String activateTime = MapUtils.getString(map, "activateTime");
        String onlineStatus = MapUtils.getString(map, "onlineStatus");
        String bindStatus = MapUtils.getString(map, "bindStatus");
        String bindTime = MapUtils.getString(map, "bindTime");
        String carId = MapUtils.getString(map, "carId");
        String sql = "INSERT INTO cloud_box_info(id,imei,sn,activateStatus,source,activateTime,onlineStatus,bindStatus,bindTime,carId)"
            + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, new String[] {
            id, imei, sn, activateStatus, source, activateTime, onlineStatus, bindStatus, bindTime, carId
        });
    }
}
