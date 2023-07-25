package com.ruoyi.project.monitor.job.task.gansu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.utils.ByteUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.project.monitor.job.domain.JiliangZhifu;
import com.ruoyi.project.monitor.job.mapper.JiliangZhifuMapper;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author hyr
 * @Description 同步计量支付数据
 * @Date create in 2023/7/15 11:03
 */
@Service("ganSuMeasurement")
public class SyncGanSuMeasurementDataTask {

    @Autowired
    private JiliangZhifuMapper jfMapper;

    public void execute() {
        String projectCode = "JK43Q";
        String time = String.valueOf(new Date().getTime());
        String key = "comm_gshs";
        String secret = "LRSJ80T5";
        String sign = DigestUtils.md5DigestAsHex(ByteUtils.getIsoBytes(key + secret + time));
        Map map = new HashMap();
        map.put("projectCode", projectCode);
        map.put("time", time);
        map.put("key", key);
        map.put("sign", sign);
        String url = "http://sync.jl.smartcost.com.cn/api/list";
        String request = HttpUtils.sendPost(url, JSONObject.toJSONString(map));
        Map rsp = (Map) JSON.parse(request);
        if (!rsp.containsKey("data")) {
            return;
        }
        JSONArray rspData = (JSONArray) rsp.get("data");
        if (Objects.nonNull(rspData)) {
            jfMapper.delJiliangZhifuGanSu();
            jfMapper.delJlzFmonthGanSu();
        }
        Iterator<Object> iterator = rspData.iterator();
        while (iterator.hasNext()) {
            Map next = (Map) iterator.next();
            String id = MapUtils.getString(next, "id");
            map.put("tenderId", id);
            getList(id, JSONObject.toJSONString(map));
        }
    }

    public void getList(String id, String data) {
        try {
            Thread.sleep(1000); // 避免使用
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String request = HttpUtils.sendPost("http://sync.jl.smartcost.com.cn/api/info", data);
        Map rsp = (Map) JSON.parse(request);
        if (MapUtils.isEmpty(rsp)) {
            return;
        }
        if (!rsp.containsKey("data")) {
            return;
        }
        Map rspData = (Map) rsp.get("data");
        if (MapUtils.isEmpty(rspData)) {
            return;
        }
        String name = MapUtils.getString(rspData, "name");
        long contractPrice = MapUtils.getLongValue(rspData, "contractPrice");
        List<Map> stages = ((List<Map>) rspData.get("stages")).stream().filter(item ->
                StringUtils.equals("审批通过", MapUtils.getString(item, "status"))).collect(Collectors.toList());
        Map order = stages.stream().max((a, b) -> {
            int order1 = (Integer) a.get("order");
            int order2 = (Integer) b.get("order");
            return order1 > order2 ? 1 : -1;
        }).orElse(new HashMap());
        if (MapUtils.isNotEmpty(order)) {
//            String status = MapUtils.getString(order, "status");
//            if (!StringUtils.equals("审批通过", status)) {
//                return;
//            }
            double endDealTp = 0;
            double endChangeTp = 0;
            double dealTp = 0;
            double changeTp = 0;
            if (order.get("end_deal_tp") instanceof Integer) {
                endDealTp = Double.valueOf((Integer)order.get("end_deal_tp"));
            }
            else {
                endDealTp = ((BigDecimal) order.get("end_deal_tp")).doubleValue();
            }
            if (order.get("end_change_tp") instanceof Integer) {
                endChangeTp = Double.valueOf((Integer) order.get("end_change_tp"));
            }
            else {
                endChangeTp = ((BigDecimal) order.get("end_change_tp")).doubleValue();
            }
            if (order.get("deal_tp") instanceof Integer) {
                dealTp = Double.valueOf((Integer) order.get("deal_tp"));
            }
            else {
                dealTp = ((BigDecimal) order.get("deal_tp")).doubleValue();
            }
            if (order.get("change_tp") instanceof Integer) {
                changeTp = Double.valueOf((Integer) order.get("change_tp"));
            }
            else {
                changeTp = ((BigDecimal) order.get("change_tp")).doubleValue();
            }
            String num = division(endDealTp + endChangeTp, contractPrice);
            JiliangZhifu obj = new JiliangZhifu();
            obj.setId(id);
            obj.setName(name);
            obj.setNum(num);
            obj.setAmount((endDealTp + endChangeTp) + "");
            obj.setContractPrice(contractPrice + "");
            jfMapper.insertJiliangZhifuGanSu(obj);

            String time = MapUtils.getString(order, "time");
            int sf_tp = MapUtils.getInteger(order, "sf_tp");
            int end_sf_tp = MapUtils.getInteger(order, "end_yf_tp");
            obj.setYueFen(time);
            obj.setEndDealTp(endDealTp);
            obj.setSfTp(sf_tp);
            obj.setEndSfTp(end_sf_tp);
            obj.setDealTp(dealTp);
            obj.setChangeTp(changeTp);
            jfMapper.insertJlzFmonthGanSu(obj);
        }
    }


    public String division(double a, double b) {
        String result = "";
        double num = a / b;
        DecimalFormat df = new DecimalFormat("0.00");
        result = df.format(num);
        return result;
    }

}
