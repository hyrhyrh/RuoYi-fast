package com.ruoyi.project.monitor.job.task.pingrong;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.exception.base.BaseException;
import com.ruoyi.common.exception.user.UserException;
import com.ruoyi.common.utils.ExceptionUtil;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.project.monitor.job.util.DataSourceContext;
import com.ruoyi.project.monitor.job.util.DataSourceManager;
import com.ruoyi.project.monitor.job.util.DateTimeUtil;
import com.ruoyi.project.monitor.job.util.ParamKeyValue;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Author hyr
 * @Description 同步出勤率信息
 * @Date create in 2023/5/12 15:54
 */
@Service("constructionWorker")
public class SyncConstructionWorkerDataTask {

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
        String str = "/private/sign/queryProjectWorkerGroupTeam/v1?appId=6c0a4649c38f4baf91b13656dc8284dd&tokenSign=" + token + "&timestamp=" + timestamp; // 待加密字符串
        String tokenSign = DigestUtils.md5Hex(str.getBytes(StandardCharsets.UTF_8));

        String tempUrl = "http://che.gongyoumishu.com:8083/private/sign/queryProjectWorkerGroupTeam/v1?appId=6c0a4649c38f4baf91b13656dc8284dd&tokenSign=" + tokenSign +
                "&timestamp=" + timestamp;
        String sql = "delete from t_project_worker where 1 = 1";
        jdbcTemplate.update(sql);
        // 中交一公局广西平容高速公路项目1标段、2标段、5标段
        List<String> arrayList = Arrays.asList("2c08aa2670264ab2a09f381c15144c59", "ad0d3cfacb914ac5a98505978041c1c3", "0478368d052e4b12bd43abf7e3310477");
        for (String s : arrayList) {
            reqMap.clear();
            reqMap.put("projectId", s);
            String grkqStr = HttpUtils.sendPost(tempUrl, JSONObject.toJSONString(reqMap));
            Map rspMap = (Map) JSON.parse(grkqStr);
            if (!rspMap.containsKey("data")) {
                return;
            }
            Map data = (Map) rspMap.get("data");
            Iterator iterator = data.keySet().iterator();
            while (iterator.hasNext()) {
                Object key = iterator.next();
                Map param = (Map) data.get(key);
                param.put("teamName", key);
                param.put("projectId", s);
                saveProjectWorker(param);
            }
        }
    }

    public void saveProjectWorker(Map map) {
        String bd = bdMap.get(MapUtils.getString(map, "projectId"));
        String biaoduan = biaoDuanMap.get(MapUtils.getString(map, "projectId"));
        String teamName = MapUtils.getString(map, "teamName");
        String signWorkerCount = MapUtils.getInteger(map, "signWorkerCount") + "";
        String roster = MapUtils.getInteger(map, "roster") + "";
        String signInCount = MapUtils.getInteger(map, "signInCount") + "";
        String inProjectCount = MapUtils.getInteger(map, "inProjectCount") + "";
        String sql = "insert into t_project_worker (bd,biaoduan,teamName,signWorkerCount,roster,signInCount,inProjectCount) values (?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql, new String[]{bd, biaoduan, teamName, signWorkerCount, roster, signInCount, inProjectCount});
    }

}
