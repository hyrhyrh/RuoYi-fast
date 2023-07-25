package com.ruoyi.project.monitor.job.task.company;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hikvision.artemis.sdk.ArtemisHttpUtil;
import com.hikvision.artemis.sdk.config.ArtemisConfig;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.project.monitor.job.util.DataSourceContext;
import com.ruoyi.project.monitor.job.util.DataSourceManager;
import com.ruoyi.project.monitor.job.util.DateTimeUtil;
import com.ruoyi.project.monitor.job.util.ParamKeyValue;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author hyr
 * @Description
 * @Date create in 2023/6/1 9:43
 */
@Service("syncVedioDataTask")
public class SyncVedioDataTask {

    protected static final Logger logger = LoggerFactory.getLogger(SyncVedioDataTask.class);

    /**
     * 请根据自己的appKey和appSecret更换static静态块中的三个参数. [1 host]
     * 如果你选择的是和现场环境对接,host要修改为现场环境的ip,https端口默认为443，http端口默认为80.例如10.33.25.22:443 或者10.33.25.22:80
     * appKey和appSecret请按照或得到的appKey和appSecret更改.
     * 调用前先要清楚接口传入的是什么，是传入json就用doPostStringArtemis方法，下载图片doPostStringImgArtemis方法
     */
    static {
        ArtemisConfig.host = "221.7.253.162:8088";// 代理API网关nginx服务器ip端口
        ArtemisConfig.appKey = "24687210";// 秘钥appkey
        ArtemisConfig.appSecret = "8U9xR3MxKiHpHULKAVpr";// 秘钥appSecret
    }

    /**
     * 能力开放平台的网站路径
     * TODO 路径不用修改，就是/artemis
     */
    private static final String ARTEMIS_PATH = "/artemis";
    private JdbcTemplate jdbcTemplate;

    public void execute() {
        DataSourceContext.getContext().setXaFlag(true);
        jdbcTemplate = new JdbcTemplate(DataSourceManager.instance().get(ParamKeyValue.DB_SLAVE_COMPANY));
        deal();
    }

    /**
     * 获取监控点列表接口可用来全量同步监控点信息，返回结果分页展示
     * http://10.33.47.50/artemis/api/scpms/v1/eventLogs/searches
     * 根据API文档可以看出来，这是一个POST请求的Rest接口，而且传入的参数值为一个json
     * ArtemisHttpUtil工具类提供了doPostStringArtemis这个函数，一共六个参数在文档里写明其中的意思，因为接口是https，
     * 所以第一个参数path是一个hashmap类型，请put一个key-value，query为传入的参数，body为传入的json数据
     * 传入的contentType为application/json，accept不指定为null
     * header没有额外参数可不传,指定为null
     *
     */
    public void deal() {
        String rsp = callCamerasApi(1, 1000);
        Map rspMap = (Map) JSON.parse(rsp);
        if (!rspMap.containsKey("data")) {
            return;
        }
        Map paramMap = (Map) rspMap.get("data");
        int pageSize = 9;
        int total = MapUtils.getInteger(paramMap, "total");
        int pages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1; // 总共多少页数据

        String sql = "select pageNo from t_hq_video_data where state = '1' limit 1";
        String pageNo = getStr(sql);
        sql = "update t_hq_video_data set state = '2' where state = '1'";
        jdbcTemplate.update(sql);
        int no = 1;
        if (StringUtils.isNotEmpty(pageNo)) {
            no = (Integer.valueOf(pageNo) + 1) > pages ? 1 : Integer.valueOf(pageNo) + 1; // 判断页数是否已经超出最大值，超出后返回重新查询
        }
        String result = callCamerasApi(no, pageSize);
        Map apiRes = (Map) JSON.parse(result);
        if (!apiRes.containsKey("data")) {
            return;
        }
        Map rspData = (Map) apiRes.get("data");
        JSONArray list = (JSONArray) rspData.get("list");
        Iterator<Object> iterator = list.iterator();
        while (iterator.hasNext()) {
            Map next = (Map) iterator.next();
            next.put("pageNo", no);
            callPreviewURLsApi(next);
        }
    }

    private String callCamerasApi(int pageNo, int pageSize) {
        String getCamsApi = ARTEMIS_PATH + "/api/resource/v1/cameras"; // 获取手机监控点列表
        Map<String, String> path = new HashMap<String, String>(2) {
            {
                put("https://", getCamsApi); //根据现场环境部署确认是http还是https
            }
        };
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("pageNo", pageNo);
        jsonBody.put("pageSize", pageSize);
        String body = jsonBody.toJSONString();
        logger.info("body: " + body);
        String result = ArtemisHttpUtil.doPostStringArtemis(path, body, null, null, "application/json", null);// post请求application/json类型参数
        logger.info("result: " + result);
        return result;
    }
    /**
     * 获取监控点预览取流URLv2
     */
    public void callPreviewURLsApi(Map map) {
        String cameraIndexCode = MapUtils.getString(map, "cameraIndexCode");
        String VechicleDataApi = ARTEMIS_PATH + "/api/video/v2/cameras/previewURLs"; // 获取hls协议流地址
        Map<String, String> path = new HashMap<String, String>(2) {
            {
                put("https://", VechicleDataApi);
            }
        };

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("cameraIndexCode", cameraIndexCode);
        jsonBody.put("streamType", "0");  // 0 主码流   1字码流 监控点唯一标识
        jsonBody.put("protocol", "ws"); // 取流协议 hls/ws
        jsonBody.put("transmode", "1"); // 传输协议 0:UDP 1:TCP 默认是TCP
//        jsonBody.put("expand", "transcode=1&resolution=QCIF&videotype=h264&audiotype=AAC");
        String body = jsonBody.toJSONString();
        logger.info("body: " + body);
        String result = ArtemisHttpUtil.doPostStringArtemis(path, body, null, null, "application/json", null);
        logger.info("result: " + result);
        Map rsp = (Map) JSON.parse(result);
        if (!rsp.containsKey("data")) {
            return;
        }
        Map rspData = (Map) rsp.get("data");
        String url = MapUtils.getString(rspData, "url");
        map.put("url", url);
        saveVedioData(map);
    }

    private void saveVedioData(Map map) {
        String cameraIndexCode = MapUtils.getString(map, "cameraIndexCode");
        String cameraName = MapUtils.getString(map, "cameraName");
        String cameraType = MapUtils.getString(map, "cameraType");
        String cameraTypeName = MapUtils.getString(map, "cameraTypeName");
        String url = MapUtils.getString(map, "url");
        String channelNo = MapUtils.getString(map, "channelNo");
        String channelType = MapUtils.getString(map, "channelType");
        String channelTypeName = MapUtils.getString(map, "channelTypeName");
        String createTime = DateTimeUtil.utcToDate(MapUtils.getString(map, "createTime"));
        String updateTime = DateTimeUtil.utcToDate(MapUtils.getString(map, "updateTime"));
        String transType = MapUtils.getString(map, "transType");
        String transTypeName = MapUtils.getString(map, "transTypeName");
        String pageNo = MapUtils.getString(map, "pageNo");
        String state = "1";
        String sql = "insert into t_hq_video_data (cameraIndexCode,cameraName,cameraType,cameraTypeName,url,channelNo,channelType,channelTypeName,createTime,updateTime,transType,transTypeName, pageNo, state) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql, new String[]{cameraIndexCode,cameraName,cameraType,cameraTypeName,url,channelNo,channelType,channelTypeName,createTime,updateTime,transType,transTypeName, pageNo, state});
    }

    private String getStr(String sql) {
        try {
            String s = jdbcTemplate.queryForObject(sql, String.class);
            return s;
        }
        catch (Exception e) {
            logger.info(e.getMessage());
        }
        return "";
    }
}
