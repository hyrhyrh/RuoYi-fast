package com.ruoyi.project.monitor.job.task.company;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.project.monitor.job.util.DataSourceContext;
import com.ruoyi.project.monitor.job.util.DataSourceManager;
import com.ruoyi.project.monitor.job.util.ParamKeyValue;

/**
 * @Author hyr
 * @Description 云盒绑定的车辆和用户
 * @Date create in 2023/7/22 10:59
 */
@Service("cloudBoxUserCar")
public class SyncCloudBoxUserCarTask {
    private JdbcTemplate jdbcTemplate;

    public void execute() {
        DataSourceContext.getContext().setXaFlag(true);
        jdbcTemplate = new JdbcTemplate(DataSourceManager.instance().get(ParamKeyValue.DB_SLAVE_COMPANY));
        String delSql = "delete from cloud_box_user_car where 1 = 1";
        jdbcTemplate.update(delSql);

        String sql = "select distinct sn from cloud_box_info where 1 = 1";
        List<String> mapList = jdbcTemplate.queryForList(sql, new String[] {}, String.class);
        for (String s : mapList) {
            Map dataMap = new HashMap();
            dataMap.put("sn", s);
            String method = "getUserCar";
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
            try {
                String url = "http://cloudboxapi.cehome.com/oapi/cloudbox/getUserCar?sign=" + sign;
                String result = HttpUtils.sendPost(url, reqParam);
                Map apiRsp = (Map) JSON.parse(result);
                if (MapUtils.isEmpty(apiRsp)) {
                    continue;
                }
                Map rspData = (Map) apiRsp.get("data");
                saveUserCar(rspData);
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

    private void saveUserCar(Map map) {
        String bindTime = MapUtils.getString(map, "bindTime");
        String phone = MapUtils.getString(map, "phone");
        String prevWorkhours = MapUtils.getString(map, "prevWorkhours");
        String model = MapUtils.getString(map, "model");
        String totalWorkhours = MapUtils.getString(map, "totalWorkhours");
        String realName = MapUtils.getString(map, "realName");
        String sn = MapUtils.getString(map, "sn");
        String carId = MapUtils.getString(map, "carId");
        String manufactTime = MapUtils.getString(map, "manufactTime");
        String category = MapUtils.getString(map, "category");
        String brand = MapUtils.getString(map, "brand");
        String deviceName = MapUtils.getString(map, "deviceName");
        String rackId = MapUtils.getString(map, "rackId");
        String sql = "INSERT INTO cloud_box_user_car(bindTime,phone,prevWorkhours,model,totalWorkhours,realName,sn,carId,manufactTime,category,brand,deviceName,rackId) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, new String[] {
            bindTime, phone, prevWorkhours, model, totalWorkhours, realName, sn, carId, manufactTime, category, brand,
            deviceName, rackId
        });
    }
}
