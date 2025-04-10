package com.example.myapplicationbb;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplicationbb.data.MeasurementDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.text.DecimalFormat;

import com.example.myapplicationbb.service.NutritionAdviceService;

public class MeasurementResultFragment extends Fragment {
    private TextView heightValue;
    private TextView generalAdviceContent;
    private TextView specificAdviceContent;
    private Button saveResultButton;
    private float measuredHeight;
    private JsonObject nutritionAdvice;
    private String age;
    public boolean isMale;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_measurement_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        heightValue = view.findViewById(R.id.height_value);
        generalAdviceContent = view.findViewById(R.id.general_advice_content);
        specificAdviceContent = view.findViewById(R.id.specific_advice_content);
        saveResultButton = view.findViewById(R.id.save_result_button);

        // 获取传递的测量结果数据
        // 获取传递的测量结果数据
        Bundle args = getArguments();
        if (args != null) {
            // 获取年龄和性别参数
            if (args.containsKey("age")) {
                age = String.valueOf(args.getInt("age")); // 直接获取 String 类型
            }

            if (args.containsKey("isMale")) {
                isMale = args.getBoolean("isMale");
            }

            String resultJson = args.getString("measurement_result");
            if (resultJson != null) {
                // 解析 JSON 字符串为 JsonObject
                JsonObject result = new Gson().fromJson(resultJson, JsonObject.class);

                // 将 age 和 isMale 添加到 result 中

                result.addProperty("age", age); // 直接添加 String 类型的 age

                result.addProperty("isMale", isMale); // 布尔值直接添加

                // 现在 result 包含 height、age 和 isMale
                displayResult(result);
            }
        }

        saveResultButton.setOnClickListener(v -> saveResult());
    }

    private void displayResult(JsonObject result) {
        if (result.has("height")) {
            measuredHeight = result.get("height").getAsFloat();

//            // 获取年龄和性别参数
//            if (result.has("age")) {
//                age = result.get("age").getAsString();
//            }
//
//            if (result.has("isMale")) {
//                isMale= result.get("isMale").getAsBoolean();
//            }
            age = result.get("age").getAsString();
            isMale= result.get("isMale").getAsBoolean();

            // 显示身高
            DecimalFormat df = new DecimalFormat("#.0");
            heightValue.setText(df.format(measuredHeight) + " cm");

            // 使用NutritionAdviceService生成饮食建议，基于身高、年龄和体重
            nutritionAdvice = NutritionAdviceService.generateNutritionAdvice(age, isMale, measuredHeight);

            // 显示饮食建议
            if (nutritionAdvice.has("generalAdvice")) {
                generalAdviceContent.setText(nutritionAdvice.get("generalAdvice").getAsString());
            }
        } else {
            generalAdviceContent.setText("暂无通用建议");
            if (nutritionAdvice.has("specificAdvice")) {
                specificAdviceContent.setText(nutritionAdvice.get("specificAdvice").getAsString());


            }
        }
    }


    private void displayResult2(JsonObject result) {
        if (result.has("height") && result.has("nutrition_advice")) {
            measuredHeight = result.get("height").getAsFloat();
            nutritionAdvice = result.getAsJsonObject("nutrition_advice");

            // 显示身高
            DecimalFormat df = new DecimalFormat("#.0");
            heightValue.setText(df.format(measuredHeight) + " cm");

            // 显示一般建议
            StringBuilder generalAdvice = new StringBuilder();
            for (JsonElement advice : nutritionAdvice.getAsJsonArray("general").asList()) {
                generalAdvice.append("• ").append(advice).append("\n");
            }
            generalAdviceContent.setText(generalAdvice.toString());

            // 显示个性化建议
            StringBuilder specificAdvice = new StringBuilder();
            for (JsonElement advice : nutritionAdvice.getAsJsonArray("specific").asList()) {
                specificAdvice.append("• ").append(advice).append("\n");
            }
            specificAdviceContent.setText(specificAdvice.toString());
        }
    }

    private void saveResult() {
        // 保存测量结果到本地数据库
        if (measuredHeight > 0) {
            try {
                // 获取数据库实例并保存记录
                boolean success = MeasurementDatabase
                        .getInstance(requireContext())
                        .saveMeasurement(measuredHeight);

                if (success) {
                    Toast.makeText(requireContext(), "测量结果已成功保存", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "保存失败，请重试", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(requireContext(), "保存过程中出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(requireContext(), "无效的测量结果，无法保存", Toast.LENGTH_SHORT).show();
        }
    }
}