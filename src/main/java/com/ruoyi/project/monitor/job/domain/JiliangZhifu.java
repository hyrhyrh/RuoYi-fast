package com.ruoyi.project.monitor.job.domain;

import com.ruoyi.framework.web.domain.BaseEntity;

import java.io.Serializable;

/**
 * @Author hyr
 * @Description
 * @Date create in 2023/5/12 10:00
 */
public class JiliangZhifu  extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    private String num;

    private String amount;

    private String contractPrice;

    private String yueFen;

    private double endDealTp;

    private double sfTp;

    private double endSfTp;

    private double dealTp;

    private double changeTp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getContractPrice() {
        return contractPrice;
    }

    public void setContractPrice(String contractPrice) {
        this.contractPrice = contractPrice;
    }

    public String getYueFen() {
        return yueFen;
    }

    public void setYueFen(String yueFen) {
        this.yueFen = yueFen;
    }

    public double getEndDealTp() {
        return endDealTp;
    }

    public void setEndDealTp(double endDealTp) {
        this.endDealTp = endDealTp;
    }

    public double getSfTp() {
        return sfTp;
    }

    public void setSfTp(double sfTp) {
        this.sfTp = sfTp;
    }

    public double getEndSfTp() {
        return endSfTp;
    }

    public void setEndSfTp(double endSfTp) {
        this.endSfTp = endSfTp;
    }

    public double getDealTp() {
        return dealTp;
    }

    public void setDealTp(double dealTp) {
        this.dealTp = dealTp;
    }

    public double getChangeTp() {
        return changeTp;
    }

    public void setChangeTp(double changeTp) {
        this.changeTp = changeTp;
    }

    @Override
    public String toString() {
        return "JiliangZhifu{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", num='" + num + '\'' +
                ", amount='" + amount + '\'' +
                ", contractPrice='" + contractPrice + '\'' +
                ", yueFen='" + yueFen + '\'' +
                ", endDealTp=" + endDealTp +
                ", sfTp=" + sfTp +
                ", endSfTp=" + endSfTp +
                ", dealTp=" + dealTp +
                ", changeTp=" + changeTp +
                '}';
    }
}
