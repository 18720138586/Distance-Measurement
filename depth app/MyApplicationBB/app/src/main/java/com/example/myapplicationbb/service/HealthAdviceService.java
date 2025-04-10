package com.example.myapplicationbb.service;


import java.util.ArrayList;
import java.util.List;

public class HealthAdviceService {

    /**
     * 根据年龄和体重生成健康建议
     * @param age 年龄（岁）
     * @param weight 体重（公斤）
     * @param height 身高（厘米）
     * @return 健康建议列表
     */
    public static List<String> generateHealthAdvice(int age, float weight, float height) {
        List<String> adviceList = new ArrayList<>();

        // 基础建议（适用于所有人群）
        adviceList.add("保持均衡饮食，多摄入蛋白质、维生素和矿物质");
        adviceList.add("每天保持充足的水分摄入，建议6-8杯水");
        adviceList.add("保持规律作息，确保充足睡眠");

        // 根据年龄段提供建议
        if (age < 12) {
            // 儿童
            adviceList.add("每天喝牛奶补充钙质，促进骨骼发育");
            adviceList.add("多吃富含DHA的食物，如鱼类，有助于大脑发育");
            adviceList.add("避免过多摄入糖分和加工食品");
        } else if (age < 18) {
            // 青少年
            adviceList.add("增加钙质摄入，如牛奶、豆制品等，促进骨骼发育");
            adviceList.add("适量增加蛋白质摄入，如瘦肉、鸡蛋等，促进肌肉发育");
            adviceList.add("控制高糖高脂食品摄入，避免肥胖");
        } else if (age < 40) {
            // 青年
            adviceList.add("保持适量运动，增强心肺功能");
            adviceList.add("注意膳食纤维摄入，保持肠道健康");
            adviceList.add("控制咖啡因摄入，避免影响睡眠质量");
        } else if (age < 60) {
            // 中年
            adviceList.add("增加抗氧化食物摄入，如深色蔬果");
            adviceList.add("控制盐分摄入，预防高血压");
            adviceList.add("适量补充钙质和维生素D，预防骨质疏松");
        } else {
            // 老年
            adviceList.add("增加钙质和维生素D摄入，预防骨质疏松");
            adviceList.add("适量补充蛋白质，防止肌肉流失");
            adviceList.add("多吃富含抗氧化物质的食物，如蓝莓、绿茶等");
        }

        // 根据体重指数(BMI)提供建议
        float bmi = weight / ((height/100) * (height/100));

        if (bmi < 18.5) {
            // 偏瘦
            adviceList.add("适当增加热量摄入，保持健康体重");
            adviceList.add("增加优质蛋白质摄入，如瘦肉、鸡蛋、奶制品等");
            adviceList.add("适量进行力量训练，增加肌肉量");
        } else if (bmi < 24) {
            // 正常体重
            adviceList.add("保持当前饮食习惯和运动量");
            adviceList.add("定期体检，关注身体健康状况");
        } else if (bmi < 28) {
            // 超重
            adviceList.add("控制每日热量摄入，减少高糖高脂食品");
            adviceList.add("增加有氧运动，如快走、游泳等");
            adviceList.add("增加膳食纤维摄入，增加饱腹感");
        } else {
            // 肥胖
            adviceList.add("严格控制热量摄入，制定科学的减重计划");
            adviceList.add("增加运动频率和强度，消耗多余热量");
            adviceList.add("建议咨询专业营养师或医生，获取个性化减重方案");
            adviceList.add("注意监测血压、血糖等指标，预防代谢综合征");
        }

        return adviceList;
    }
}
