package com.ruoyi.project.monitor.job.task.company;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.langdao.openapi.sdk.DefaultApiClient;
import com.langdao.openapi.sdk.LangdaoApiClient;
import com.langdao.openapi.sdk.exception.LangdaoOpenApiException;
import com.langdao.openapi.sdk.request.cms.EngineeringItemCategoryStatisticSumRequest;
import com.langdao.openapi.sdk.request.cms.EngineeringItemProgressStatisticRequest;
import com.langdao.openapi.sdk.request.cms.MaterialProQuantityListRequest;
import com.langdao.openapi.sdk.request.cms.MaterialStockPageRequest;
import com.langdao.openapi.sdk.request.cms.OrgConcreteJcGradeQuantityRequest;
import com.langdao.openapi.sdk.request.cms.OrgLineStatisticsRequest;
import com.langdao.openapi.sdk.request.cms.ProConcreteJcGradeQuantityRequest;
import com.langdao.openapi.sdk.request.cms.ProListByCompanyIdRequest;
import com.langdao.openapi.sdk.request.cms.ProPourProgressListRequest;
import com.langdao.openapi.sdk.request.cms.SumMaterialsRequest;
import com.langdao.openapi.sdk.request.cms.TemporaryConcreteListRequest;
import com.langdao.openapi.sdk.response.cms.CompanyMaterialJcMaterialTypeQuantityResponse;
import com.langdao.openapi.sdk.response.cms.CompanyMaterialJcProQuantityResponse;
import com.langdao.openapi.sdk.response.cms.EngineeringItemCategoryStatisticResponse;
import com.langdao.openapi.sdk.response.cms.EngineeringItemCategoryStatisticSumResponse;
import com.langdao.openapi.sdk.response.cms.EngineeringItemProgressStatisticDetailsResponse;
import com.langdao.openapi.sdk.response.cms.EngineeringItemProgressStatisticResponse;
import com.langdao.openapi.sdk.response.cms.MaterialProQuantityListResponse;
import com.langdao.openapi.sdk.response.cms.MaterialProQuantityResponse;
import com.langdao.openapi.sdk.response.cms.OrgLineStatisticsResponse;
import com.langdao.openapi.sdk.response.cms.ProListByCompanyIdResponse;
import com.langdao.openapi.sdk.response.cms.ProListResponse;
import com.langdao.openapi.sdk.response.cms.ProPourProgressListResponse;
import com.langdao.openapi.sdk.response.cms.ProPourProgressResponse;
import com.langdao.openapi.sdk.response.cms.TemporaryConcreteListResponse;
import com.langdao.openapi.sdk.response.cms.TemporaryConcreteProListResponse;
import com.langdao.openapi.sdk.response.cms.TemporaryConcreteProResponse;
import com.langdao.openapi.sdk.response.cms.TemporaryConcreteResponse;
import com.langdao.openapi.sdk.response.cms.concrete.CompanyConcreteJcGradeQuantityResponse;
import com.langdao.openapi.sdk.response.cms.concrete.CompanyConcreteJcQuantityResponse;
import com.langdao.openapi.sdk.response.cms.concrete.OrgConcreteJcGradeQuantityResponse;
import com.langdao.openapi.sdk.response.cms.concrete.ProConcreteJcGradeQuantityResponse;
import com.langdao.openapi.sdk.response.cms.concrete.SectionOverStatisticByConcreteResponse;
import com.langdao.openapi.sdk.response.cms.concrete.SectionOverStatisticByGradeConcreteListResponse;
import com.langdao.openapi.sdk.response.cms.materialStock.CompanyMaterialGatherStatisticForeignResponse;
import com.langdao.openapi.sdk.response.cms.materialStock.MaterialStockPageListResponse;
import com.langdao.openapi.sdk.response.cms.materialStock.MaterialStockPageResponse;
import com.langdao.openapi.sdk.response.cms.materialStock.ProMaterialGatherStatisticForeignResponse;
import com.langdao.openapi.sdk.response.cms.materialStock.SumMaterialsResponse;
import com.langdao.openapi.sdk.response.cms.orgLineStatistics.OrgConcreteResponse;
import com.langdao.openapi.sdk.response.cms.orgLineStatistics.OrgLineStatisticsListResponse;
import com.langdao.openapi.sdk.response.cms.orgLineStatistics.ProConcreteToOrgResponse;
import com.langdao.openapi.sdk.response.cms.orgLineStatistics.ProLineStatisticToOrgResponse;
import com.ruoyi.project.monitor.job.util.DataSourceContext;
import com.ruoyi.project.monitor.job.util.DataSourceManager;
import com.ruoyi.project.monitor.job.util.ParamKeyValue;

/**
 * @Author hyr
 * @Description 智砼道合数据同步
 * @Date create in 2023/6/12 15:08
 */
@Service("syncZhiTongDaoHeDataTask")
public class SyncZhiTongDaoHeDataTask {

    private static final Logger log = LoggerFactory.getLogger(SyncZhiTongDaoHeDataTask.class);

    private JdbcTemplate jdbcTemplate;

    public void execute() {
        DataSourceContext.getContext().setXaFlag(true);
        jdbcTemplate = new JdbcTemplate(DataSourceManager.instance().get(ParamKeyValue.DB_SLAVE_COMPANY));
        long b = System.currentTimeMillis();
        String serverUrl = "http://openapi.jadinec.com/gateway";

        String app_id = "197165547346071557";
        String pid = "20230607";
        String prod_code = "343083933654295108";
        String app_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCmiId9z//pI9MsMbTo/+0GnLNgUyfTC6zWBhyZigaPWnmwXIo16OV4NZC9Oebet+7CCGn/0sopldBW8W4tWb9ZDbB7SP6dn0mCFFmanDEEDP9RU+Hvr9l5cc2+ba5tp0Y2LehSSgLb3/0Ah9UNgVnkh6YLnA7elWfO6qLOxy08+QjjWUKla1ni7/yzH4zM/jPvNmsaAbYxM67XspE9ftxUH++ub+cXSHF9cdNqA18+8Z1Ou+twWSxSSAKNHRSLi7UbELL7QhK032t4uTs/op69C6vKSemu5T2IlmSWbvxpTFaZd5TElColmxco26ipSqd5am6+cIuCkm3/vVFC2REtAgMBAAECggEAYnIXVqKdL99DuL5xqcORBPWhfHHr+vDN7X+TbJiRpDDh+wXZlmDlNhjERXhzKHonJIEA2IBtjgPIM31pmlmRqj1TdK4EBn3hYIpTJfjraO+eBx/FUoHr18UU0VvZX20hLmoZCnxCEGTrFWM26VLsJxKhb/+DMsWzpjhComZQMdg5bDnxlyPSRk8ZiX0XpqLdo4KJ6Bt+v5GraARbw3s1TCn+5Sozt6+7CFJGhtCArDpvtprx2F/72NXCLZnjQjRzIX/Xlu8KgTxPhP8o+Y53pjzQyIxtl0g5Mx1v6LZJ7zqYF3+4opjpzqHEuLywvIE/+mso9OPWgLuaTiya6HOKaQKBgQDcVy/SRdQuqLClwqiOV1+4Xyah0fLyG5gKCJzqPGDfvRBSc2PlEADu6lZuFz5sowkZ6/3KaUHm2D9f8GMPCHUpufbEil/O9LEYgiDI9olWkJ1Glp5llWevec4vBnxkW+B1huDeVXxHe+XkMSgR6EnlQjbNtCKU7ZM1fRxi7sqIYwKBgQDBfBUneARSFKPOTIwUHyVR55MMOkO3IEUuQOmRA72ZlS0DEAk2riS/ibp+QQbzL4hiE0/5R5IzpLWpKH6mtznEvEhgjYSYkRn68EK+RUvdB59kYvy2QTsoCZWIooBGQ5ocqUmthkbI9iZbCLbDnPNoXV/F/ablLFrZ3+YHQl8NLwKBgHrPXaglo44eF9dMGAa+TGvHHRE0o+St3KA7DLrBzHzoNCDAj/0sXljL5ECYVALZHEmGb35SDJlODoS6x3upiyQmspP5TE1dtRej8AIS55mIISEm8kJMc+4qRfGP++U0EHdxJgTGQO7W79uwSlUw038k2MbukBfSEyrNAPVjgx9hAoGAJu3SS8ioBePFOYLJRFsn5RzUx0cJzNSfVrkUxWqdQUN0dQNK90d+eVM6xKBMx60uoZ0FIEC4N8/SBSDyqpR++Qhw/4w5u4CmW4jvxNyocOZRuZ74/aqOPnX/MiiP6pM7dLzS6Cj2IQT1nqdYLm9rwfnXhTyN0dpkxSV5YMiH210CgYEAtJpWtASp+sBaUbwLkYP1HcgvYTqGiECTi5JKEWTTW1uGD0SBz0w0ReHnhMLHMdG8fd4XvmTXWMRswteqHSc3/T/5Kep6EWPE9cMCrgfFg07ELO0QDvKNN/5+dyphYuX65bNwUs3dO9ugx/E2+CHHULmLX1r2w7vU4F+i9+Fja/I=";
        String format = "json";
        String charset = "utf-8";
        String public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArfggUeAAiy2FbJrdHtWsa9pLFk5M7j0odrWt6tQ5LIc2uHSbwkCVSL6Vdm1EfRah7HIfDEeFjkUIneQv316tf6CI+Vu3IzBwhMLD0jplFKi6dQ/g6dbcw7PMHtn4x+69GIsm3QAmxzv2UoF41B+s8HD0EBYB/mTCIYc6i8WshYsGvo61NxE+hSluUUOPexPQpS2ONbxfgCIKJybMQBT+fFTEy9Lb/fuNy17veh4Z2rIsxejmYMoAZlvcV+i8BcmpuRcosc85iZGG8/jwfp8u8Y2g8RPj9jTW1ulWHk3ziRAgKbGNPp5kHp80QOIb87I5Sr04eR6K+A5Gd5XsFwny0wIDAQAB";
        LangdaoApiClient client = new DefaultApiClient(serverUrl, pid, app_id, app_private_key, format, prod_code,
            charset, public_key, "RSA2");

        callProListByCompanyId(client); // 根据公司 id 查询项目信息, 项目纬度 混凝土标号查询 (完成1)

        callEngineeringItemProgressStatistic(client); // 浇筑进度明细-工程类别进度汇总 TODO

        callEngineeringItemCategoryStatisticSum(client); // 浇筑进度明细-工程类别进度列表 (完成1)

        callProPourProgressList(client); // 浇筑进度汇总-项目进度汇总列表 （完成1）

        // callTeamProPourProgressList(client); // 协作队伍汇总-按项目汇总（实体）列表 TODO

        callMaterialProQuantityList(client); // 地图大屏-项目原材料节超详情 (完成1)

        callTemporaryConcreteList(client); // 混凝土统计-临建用砼列表 (完成1)

        callOrgLineStatisticsRequest(client); // 砼生产量统计 (完成1)

        callSumMaterialsRequest(client); // 原材料汇总统计 (完成1)

        callOrgConcreteJcGradeQuantity(client); // 公司纬度 混凝土标号查询 (完成1)

        log.info(String.valueOf(System.currentTimeMillis() - b));
    }

    /**
     * 根据公司 id 查询项目信息
     * 
     * @param client
     */
    private void callProListByCompanyId(LangdaoApiClient client) {
        ProListByCompanyIdRequest requestC = new ProListByCompanyIdRequest();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("companyId", "241649062955323392");
        requestC.setBizContent(JSON.toJSONString(jsonObject));
        try {
            ProListByCompanyIdResponse response = client.execute(requestC);
            List<ProListResponse> dataList = response.getData();
            if (!CollectionUtils.isEmpty(dataList)) {
                String sql = "delete from t_pro_concrete_grade_quantity where 1 = 1";
                jdbcTemplate.update(sql);
                sql = "delete from t_pro_concrete_grade_quantity_detail where 1 = 1";
                jdbcTemplate.update(sql);
                sql = "delete from t_material_stock where 1 = 1";
                jdbcTemplate.update(sql);
                for (ProListResponse proListResponse : dataList) {
                    callProConcreteJcGradeQuantityRequest(client, proListResponse); // 项目纬度 混凝土标号查询

                    callMaterialStockPageRequest(client, proListResponse); // 物料库存统计
                }
            }
            if (response.isSuccess()) {
                log.info("***Success***");

            }
            else {
                log.info("**error**");
                log.info(response.getCode());
                log.info(response.getMsg());
                log.info(response.getSubCode());
                log.info(response.getSubMsg());
            }
        }
        catch (LangdaoOpenApiException e) {
            e.printStackTrace();
        }

    }

    /**
     * 浇筑进度明细-工程类别进度汇总
     *
     * @param client
     */
    private void callEngineeringItemProgressStatistic(LangdaoApiClient client) {
        EngineeringItemProgressStatisticRequest requestC = new EngineeringItemProgressStatisticRequest();
        try {
            List<String> strings = Arrays.asList("0", "1", "2", "3", "5"); // 查询范围 0-开累；1-当年；2-当月；3-当日； 4-自定义;5-当季
            for (String queryType : strings) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("companyId", "241649062955323392");
                jsonObject.put("queryType", queryType);
                requestC.setBizContent(JSON.toJSONString(jsonObject));
                EngineeringItemProgressStatisticResponse response = client.execute(requestC);
                EngineeringItemProgressStatisticDetailsResponse data = response.getData();
                if (Objects.nonNull(data)) {
                    saveEngineeringItemProgress(data);
                }
                if (response.isSuccess()) {
                    log.info("***Success***");

                }
                else {
                    log.info("**error**");
                    log.info(response.getCode());
                    log.info(response.getMsg());
                    log.info(response.getSubCode());
                    log.info(response.getSubMsg());
                }
            }
        }
        catch (LangdaoOpenApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * 浇筑进度明细-工程类别进度列表
     *
     * @param client
     */
    private void callEngineeringItemCategoryStatisticSum(LangdaoApiClient client) {
        EngineeringItemCategoryStatisticSumRequest request = new EngineeringItemCategoryStatisticSumRequest();
        try {
            String sql = "delete from t_pro_engineering_item_category where 1 = 1";
            jdbcTemplate.update(sql);
            List<String> strings = Arrays.asList("0", "1", "2", "3", "5"); // 查询范围 0-开累；1-当年；2-当月；3-当日； 4-自定义;5-当季
            for (String queryType : strings) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("companyId", "241649062955323392");
                jsonObject.put("queryType", queryType);
                request.setBizContent(JSON.toJSONString(jsonObject));
                EngineeringItemCategoryStatisticSumResponse response = client.execute(request);
                List<EngineeringItemCategoryStatisticResponse> data = response.getData();
                if (Objects.nonNull(data)) {
                    for (EngineeringItemCategoryStatisticResponse infoResponse : data) {
                        sallEngineeringItemCategoryStatisticSum(infoResponse, queryType);
                    }
                }
                if (response.isSuccess()) {
                    log.info("***Success***");

                }
                else {
                    log.info("**error**");
                    log.info(response.getCode());
                    log.info(response.getMsg());
                    log.info(response.getSubCode());
                    log.info(response.getSubMsg());
                }
            }
        }
        catch (LangdaoOpenApiException e) {
            e.printStackTrace();
        }
    }

    private void sallEngineeringItemCategoryStatisticSum(EngineeringItemCategoryStatisticResponse infoResponse,
        String queryType) {
        Integer engineeringCategory = infoResponse.getEngineeringCategory();
        String engineeringCategoryName = infoResponse.getEngineeringCategoryName();
        Integer totalCount = infoResponse.getTotalCount();
        Double designTotalQuantity = infoResponse.getDesignTotalQuantity();
        Integer finishCount = infoResponse.getFinishCount();
        Double finishQuantity = infoResponse.getFinishQuantity();
        Double totalQuantity = infoResponse.getTotalQuantity();
        Integer applyingCount = infoResponse.getApplyingCount();
        String finishRatio = infoResponse.getFinishRatio();
        Double jcQuantity = infoResponse.getJcQuantity();
        String jcRatio = infoResponse.getJcRatio();
        Double finishDesign = infoResponse.getFinishDesign();
        // List<EngineeringItemTypeStatisticSumResponse> typeStatisticSumList = infoResponse.getTypeStatisticSumList();
        String sql = "INSERT INTO t_pro_engineering_item_category(engineeringCategory,engineeringCategoryName,queryType,totalCount,designTotalQuantity,finishCount,finishQuantity,totalQuantity,applyingCount,finishRatio,jcQuantity,jcRatio,finishDesign)"
            + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, new String[] {
            engineeringCategory + "", engineeringCategoryName, queryType, totalCount + "", designTotalQuantity + "",
            finishCount + "", finishQuantity + "", totalQuantity + "", applyingCount + "", finishRatio, jcQuantity + "",
            jcRatio, finishDesign + ""
        });
    }

    private void callProPourProgressList(LangdaoApiClient client) {
        ProPourProgressListRequest request = new ProPourProgressListRequest();
        try {
            String sql = "delete from t_pro_four_progress where 1 = 1";
            jdbcTemplate.update(sql);
            List<String> strings = Arrays.asList("0", "1", "2", "3", "5"); // 查询范围 0-开累；1-当年；2-当月；3-当日； 4-自定义;5-当季
            for (String queryType : strings) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("companyId", "241649062955323392");
                jsonObject.put("queryType", queryType);
                request.setBizContent(JSON.toJSONString(jsonObject));
                ProPourProgressResponse response = client.execute(request);
                List<ProPourProgressListResponse> data = response.getData();
                if (!CollectionUtils.isEmpty(data)) {
                    for (ProPourProgressListResponse infoResponse : data) {
                        saveProPourProgressList(infoResponse, queryType);
                    }
                }
                if (response.isSuccess()) {
                    log.info("***Success***");
                }
                else {
                    log.info("**error**");
                    log.info(response.getCode());
                    log.info(response.getMsg());
                    log.info(response.getSubCode());
                    log.info(response.getSubMsg());
                }
            }
        }
        catch (LangdaoOpenApiException e) {
            e.printStackTrace();
        }
    }

    private void callTeamProPourProgressList(LangdaoApiClient client) {

    }

    private void callMaterialProQuantityList(LangdaoApiClient client) {
        MaterialProQuantityListRequest request = new MaterialProQuantityListRequest();
        try {
            String sql = "delete from t_material_jc_pro_quantity where 1 = 1";
            jdbcTemplate.update(sql);
            sql = "delete from t_material_jc_pro_quantity_detail where 1 = 1";
            jdbcTemplate.update(sql);
            List<String> strings = Arrays.asList("0", "1", "2", "3", "5"); // 查询范围 0-开累；1-当年；2-当月；3-当日； 4-自定义;5-当季
            for (String queryType : strings) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("companyId", "241649062955323392");
                jsonObject.put("queryType", queryType);
                request.setBizContent(JSON.toJSONString(jsonObject));
                MaterialProQuantityResponse response = client.execute(request);
                MaterialProQuantityListResponse data = response.getData();
                if (Objects.nonNull(data)) {
                    saveMaterialProQuantityList(data, queryType);
                }
                if (response.isSuccess()) {
                    log.info("***Success***");
                }
                else {
                    log.info("**error**");
                    log.info(response.getCode());
                    log.info(response.getMsg());
                    log.info(response.getSubCode());
                    log.info(response.getSubMsg());
                }
            }
        }
        catch (LangdaoOpenApiException e) {
            e.printStackTrace();
        }
    }

    private void callTemporaryConcreteList(LangdaoApiClient client) {
        TemporaryConcreteListRequest request = new TemporaryConcreteListRequest();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("companyId", "241649062955323392");
        request.setBizContent(JSON.toJSONString(jsonObject));
        try {
            TemporaryConcreteResponse response = client.execute(request);
            TemporaryConcreteListResponse data = response.getData();
            if (Objects.nonNull(data)) {
                String sql = "delete from t_pro_temporary_concrete where 1 = 1";
                jdbcTemplate.update(sql);
                sql = "delete from t_pro_temporary_concrete_detail where 1 = 1";
                jdbcTemplate.update(sql);
                saveTemporaryConcreteList(data);
            }
            if (response.isSuccess()) {
                log.info("***Success***");
            }
            else {
                log.info("**error**");
                log.info(response.getCode());
                log.info(response.getMsg());
                log.info(response.getSubCode());
                log.info(response.getSubMsg());
            }
        }
        catch (LangdaoOpenApiException e) {
            e.printStackTrace();
        }
    }

    private void saveEngineeringItemProgress(EngineeringItemProgressStatisticDetailsResponse data) {
        int totalCount = data.getTotalCount();
        int finishCount = data.getFinishCount();
        int applyingCount = data.getApplyingCount();
        double designTotalQuantity = data.getDesignTotalQuantity();
        double finishQuantity = data.getFinishQuantity();
        String finishRatio = data.getFinishRatio();
        double jcQuantity = data.getJcQuantity();
        String jcRatio = data.getJcRatio();
        Double designQuantity = data.getDesignQuantity();
        // TODO
        // String sql = "INSERT INTO material_info (proId, proName, totalDesignQuantity, totalUseQuantity,
        // completeDesignQuantity, savingExcess, overshootRate, totalTempQuantity)" +
        // " VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        // jdbcTemplate.update(sql, new String[]{});

    }

    private void saveEngineeringItemCategoryStatistic(EngineeringItemCategoryStatisticResponse data) {
        String engineeringCategoryName = data.getEngineeringCategoryName();
        Integer dataTotalCount = data.getTotalCount();
        Integer applyingCount = data.getApplyingCount();
        Integer finishCount = data.getFinishCount();
    }

    private void saveProPourProgressList(ProPourProgressListResponse data, String queryType) {
        Long proId = data.getProId();
        String proName = data.getProName();
        Integer totalCount = data.getTotalCount();
        Double designTotalQuantity = data.getDesignTotalQuantity();
        Integer finishCount = data.getFinishCount();
        Double finishQuantity = data.getFinishQuantity();
        Double totalQuantity = data.getTotalQuantity();
        Integer applyingCount = data.getApplyingCount();
        String finishRatio = data.getFinishRatio();
        Double jcQuantity = data.getJcQuantity();
        String jcRatio = data.getJcRatio();
        Double finishDesign = data.getFinishDesign();
        Double finishUse = data.getFinishUse();
        // List<ProPourProgressListDetailsResponse> engineeringCategoryList = data.getEngineeringCategoryList();
        String sql = "INSERT INTO t_pro_four_progress(proId,proName,queryType,totalCount,designTotalQuantity,finishCount,finishQuantity,totalQuantity,applyingCount,finishRatio,jcQuantity,jcRatio,finishDesign,finishUse)"
            + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, new String[] {
            proId + "", proName, queryType, totalCount + "", designTotalQuantity + "", finishCount + "",
            finishQuantity + "", totalQuantity + "", applyingCount + "", finishRatio, jcQuantity + "", jcRatio,
            finishDesign + "", finishUse + ""
        });

    }

    private void saveMaterialProQuantityList(MaterialProQuantityListResponse data, String queryType) {
        Double designTotalQuantity = data.getDesignTotalQuantity();
        Double useTotalQuantity = data.getUseTotalQuantity();
        Double jcTotalQuantity = data.getJcTotalQuantity();
        Double jcTotalRatio = data.getJcTotalRatio();
        List<CompanyMaterialJcProQuantityResponse> materialJcProQuantityList = data.getMaterialJcProQuantityList();
        if (!CollectionUtils.isEmpty(materialJcProQuantityList)) {
            for (CompanyMaterialJcProQuantityResponse companyMaterialJcProQuantityResponse : materialJcProQuantityList) {
                String proId = companyMaterialJcProQuantityResponse.getProId();
                String proName = companyMaterialJcProQuantityResponse.getProName();
                Double designProTotalQuantity = companyMaterialJcProQuantityResponse.getDesignProTotalQuantity();
                Double useProTotalQuantity = companyMaterialJcProQuantityResponse.getUseProTotalQuantity();
                Double jcProTotalQuantity = companyMaterialJcProQuantityResponse.getJcProTotalQuantity();
                Double jcProTotalRatio = companyMaterialJcProQuantityResponse.getJcProTotalRatio();
                String sql = "INSERT INTO t_material_jc_pro_quantity(queryType, proId,proName,designProTotalQuantity,useProTotalQuantity,jcProTotalQuantity,jcProTotalRatio) VALUES (?, ?, ?, ?, ?, ?, ?)";
                jdbcTemplate.update(sql, new String[] {
                    queryType, proId + "", proName, designProTotalQuantity + "", useProTotalQuantity + "",
                    jcProTotalQuantity + "", jcProTotalRatio + ""
                });

                List<CompanyMaterialJcMaterialTypeQuantityResponse> materialJcProOfMaterialTypeQuantityList = companyMaterialJcProQuantityResponse
                    .getMaterialJcProOfMaterialTypeQuantityList();
                for (CompanyMaterialJcMaterialTypeQuantityResponse quantityResponse : materialJcProOfMaterialTypeQuantityList) {
                    String rawMaterialId = quantityResponse.getRawMaterialId();
                    String rawMaterialName = quantityResponse.getRawMaterialName();
                    Double designMaterialTypeQuantity = quantityResponse.getDesignMaterialTypeQuantity();
                    Double useMaterialTypeQuantity = quantityResponse.getUseMaterialTypeQuantity();
                    Double jcMaterialTypeQuantity = quantityResponse.getJcMaterialTypeQuantity();
                    Double jcMaterialTypeRatio = quantityResponse.getJcMaterialTypeRatio();
                    // List<CompanyMaterialJcGlobalMaterialQuantityResponse> globalMaterialQuantityList =
                    // quantityResponse.getGlobalMaterialQuantityList();
                    // for (CompanyMaterialJcGlobalMaterialQuantityResponse globalMaterialQuantityResponse :
                    // globalMaterialQuantityList) {
                    //
                    // }
                    sql = "INSERT INTO t_material_jc_pro_quantity_detail(queryType,proId,proName,rawMaterialId,rawMaterialName,designMaterialTypeQuantity,useMaterialTypeQuantity,jcMaterialTypeQuantity,jcMaterialTypeRatio) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    jdbcTemplate.update(sql, new String[] {
                        queryType, proId + "", proName, rawMaterialId, rawMaterialName, designMaterialTypeQuantity + "",
                        useMaterialTypeQuantity + "", jcMaterialTypeQuantity + "", jcMaterialTypeRatio + ""
                    });
                }
            }
        }

    }

    private void saveTemporaryConcreteList(TemporaryConcreteListResponse data) {
        // Double useQuantityTotal = data.getUseQuantityTotal();
        // Double totalQuantity = data.getTotalQuantity();
        List<TemporaryConcreteProResponse> proTemporaryConcreteList = data.getProTemporaryConcreteList();
        if (!CollectionUtils.isEmpty(proTemporaryConcreteList)) {
            for (TemporaryConcreteProResponse temporaryConcreteProResponse : proTemporaryConcreteList) {
                Double useQuantityTotal = temporaryConcreteProResponse.getUseQuantityTotal();
                Double totalQuantity = temporaryConcreteProResponse.getTotalQuantity();
                Long proId = temporaryConcreteProResponse.getProId();
                String proName = temporaryConcreteProResponse.getProName();
                String sql = "INSERT INTO t_pro_temporary_concrete(proId,proName,useQuantityTotal,totalQuantity) VALUES (?, ?, ?, ?)";
                jdbcTemplate.update(sql, new String[] {
                    proId + "", proName, useQuantityTotal + "", totalQuantity + ""
                });
                List<TemporaryConcreteProListResponse> temporaryConcreteProDetailsList = temporaryConcreteProResponse
                    .getTemporaryConcreteProDetailsList();
                for (TemporaryConcreteProListResponse proListResponse : temporaryConcreteProDetailsList) {
                    String tempName = proListResponse.getTempName();
                    Double useQuantity = proListResponse.getUseQuantity();
                    Double proTotalQuantity = proListResponse.getTotalQuantity();
                    sql = "INSERT INTO t_pro_temporary_concrete_detail(proId,proName,tempName,useQuantity,totalQuantity) VALUES (?, ?, ?, ?, ?)";
                    jdbcTemplate.update(sql, new String[] {
                        proId + "", proName, tempName, useQuantity + "", proTotalQuantity + ""
                    });
                }
            }
        }
    }

    private void callOrgLineStatisticsRequest(LangdaoApiClient client) {
        OrgLineStatisticsRequest request = new OrgLineStatisticsRequest();
        try {
            String sql = "delete from t_production_quantity where 1 = 1";
            jdbcTemplate.update(sql);
            List<String> strings = Arrays.asList("0", "1", "2", "3", "5"); // 查询范围 0-开累；1-当年；2-当月；3-当日； 4-自定义;5-当季
            for (String queryType : strings) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("companyId", "241649062955323392");
                jsonObject.put("queryType", queryType);
                request.setBizContent(JSON.toJSONString(jsonObject));
                OrgLineStatisticsResponse response = client.execute(request);
                OrgLineStatisticsListResponse data = response.getData();
                if (Objects.nonNull(data)) {
                    saveOrgLineStatisticsList(data, queryType);
                }
                if (response.isSuccess()) {
                    log.info("***Success***");

                }
                else {
                    log.info("**error**");
                    log.info(response.getCode());
                    log.info(response.getMsg());
                    log.info(response.getSubCode());
                    log.info(response.getSubMsg());
                }
            }
        }
        catch (LangdaoOpenApiException e) {
            e.printStackTrace();
        }
    }

    private void saveOrgLineStatisticsList(OrgLineStatisticsListResponse data, String queryType) {
        Double orgProductionQuantityTotal = data.getProductionQuantityTotal();
        // 公司各个混凝土标号明细集合
        List<OrgConcreteResponse> orgConcreteList = data.getOrgConcreteList();
        for (OrgConcreteResponse orgConcreteResponse : orgConcreteList) {
            Long globalConcreteId = orgConcreteResponse.getGlobalConcreteId();
            String globalConcreteName = orgConcreteResponse.getGlobalConcreteName();
            Double productionQuantity = orgConcreteResponse.getProductionQuantity();
            String unit = orgConcreteResponse.getUnit();
            String sql = "INSERT INTO t_org_concrete(productionQuantityTotal,globalConcreteId,globalConcreteName,productionQuantity,unit,queryType) VALUES (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, new String[] {
                orgProductionQuantityTotal + "", globalConcreteId + "", globalConcreteName, productionQuantity + "",
                unit, queryType
            });
        }
        // 各个项目混凝土生产总量集合
        List<ProLineStatisticToOrgResponse> proLineStatisticList = data.getProLineStatisticList();
        if (!CollectionUtils.isEmpty(proLineStatisticList)) {
            for (ProLineStatisticToOrgResponse response : proLineStatisticList) {
                Double productionQuantityTotal = response.getProductionQuantityTotal();
                List<ProConcreteToOrgResponse> proConcreteList = response.getProConcreteList();
                for (ProConcreteToOrgResponse proConcreteToOrgResponse : proConcreteList) {
                    Long proId = proConcreteToOrgResponse.getProId();
                    Long concreteId = proConcreteToOrgResponse.getConcreteId();
                    String concreteName = proConcreteToOrgResponse.getConcreteName();
                    // Long globalConcreteId = proConcreteToOrgResponse.getGlobalConcreteId();
                    // String globalConcreteName = proConcreteToOrgResponse.getGlobalConcreteName();
                    Double productionQuantity = proConcreteToOrgResponse.getProductionQuantity();
                    String unit = proConcreteToOrgResponse.getUnit();
                    String sql = "INSERT INTO t_pro_concrete(proId,productionQuantityTotal,concreteId,concreteName,productionQuantity,unit,queryType) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    jdbcTemplate.update(sql, new String[] {
                        proId + "", productionQuantityTotal + "", concreteId + "", concreteName,
                        productionQuantity + "", unit, queryType
                    });
                }
            }
        }
    }

    private void callSumMaterialsRequest(LangdaoApiClient client) {
        SumMaterialsRequest request = new SumMaterialsRequest();
        try {
            String sql = "delete from t_sum_materials where 1 = 1";
            jdbcTemplate.update(sql);
            List<String> strings = Arrays.asList("0", "1", "2", "3", "5"); // 查询范围 0-开累；1-当年；2-当月；3-当日； 4-自定义;5-当季
            for (String queryType : strings) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("companyId", "241649062955323392");
                jsonObject.put("queryType", queryType);
                request.setBizContent(JSON.toJSONString(jsonObject));
                SumMaterialsResponse response = client.execute(request);
                List<CompanyMaterialGatherStatisticForeignResponse> data = response.getData();
                if (Objects.nonNull(data)) {
                    saveSumMaterialsList(data, queryType);
                }
                if (response.isSuccess()) {
                    log.info("***Success***");

                }
                else {
                    log.info("**error**");
                    log.info(response.getCode());
                    log.info(response.getMsg());
                    log.info(response.getSubCode());
                    log.info(response.getSubMsg());
                }
            }
        }
        catch (LangdaoOpenApiException e) {
            e.printStackTrace();
        }
    }

    private void saveSumMaterialsList(List<CompanyMaterialGatherStatisticForeignResponse> data, String queryType) {
        if (!CollectionUtils.isEmpty(data)) {
            for (CompanyMaterialGatherStatisticForeignResponse r : data) {
                Long materialId = r.getMaterialId();
                String materialName = r.getMaterialName();
                String model = r.getModel();
                String unit = r.getUnit();
                String proName = r.getProName();
                Long proId = r.getProId();
                BigDecimal realityConsume = r.getRealityConsume();
                BigDecimal putInSum = r.getPutInSum();
                BigDecimal putOutSum = r.getPutOutSum();
                BigDecimal newStock = r.getNewStock();
                BigDecimal theoryStock = r.getTheoryStock();
                BigDecimal inventoryAdjustment = r.getInventoryAdjustment();
                List<ProMaterialGatherStatisticForeignResponse> proMaterial = r.getProMaterial();
                for (ProMaterialGatherStatisticForeignResponse material : proMaterial) {
                    saveProMaterial(material, queryType);
                }
                String sql = "INSERT INTO t_sum_materials(queryType,materialId,materialName,model,unit,proName,proId,realityConsume,putInSum,putOutSum,newStock,theoryStock,inventoryAdjustment) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                jdbcTemplate.update(sql, new String[] {
                    queryType, materialId + "", materialName, model, unit, proName, proId + "", realityConsume + "",
                    putInSum + "", putOutSum + "", newStock + "", theoryStock + "", inventoryAdjustment + ""
                });
            }
        }
    }

    private void saveProMaterial(ProMaterialGatherStatisticForeignResponse material, String queryType) {
        Long materialId = material.getMaterialId();
        String materialName = material.getMaterialName();
        String model = material.getModel();
        String unit = material.getUnit();
        String proName = material.getProName();
        Long proId = material.getProId();
        BigDecimal realityConsume = material.getRealityConsume();
        BigDecimal putInSum = material.getPutInSum();
        BigDecimal putOutSum = material.getPutOutSum();
        BigDecimal newStock = material.getNewStock();
        BigDecimal theoryStock = material.getTheoryStock();
        BigDecimal inventoryAdjustment = material.getInventoryAdjustment();
        String sql = "INSERT INTO t_sum_materials_project(queryType,materialId,materialName,model,unit,proName,proId,realityConsume,putInSum,putOutSum,newStock,theoryStock,inventoryAdjustment) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, new String[] {
            queryType, materialId + "", materialName, model, unit, proName, proId + "", realityConsume + "",
            putInSum + "", putOutSum + "", newStock + "", theoryStock + "", inventoryAdjustment + ""
        });
    }

    private void callMaterialStockPageRequest(LangdaoApiClient client, ProListResponse proListResponse) {
        MaterialStockPageRequest request = new MaterialStockPageRequest();
        try {
            String id = proListResponse.getId();
            String proName = proListResponse.getProName();
            List<String> strings = Arrays.asList("0", "1", "2", "3", "5"); // 查询范围 0-开累；1-当年；2-当月；3-当日； 4-自定义;5-当季
            for (String queryType : strings) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("proId", id);
                jsonObject.put("queryType", queryType);
                request.setBizContent(JSON.toJSONString(jsonObject));
                MaterialStockPageResponse response = client.execute(request);
                List<MaterialStockPageListResponse> data = response.getData();
                if (Objects.nonNull(data)) {
                    saveMaterialStockPageList(data, queryType, id, proName);
                }
                if (response.isSuccess()) {
                    log.info("***Success***");

                }
                else {
                    log.info("**error**");
                    log.info(response.getCode());
                    log.info(response.getMsg());
                    log.info(response.getSubCode());
                    log.info(response.getSubMsg());
                }
            }
        }
        catch (LangdaoOpenApiException e) {
            e.printStackTrace();
        }
    }

    private void saveMaterialStockPageList(List<MaterialStockPageListResponse> data, String queryType, String proId,
        String proName) {
        if (!CollectionUtils.isEmpty(data)) {
            for (MaterialStockPageListResponse response : data) {
                String materialId = response.getMaterialId();
                String materialName = response.getMaterialName();
                String model = response.getModel();
                String unit = response.getUnit();
                BigDecimal realityStock = response.getRealityStock();
                BigDecimal inventoryAdjustment = response.getInventoryAdjustment();
                BigDecimal putInSum = response.getPutInSum();
                BigDecimal putOutSum = response.getPutOutSum();
                BigDecimal realityConsume = response.getRealityConsume();
                String sql = "INSERT INTO t_material_stock(proId,proName,queryType,materialId,materialName,model,unit,realityStock,realityConsume,putInSum,putOutSum,inventoryAdjustment) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                jdbcTemplate.update(sql, new String[] {
                    proId, proName, queryType, materialId + "", materialName, model, unit, realityStock + "",
                    realityConsume + "", putInSum + "", putOutSum + "", inventoryAdjustment + ""
                });
            }
        }
    }

    private void callProConcreteJcGradeQuantityRequest(LangdaoApiClient client, ProListResponse proListResponse) {
        ProConcreteJcGradeQuantityRequest request = new ProConcreteJcGradeQuantityRequest();
        try {
            String id = proListResponse.getId();
            String proName = proListResponse.getProName();
            List<String> strings = Arrays.asList("0", "1", "2", "3", "5"); // 查询范围 0-开累；1-当年；2-当月；3-当日； 4-自定义;5-当季
            for (String queryType : strings) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("proId", id);
                jsonObject.put("queryType", queryType);
                request.setBizContent(JSON.toJSONString(jsonObject));
                ProConcreteJcGradeQuantityResponse response = client.execute(request);
                SectionOverStatisticByConcreteResponse data = response.getData();
                if (Objects.nonNull(data)) {
                    saveProConcreteJcGradeQuantity(data, queryType, id, proName);
                }
                if (response.isSuccess()) {
                    log.info("***Success***");

                }
                else {
                    log.info("**error**");
                    log.info(response.getCode());
                    log.info(response.getMsg());
                    log.info(response.getSubCode());
                    log.info(response.getSubMsg());
                }
            }
        }
        catch (LangdaoOpenApiException e) {
            e.printStackTrace();
        }
    }

    private void saveProConcreteJcGradeQuantity(SectionOverStatisticByConcreteResponse data, String queryType,
        String proId, String proName) {
        Double sectionOverweightTotal = data.getSectionOverweightTotal();
        String sectionOverRateTotal = data.getSectionOverRateTotal();
        Double designQuantityTotal = data.getDesignQuantityTotal();
        Double useQuantityTotal = data.getUseQuantityTotal();
        String sql = "INSERT INTO t_pro_concrete_grade_quantity(proId,proName,queryType,sectionOverweightTotal,sectionOverRateTotal,designQuantityTotal,useQuantityTotal) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, new String[] {
            proId, proName, queryType, sectionOverweightTotal + "", sectionOverRateTotal, designQuantityTotal + "",
            useQuantityTotal + ""
        });
        List<SectionOverStatisticByGradeConcreteListResponse> list = data.getList();
        for (SectionOverStatisticByGradeConcreteListResponse response : list) {
            String concreteGradeId = response.getConcreteGradeId();
            String concreteGradeName = response.getConcreteGradeName();
            String sectionOverRate = response.getSectionOverRate();
            Double tempDesignQuantityTotal = response.getDesignQuantityTotal();
            Double tempUseQuantityTotal = response.getUseQuantityTotal();
            Double sectionOverweight = response.getSectionOverweight();
            // List<SectionOverStatisticByConcreteListResponse> list1 = response.getList();
            // for (SectionOverStatisticByConcreteListResponse listResponse : list1) {
            //
            // }
            sql = "INSERT INTO t_pro_concrete_grade_quantity_detail(proId,proName,queryType,concreteGradeId,concreteGradeName,sectionOverRate,designQuantityTotal,useQuantityTotal,sectionOverweight) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, new String[] {
                proId, proName, queryType, concreteGradeId, concreteGradeName, sectionOverRate,
                tempDesignQuantityTotal + "", tempUseQuantityTotal + "", sectionOverweight + ""
            });
        }
    }

    private void callOrgConcreteJcGradeQuantity(LangdaoApiClient client) {
        OrgConcreteJcGradeQuantityRequest request = new OrgConcreteJcGradeQuantityRequest();
        try {
            String sql = "delete from t_org_concrete_grade_quantity where 1 = 1";
            jdbcTemplate.update(sql);
            sql = "delete from t_org_concrete_grade_quantity_detail where 1 = 1";
            jdbcTemplate.update(sql);
            List<String> strings = Arrays.asList("0", "1", "2", "3", "5"); // 查询范围 0-开累；1-当年；2-当月；3-当日； 4-自定义;5-当季
            for (String queryType : strings) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("companyId", "241649062955323392");
                jsonObject.put("queryType", queryType);
                request.setBizContent(JSON.toJSONString(jsonObject));
                OrgConcreteJcGradeQuantityResponse response = client.execute(request);
                CompanyConcreteJcQuantityResponse data = response.getData();
                if (Objects.nonNull(data)) {
                    saveOrgConcreteJcGradeQuantity(data, queryType);
                }
                if (response.isSuccess()) {
                    log.info("***Success***");

                }
                else {
                    log.info("**error**");
                    log.info(response.getCode());
                    log.info(response.getMsg());
                    log.info(response.getSubCode());
                    log.info(response.getSubMsg());
                }
            }
        }
        catch (LangdaoOpenApiException e) {
            e.printStackTrace();
        }
    }

    private void saveOrgConcreteJcGradeQuantity(CompanyConcreteJcQuantityResponse data, String queryType) {
        Double designTotalQuantity = data.getDesignTotalQuantity();
        Double useTotalQuantity = data.getUseTotalQuantity();
        Double jcTotalQuantity = data.getJcTotalQuantity();
        Double jcTotalRatio = data.getJcTotalRatio();
        String sql = "INSERT INTO t_org_concrete_grade_quantity(queryType,designTotalQuantity,useTotalQuantity,jcTotalQuantity,jcTotalRatio) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, new String[] {
            queryType, designTotalQuantity + "", useTotalQuantity + "", jcTotalQuantity + "", jcTotalRatio + ""
        });
        List<CompanyConcreteJcGradeQuantityResponse> concreteJcGradeQuantityList = data
            .getConcreteJcGradeQuantityList();
        for (CompanyConcreteJcGradeQuantityResponse quantityResponse : concreteJcGradeQuantityList) {
            String concreteGradeId = quantityResponse.getConcreteGradeId();
            String concreteGradeName = quantityResponse.getConcreteGradeName();
            Double designGradeTotalQuantity = quantityResponse.getDesignGradeTotalQuantity();
            Double useGradeTotalQuantity = quantityResponse.getUseGradeTotalQuantity();
            Double jcGradeTotalQuantity = quantityResponse.getJcGradeTotalQuantity();
            Double jcGradeTotalRatio = quantityResponse.getJcGradeTotalRatio();
            // List<CompanyConcreteJcGlobalQuantityResponse> concreteJcGlobalQuantityList =
            // quantityResponse.getConcreteJcGlobalQuantityList();
            sql = "INSERT INTO t_org_concrete_grade_quantity_detail(queryType,concreteGradeId,concreteGradeName,designGradeTotalQuantity,useGradeTotalQuantity,jcGradeTotalQuantity,jcGradeTotalRatio) VALUES (?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, new String[] {
                queryType, concreteGradeId, concreteGradeName, designGradeTotalQuantity + "",
                useGradeTotalQuantity + "", jcGradeTotalQuantity + "", jcGradeTotalRatio + ""
            });
        }

    }

}
