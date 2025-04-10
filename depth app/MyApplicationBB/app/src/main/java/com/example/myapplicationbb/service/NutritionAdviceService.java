// HeightAdviceService.java
package com.example.myapplicationbb.service;

import com.example.myapplicationbb.model.HeightData;
import com.example.myapplicationbb.repository.HeightDataRepository;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class NutritionAdviceService {

    /**
     * 根据性别、年龄和身高生成成长建议
     * @param isMale 性别（true=男性，false=女性）
     * @param age 年龄标签（如"12岁"）
     * @param measuredHeight 当前身高（厘米）
     * @return 包含建议的JsonObject
     */
    public static JsonObject generateNutritionAdvice(String age,boolean isMale,  float measuredHeight) {
        JsonObject result = new JsonObject();
        List<String> generalAdvice = new ArrayList<>();
        List<String> specificAdvice = new ArrayList<>();

        // 获取数据源
        List<HeightData> data = isMale ?
                HeightDataRepository.getMaleData() :
                HeightDataRepository.getFemaleData();

        // 查找匹配数据
        HeightData targetData = findTargetData(data, age);

        if (targetData == null) {
            result.addProperty("error", "未找到[" + age + "]的参考数据");
            return result;
        }

        // 生成通用建议
        generateGeneralAdvice(measuredHeight, targetData, generalAdvice);

        // 生成专项建议，以后再进行扩充
        specificAdvice.addAll(getAgeSpecificAdvice(isMale, age));

        // 构建JSON结果
        buildJsonResult(result, generalAdvice, specificAdvice);

        return result;
    }

    // 数据匹配方法
    private static HeightData findTargetData(List<HeightData> data, String ageLabel) {
        for (HeightData item : data) {
            if (item.getAgeLabel().equals(ageLabel)) {
                return item;
            }
        }
        return null;
    }

    // 通用建议生成
    private static void generateGeneralAdvice(float height, HeightData data, List<String> advice) {
        if (height < data.getP3()) {
            advice.add("【身高处于P3以下（矮小）】需警惕生长迟缓，建议：");
            advice.add("→ 立即到儿童内分泌科进行系统检查（骨龄+生长激素筛查）");
            advice.add("→ 每日保证：1个鸡蛋+200ml牛奶+50g瘦肉优质蛋白组合");
            advice.add("→ 维生素D3补充800IU/日（需医生指导）");
            advice.add("→ 避免夜间灯光暴露，保证21点前入睡");
            advice.add("→ 每周3次跳绳/篮球等纵向运动（每次30分钟）");

        } else if (height < data.getP10()) {
            advice.add("【身高处于P3-P10（下等）】潜在生长不足风险，建议：");
            advice.add("→ 3个月监测生长速度（年增长＜5cm需就医）");
            advice.add("→ 营养强化：早餐添加奶酪20g+坚果15g");
            advice.add("→ 维生素D3 400-600IU/日（冬季加倍）");
            advice.add("→ 运动方案：每天跳绳500次（分组完成）");
            advice.add("→ 避免糖分过量（每日添加糖＜25g）");

        } else if (height < data.getP25()) {
            advice.add("【身高处于P10-P25（中下等）】提升空间建议：");
            advice.add("→ 早晚各250ml牛奶（选择A2β酪蛋白型更易吸收）");
            advice.add("→ 补充策略：维生素K2（MK-7型）100μg/日");
            advice.add("→ 黄金运动组合：晨起摸高50次+晚间跳绳");
            advice.add("→ 睡眠深度保障：22点前入睡，避免夜醒");
            advice.add("→ 每周2次深海鱼（三文鱼/鳕鱼补充DHA）");

        } else if (height < data.getP75()) {
            advice.add("【身高处于P25-P75（正常中等）】优化建议：");
            advice.add("→ 蛋白质彩虹饮食：红肉/白肉/豆制品轮换");
            advice.add("→ 睡眠周期管理：保证4-5个完整睡眠周期（约9小时）");
            advice.add("→ 运动多样性：每周3次游泳+2次篮球");
            advice.add("→ 避免生长抑制因素：体重过快增长、精神压力");
            advice.add("→ 每季度进行生长曲线绘图追踪趋势");

        } else if (height < data.getP90()) {
            advice.add("【身高处于P75-P90（中偏上）】注意事项：");
            advice.add("→ 钙质科学补充：柠檬酸钙1000mg/日（分次服用）");
            advice.add("→ 禁忌：避免过早力量训练（卧推/深蹲等负重运动）");
            advice.add("→ 优选运动：瑜伽拉伸、引体向上（不负重）");
            advice.add("→ 营养平衡：控制动物蛋白过量（每日≤2g/kg体重）");
            advice.add("→ 每年骨龄检测（重点监测骨骺线变化）");

        } else if (height < data.getP97()) {
            advice.add("【身高处于P90-P97（中上等）】管理建议：");
            advice.add("→ 骨龄监测频率：每6个月1次（关注骨龄/年龄比）");
            advice.add("→ 运动处方：每天10分钟悬垂拉伸+蛙泳训练");
            advice.add("→ 营养调控：锌元素补充15mg/日（促进生长激素合成）");
            advice.add("→ 异常信号预警：年增长＞8cm需排查性早熟");
            advice.add("→ 建立生长档案：记录每月晨起身高（精确到0.1cm）");

        } else {
            advice.add("【身高处于P97以上（上等）】医学关注重点：");
            advice.add("→ 必查项目：父母遗传靶身高比对+垂体MRI排查");
            advice.add("→ 钙补充上限：每日≤1200mg（防止骨骺过早闭合）");
            advice.add("→ 运动禁忌：避免剧烈碰撞运动（预防生长板损伤）");
            advice.add("→ 内分泌评估：年增长＞10cm需排查巨人症");
            advice.add("→ 发育协调性：同步关注心肺功能发育状况");
        }

        // 通用建议（所有百分位都适用）
        advice.add("★ 通用建议：建立规律生物钟（误差＜30分钟）");
        advice.add("★ 测量规范：每月固定时间测量（建议晨起排便后）");
        advice.add("★ 心理支持：避免过度关注身高造成心理压力");
    }
    // 专项建议生成
    private static List<String> getAgeSpecificAdvice(boolean isMale, String ageLabel) {
        List<String> advice = new ArrayList<>();
        String gender = isMale ? "男孩" : "女孩";

        try {
            if (ageLabel.contains("月")) {
                advice.add(gender + "婴儿期建议");
                advice.add("• 每日被动操20分钟");
                advice.add("• 维生素AD补充（伊可新）");
            } else if (isPreschooler(ageLabel)) {
                advice.add(gender + "学龄前建议");
                advice.add("• 每日户外活动2小时");
                advice.add("• DHA补充100mg/日");
            } else if (isTeenager(ageLabel)) {
                advice.add(gender + "青春期建议");
                advice.add("• 钙摄入1200mg/日");
                advice.add("• 纵向运动30分钟/天");
                advice.add("• 22:00前入睡");
            }
        } catch (NumberFormatException e) {
            advice.add("年龄格式异常");
        }

        return advice;
    }

    // 学龄前判断（3-6岁）
    private static boolean isPreschooler(String ageLabel) {
        if (ageLabel.contains("岁")) {
            int age = Integer.parseInt(ageLabel.replace("岁", ""));
            return age >= 3 && age < 6;
        }
        return false;
    }

    // 青春期判断（12-18岁）
    private static boolean isTeenager(String ageLabel) {
        if (ageLabel.contains("岁")) {
            int age = Integer.parseInt(ageLabel.replace("岁", ""));
            return age >= 12 && age <= 18;
        }
        return false;
    }

    // 构建JSON结果
    private static void buildJsonResult(JsonObject obj, List<String> general, List<String> specific) {
        obj.addProperty("generalAdvice", formatAdviceList(general));
        obj.addProperty("specificAdvice", formatAdviceList(specific));
    }

    // 格式化建议列表
    private static String formatAdviceList(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String item : list) {
            sb.append("• ").append(item).append("\n");
        }
        return sb.toString();
    }


}