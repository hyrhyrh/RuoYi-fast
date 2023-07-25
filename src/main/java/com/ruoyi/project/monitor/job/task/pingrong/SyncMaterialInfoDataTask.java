package com.ruoyi.project.monitor.job.task.pingrong;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.langdao.openapi.sdk.DefaultApiClient;
import com.langdao.openapi.sdk.LangdaoApiClient;
import com.langdao.openapi.sdk.exception.LangdaoOpenApiException;
import com.langdao.openapi.sdk.request.cms.ContractorMaterialSavingRequest;
import com.langdao.openapi.sdk.request.cms.ContractorPhysicalPartQuantityRequest;
import com.langdao.openapi.sdk.response.cms.ContractorPhysicalPartQuantityInfoResponse;
import com.langdao.openapi.sdk.response.cms.ContractorPhysicalPartQuantityResponse;
import com.langdao.openapi.sdk.response.cms.ProRawMaterialSavingVo;
import com.langdao.openapi.sdk.response.cms.RawMaterialSavingInfoResponse;
import com.langdao.openapi.sdk.response.cms.RawMaterialSavingResponse;
import com.langdao.openapi.sdk.response.cms.RawMaterialTypeSavingVo;
import com.ruoyi.project.monitor.job.util.DataSourceContext;
import com.ruoyi.project.monitor.job.util.DataSourceManager;
import com.ruoyi.project.monitor.job.util.ParamKeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * @Author hyr
 * @Description 物料信息同步
 * @Date create in 2023/5/12 15:08
 */
@Service("syncMaterialInfoDataTask")
public class SyncMaterialInfoDataTask {

    private static final Logger log = LoggerFactory.getLogger(SyncMaterialInfoDataTask.class);
    private JdbcTemplate jdbcTemplate;

    public void execute() {
        DataSourceContext.getContext().setXaFlag(true);
        jdbcTemplate = new JdbcTemplate(DataSourceManager.instance().get(ParamKeyValue.DB_SLAVE));
        long b = System.currentTimeMillis();
        String serverUrl = "http://openapi.jadinec.com/gateway";

        String app_id = "197165547346071556";
        String pid = "20230428";
        String prod_code = "1354572346";
        String app_private_key = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDXbmSwOPE+k5GuKIQ0S6/2utrPM3F3HBr0Zg89k5duydxxEIgkrOJcOIq4bJEuljXtklqIittzrEN7H5wNbhN4/GX0lvrl1XFkPiWRFDjczPFBMmmESjyKNQHMWTv2ARvEYv3dGfhQL6TwnVQ3EdJpOTdbtJ5YewY/21kNJgG8h80LKaJnxY8+JLpEfJZr+o11zQ3H7P2/g1vD0SCo4gudWzPpqRbwlndXfHdo0wyDbamy08OldxJzUZ/HiY5cO21JbFDarr51ygl3WAs9LAS2oQK6xXkjnOl9n8hZXsLWJKcUC7wUW/AaJeKGd3ZC1x3mkcy1n/MjWpQDY5G1ixB3AgMBAAECggEAR5F5CcIhAvTrVLxJD0aFsqXowyUH8NX5bm/paD9782ZEQZuIXwbAPHrcOuB2as+kIsSYJvxaY1c7I9Age6Wx+mN/S35M48XA4dhzinr5WQEMtOgWfchbzF5Htqd+b/92RgvM4oMaJyls1jKuI41cJXDIn0KlyCc2sEgikWeq9AJc4kIt/7FGb88+Ia6yD9XqVASGUvROb9p2ZBpLrHCAS5cLwHfmqRsvngIE5VWdQ1iEM1WzFzip7dovTFK2EHaHVcYfPsoLdx25ZpGrl3/e+FNQ239CR5LDUgKOHShYbCRdetx0pUOyeUiRkCc+I3Oh1qGBApY9mU5+gN7sX5UgoQKBgQD+xwzcg7Tl3ZXF8b+1w9BdX+g0PHsCYnHkv4//wANjjAJEM3b18EbOzOCPD2tNC21Fu/wWAU56yDXPT67IQ9V8INMCkK0KlK4fxUmQg6GFMxwp5fPDld3hlmmAMmD2RzV2d0VmDizuFMAmebx0a+XN3DH2d3rwWYHKh6HQwWvjIwKBgQDYdwNTP+3JBH7Mndbv0ZndkS49lvwBzh/stbOP7BDhBgUttO4VIxgCjGCEmZ9ngYc9Nfw9yUU40gzwO6D9RkubIcMPHPsjeYdUdOy7PXl9fTCM1xNMlwvlRyMXSdxjGtkQu7Ma+kfAEcc/T13a6WfeDaCa+vGB+ICaRRZ+CxpsnQKBgQCqob0nYr4gxQquC36NyZsfLkBh26+2pSAKR5G1g2/Bl99ctesSQ93oWqZ6qaT/cIu6jHAEfNOGv9fqBD/WuDeebo1jxmleEL2dYZAnTUE76EMQfIWJlDGKCCaYSSVPS9mugark8tF8kkEug5GHl0vNV4/Ota6MkiWu8q+ZFDGhaQKBgQDOwe61J8zyQo7y1pikfKnhDnkCbyLKfp3TtUKL58m/hcaQf88g9WJLnNXcEfhGH1yTxOroAUVTHp8pH2uIQAqbZMYxu1bN7ZirvPsZZYCTsm4bOJvAfk3oc+g6qFwPdWd8KizLSeAav2B3QlG9dU+2s4GCRstDf58mu3LedbCJqQKBgQDGc7b6uzVP1hAIMpaSRgxO0t31spAdksD2sD+KuZS4wiNhffw5sfudZGpV7GhhTOnj8K5sJTQTncNgnqUdRIJS3HN6SKxeGHpo3878JlRMnLytnflswzqykz3t2VGoU7qL+pmzRm305skZtMBLds8iyP5F+rM6xO+GQBsw3QRjLg==";

        String format = "json";
        String charset = "utf-8";
        String public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArfggUeAAiy2FbJrdHtWsa9pLFk5M7j0odrWt6tQ5LIc2uHSbwkCVSL6Vdm1EfRah7HIfDEeFjkUIneQv316tf6CI+Vu3IzBwhMLD0jplFKi6dQ/g6dbcw7PMHtn4x+69GIsm3QAmxzv2UoF41B+s8HD0EBYB/mTCIYc6i8WshYsGvo61NxE+hSluUUOPexPQpS2ONbxfgCIKJybMQBT+fFTEy9Lb/fuNy17veh4Z2rIsxejmYMoAZlvcV+i8BcmpuRcosc85iZGG8/jwfp8u8Y2g8RPj9jTW1ulWHk3ziRAgKbGNPp5kHp80QOIb87I5Sr04eR6K+A5Gd5XsFwny0wIDAQAB";
        LangdaoApiClient client = new DefaultApiClient(serverUrl, pid, app_id, app_private_key, format, prod_code, charset, public_key, "RSA2");

        ContractorPhysicalPartQuantityRequest request = new ContractorPhysicalPartQuantityRequest();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("contractorId", "379775182614695936");
        request.setBizContent(JSON.toJSONString(jsonObject));
        try {
            ContractorPhysicalPartQuantityResponse response = client.execute(request);
            List<ContractorPhysicalPartQuantityInfoResponse> data = response.getData();
            if (Objects.nonNull(data)) {
                String sql = "delete from material_info where 1 = 1";
                jdbcTemplate.update(sql);
                for (ContractorPhysicalPartQuantityInfoResponse infoResponse : data) {
                    save(infoResponse);
                }
            }
            if (response.isSuccess()) {
                log.info("***Success***");

            } else {
                log.info("**error**");
                log.info(response.getCode());
                log.info(response.getMsg());
                log.info(response.getSubCode());
                log.info(response.getSubMsg());
            }
        } catch (LangdaoOpenApiException e) {
            e.printStackTrace();
        }

        ContractorMaterialSavingRequest requestC = new ContractorMaterialSavingRequest();
        JSONObject jsonObjectC = new JSONObject();
        jsonObjectC.put("contractorId", "379775182614695936");
        requestC.setBizContent(JSON.toJSONString(jsonObjectC));
        try {
            RawMaterialSavingResponse response = client.execute(requestC);
            RawMaterialSavingInfoResponse data = response.getData();
            if (Objects.nonNull(data)) {
                String sql = "delete from material_quantity where 1 = 1";
                jdbcTemplate.update(sql);
                executeC(data);
            }
            if (response.isSuccess()) {
                log.info("***Success***");

            } else {
                log.info("**error**");
                log.info(response.getCode());
                log.info(response.getMsg());
                log.info(response.getSubCode());
                log.info(response.getSubMsg());
            }
        } catch (LangdaoOpenApiException e) {
            e.printStackTrace();
        }

        log.info(String.valueOf(System.currentTimeMillis() - b));
    }

    private void executeC(RawMaterialSavingInfoResponse data) {
        List<ProRawMaterialSavingVo> proSavingList = data.getProSavingList();
        for (ProRawMaterialSavingVo proRawMaterialSavingVo : proSavingList) {
            String proName = proRawMaterialSavingVo.getAbbreviationName();
            List<RawMaterialTypeSavingVo> materialTypeSavingList = proRawMaterialSavingVo.getMaterialTypeSavingList();
            for (RawMaterialTypeSavingVo rawMaterialTypeSavingVo : materialTypeSavingList) {
                save(rawMaterialTypeSavingVo, proName);
            }
        }
    }

    private void save(ContractorPhysicalPartQuantityInfoResponse data) {
        String proId = data.getProId() + "";
        String proName = data.getAbbreviationName();
        double totalDesignQuantity = data.getTotalDesignQuantity();
        double totalUseQuantity = data.getTotalUseQuantity();
        double completeDesignQuantity = data.getCompleteDesignQuantity();
        double savingExcess = data.getSavingExcess();
        double overshootRate = data.getOvershootRate();
        double totalTempQuantity = Objects.isNull(data.getTotalTempQuantity()) ? 0.0 : data.getTotalTempQuantity();
        String sql = "INSERT INTO material_info (proId, proName, totalDesignQuantity, totalUseQuantity, completeDesignQuantity, savingExcess, overshootRate, totalTempQuantity)" +
                " VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, new String[]{proId, proName, totalDesignQuantity + "", totalUseQuantity + "", completeDesignQuantity + "", savingExcess + "", overshootRate + "", totalTempQuantity + ""});
    }

    private void save(RawMaterialTypeSavingVo rawMaterialTypeSavingVo, String proName) {
        String materialType = rawMaterialTypeSavingVo.getMaterialType();
        BigDecimal theoryOvershootRate = rawMaterialTypeSavingVo.getTheoryOvershootRate();
        BigDecimal theorySavingExcess = rawMaterialTypeSavingVo.getTheorySavingExcess();
        BigDecimal theoryQuantity = rawMaterialTypeSavingVo.getTheoryQuantity();
        BigDecimal actualQuantity = rawMaterialTypeSavingVo.getActualQuantity();
        String unit = rawMaterialTypeSavingVo.getUnit();
        BigDecimal receiveQuantity = rawMaterialTypeSavingVo.getReceiveQuantity();
        String sql = "INSERT INTO material_quantity(proName, materialType, theoryOvershootRate, theorySavingExcess, theoryQuantity, actualQuantity, unit, receiveQuantity)" +
                " VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, new String[]{proName, materialType, theoryOvershootRate + "", theorySavingExcess + "", theoryQuantity + "", actualQuantity + "", unit, String.valueOf(receiveQuantity)});
    }


}
