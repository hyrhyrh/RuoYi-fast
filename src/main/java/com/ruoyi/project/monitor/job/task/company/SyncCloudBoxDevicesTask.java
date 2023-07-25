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
import org.thymeleaf.util.ListUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.project.monitor.job.util.DataSourceContext;
import com.ruoyi.project.monitor.job.util.DataSourceManager;
import com.ruoyi.project.monitor.job.util.ParamKeyValue;

/**
 * @Author hyr
 * @Description 进退场设备统计列表
 * @Date create in 2023/7/22 10:59
 */
@Service("cloudBoxDevices")
public class SyncCloudBoxDevicesTask {
    private JdbcTemplate jdbcTemplate;

    public void execute() {
        DataSourceContext.getContext().setXaFlag(true);
        jdbcTemplate = new JdbcTemplate(DataSourceManager.instance().get(ParamKeyValue.DB_SLAVE_COMPANY));

        String delSql = "delete from cloud_box_devices where 1 = 1";
        jdbcTemplate.update(delSql);
        long endTime = System.currentTimeMillis();
        Map dataMap = new HashMap();
        dataMap.put("startTime", endTime - 36000000); // 减去1小时（60分钟 * 60秒 * 1000毫秒）
        dataMap.put("endTime", endTime);
        String method = "findConstructionStatistics";
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
        String url = "http://cloudboxapi.cehome.com/oapi/cloudbox/findConstructionStatistics?sign=" + sign;
        String result = HttpUtils.sendPost(url, reqParam);
        Map apiRsp = (Map) JSON.parse(result);
        if (MapUtils.isEmpty(apiRsp)) {
            return;
        }
        Map rspData = (Map) apiRsp.get("data");
        if (Objects.nonNull(rspData) && rspData.containsKey("datas")) {
            List list = (JSONArray) rspData.get("datas");
            if (ListUtils.isEmpty(list)) {
                return;
            }
            Iterator<Object> iterator = list.iterator();
            while (iterator.hasNext()) {
                Map next = (Map) iterator.next();
                saveDevicesInfo(next);
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

    private void saveDevicesInfo(Map map) {
        String id = MapUtils.getString(map, "id");
        String sn = MapUtils.getString(map, "sn");
        String carId = MapUtils.getString(map, "carId");
        String status = MapUtils.getString(map, "status");
        String bindStatus = MapUtils.getString(map, "bindStatus");
        String manageCode = MapUtils.getString(map, "manageCode");
        String firstCategoryName = MapUtils.getString(map, "firstCategoryName");
        String brandName = MapUtils.getString(map, "brandName");
        String modelName = MapUtils.getString(map, "modelName");
        String eqSource = MapUtils.getString(map, "eqSource");
        String deviceName = MapUtils.getString(map, "deviceName");
        String standardWorkingHours = MapUtils.getString(map, "standardWorkingHours");
        String enterOrgName = MapUtils.getString(map, "enterOrgName");
        String enterOrg = MapUtils.getString(map, "enterOrg");
        String useUnit = MapUtils.getString(map, "useUnit");
        String lessorName = MapUtils.getString(map, "lessorName");
        String eqManager = MapUtils.getString(map, "eqManager");
        String driver = MapUtils.getString(map, "driver");
        String enterTime = MapUtils.getString(map, "enterTime");
        String exitTime = MapUtils.getString(map, "exitTime");
        String totalWorkedDays = MapUtils.getString(map, "totalWorkedDays");
        String avgOfWorked = MapUtils.getString(map, "avgOfWorked");
        String avgOfUsed = MapUtils.getString(map, "avgOfUsed");
        String totalWorkedhours = MapUtils.getString(map, "totalWorkedhours");
        String totalMileage = MapUtils.getString(map, "totalMileage");
        String maintainDays = MapUtils.getString(map, "maintainDays");
        String daysSheet = MapUtils.getString(map, "daysSheet");
        String attendanceRate = MapUtils.getString(map, "attendanceRate");
        String utilizationRate = MapUtils.getString(map, "utilizationRate");
        String workLoadSheetHours = MapUtils.getString(map, "workLoadSheetHours");
        String workLoadSheetTB = MapUtils.getString(map, "workLoadSheetTB");
        String workLoadSheetFL = MapUtils.getString(map, "workLoadSheetFL");
        String totalGasVolume = MapUtils.getString(map, "totalGasVolume");
        String specificFuelConsumption = MapUtils.getString(map, "specificFuelConsumption");
        String sql = "INSERT INTO cloud_box_devices(id,sn,carId,status,bindStatus,manageCode,firstCategoryName,brandName,modelName," +
                "eqSource,deviceName,standardWorkingHours,enterOrgName,enterOrg,useUnit,lessorName,eqManager,driver,enterTime," +
                "exitTime,totalWorkedDays,avgOfWorked,avgOfUsed,totalWorkedhours,totalMileage,maintainDays,daysSheet," +
                "attendanceRate,tutilizationRate,workLoadSheetHours,workLoadSheetTB,workLoadSheetFL,totalGasVolume,specificFuelConsumption) "
            + "VALUES(?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, new String[] {
            id, sn, carId, status, bindStatus, manageCode, firstCategoryName, brandName, modelName, eqSource,
            deviceName, standardWorkingHours, enterOrgName, enterOrg, useUnit, lessorName, eqManager, driver, enterTime,
            exitTime, totalWorkedDays, avgOfWorked, avgOfUsed, totalWorkedhours, totalMileage, maintainDays, daysSheet,
            attendanceRate, utilizationRate, workLoadSheetHours, workLoadSheetTB, workLoadSheetFL, totalGasVolume,
            specificFuelConsumption
        });
    }
}
