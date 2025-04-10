package com.example.myapplicationbb.repository;

import com.example.myapplicationbb.model.HeightData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HeightDataRepository {
    // 使用不可变列表保证线程安全
    private static final List<HeightData> MALE_DATA;
    private static final List<HeightData> FEMALE_DATA;

    static {
        // 初始化男性数据（完整版）
        List<HeightData> maleTemp = new ArrayList<>();
        maleTemp.add(new HeightData("出生", 47.1f, 48.1f, 49.2f, 50.4f, 51.6f, 54f, 55.8f));
        maleTemp.add(new HeightData("2月", 54.6f, 55.09f, 57.2f, 58.7f, 60.3f, 63.3f, 65.7f));
        maleTemp.add(new HeightData("4月", 60.3f, 61.7f, 63f, 64.6f, 66.2f, 69.3f, 71.7f));
        maleTemp.add(new HeightData("6月", 64f, 65.4f, 66.8f, 68.4f, 70f, 73.3f, 75.8f));
        maleTemp.add(new HeightData("9月", 67.9f, 69.4f, 70.9f, 72.6f, 74.4f, 77.8f, 80.5f));
        maleTemp.add(new HeightData("12月", 71.5f, 73.1f, 74.7f, 76.5f, 78.4f, 82.1f, 85f));
        maleTemp.add(new HeightData("15月", 74.4f, 76.1f, 77.8f, 79.8f, 81.8f, 85.8f, 88.9f));
        maleTemp.add(new HeightData("18月", 76.9f, 78.7f, 80.6f, 82.7f, 84.8f, 89.1f, 92.4f));
        maleTemp.add(new HeightData("21月", 79.5f, 81.4f, 83.4f, 85.6f, 87.9f, 92.4f, 95.9f));
        maleTemp.add(new HeightData("2", 82.1f, 84.1f, 86.2f, 88.5f, 90.9f, 95.8f, 99.5f));
        maleTemp.add(new HeightData("2.5", 86.4f, 88.6f, 90.8f, 93.3f, 95.9f, 101f, 105f));
        maleTemp.add(new HeightData("3", 89.7f, 91.9f, 94.2f, 96.8f, 99.4f, 104.6f, 108.7f));
        maleTemp.add(new HeightData("3.5", 93.4f, 95.7f, 98f, 100.6f, 103.2f, 108.6f, 112.7f));
        maleTemp.add(new HeightData("4", 96.7f, 99.1f, 101.4f, 104.1f, 106.9f, 112.3f, 116.5f));
        maleTemp.add(new HeightData("4.5", 100f, 102.4f, 104.9f, 107.7f, 110.5f, 116.2f, 120.6f));
        maleTemp.add(new HeightData("5", 103.3f, 105.8f, 108.4f, 111.3f, 114.2f, 120.1f, 124.7f));
        maleTemp.add(new HeightData("5.5", 106.4f, 109f, 111.7f, 114.7f, 117.7f, 123.8f, 128.6f));
        maleTemp.add(new HeightData("6", 109.1f, 111.8f, 114.6f, 117.7f, 120.9f, 127.2f, 132.1f));
        maleTemp.add(new HeightData("6.5", 111.7f, 114.5f, 117.4f, 120.7f, 123.9f, 130.5f, 135.6f));
        maleTemp.add(new HeightData("7", 114.6f, 117.6f, 120.6f, 124f, 127.4f, 134.3f, 139.6f));
        maleTemp.add(new HeightData("7.5", 117.4f, 120.5f, 123.6f, 127.1f, 130.7f, 137.8f, 143.4f));
        maleTemp.add(new HeightData("8", 119.9f, 123.1f, 126.3f, 130f, 133.7f, 141.1f, 146.8f));
        maleTemp.add(new HeightData("8.5", 122.3f, 125.6f, 129f, 132.7f, 136.6f, 144.2f, 150.1f));
        maleTemp.add(new HeightData("9", 124.6f, 128f, 131.4f, 135.4f, 139.3f, 147.2f, 153.3f));
        maleTemp.add(new HeightData("9.5", 126.7f, 130.3f, 133.9f, 137.9f, 142f, 150.1f, 156.4f));
        maleTemp.add(new HeightData("10", 128.7f, 132.3f, 136f, 140.2f, 144.4f, 152.7f, 159.2f));
        maleTemp.add(new HeightData("10.5", 130.7f, 134.5f, 138.3f, 142.6f, 147f, 155.7f, 162.3f));
        maleTemp.add(new HeightData("11", 132.9f, 136.8f, 140.8f, 145.3f, 149.9f, 158.9f, 165.8f));
        maleTemp.add(new HeightData("11.5", 135.3f, 139.5f, 143.7f, 148.4f, 153.1f, 162.6f, 169.8f));
        maleTemp.add(new HeightData("12", 138.1f, 142.5f, 147f, 151.9f, 157f, 166.9f, 174.5f));
        maleTemp.add(new HeightData("12.5", 141.1f, 145.7f, 150.4f, 155.6f, 160.8f, 171.1f, 178.9f));
        maleTemp.add(new HeightData("13", 145f, 149.6f, 154.3f, 159.5f, 164.8f, 175.1f, 183f));
        maleTemp.add(new HeightData("13.5", 148.8f, 153.3f, 157.9f, 163.1f, 168.1f, 178.1f, 185.7f));
        maleTemp.add(new HeightData("14", 152.3f, 156.7f, 161f, 165.9f, 170f, 180.2f, 187.4f));
        maleTemp.add(new HeightData("14.5", 155.3f, 159.4f, 163.6f, 168.2f, 172.8f, 181.8f, 188.5f));
        maleTemp.add(new HeightData("15", 157.5f, 161.4f, 165.4f, 169.8f, 174.2f, 182.8f, 189.3f));
        maleTemp.add(new HeightData("15.5", 159.1f, 162.9f, 166.7f, 171f, 175.2f, 183.6f, 189.8f));
        maleTemp.add(new HeightData("16", 159.9f, 163.6f, 167.4f, 171.6f, 175.8f, 184f, 190.1f));
        maleTemp.add(new HeightData("16.5", 160.5f, 164.2f, 167.9f, 172.1f, 176.2f, 184.3f, 190.3f));
        maleTemp.add(new HeightData("17", 160.9f, 164.5f, 168.2f, 172.3f, 176.4f, 184.5f, 190.5f));
        maleTemp.add(new HeightData("18", 161.3f, 164.9f, 168.6f, 172.7f, 176.7f, 184.7f, 190.6f));
        MALE_DATA = Collections.unmodifiableList(maleTemp);

        // 女性数据（完整表格数据）
        List<HeightData> femaleTemp = new ArrayList<>();
        femaleTemp.add(new HeightData("出生", 46.6f, 47.5f, 48.6f, 49.7f, 50.9f, 51.9f, 53f));
        femaleTemp.add(new HeightData("2月", 53.4f, 54.7f, 56f, 57.4f, 58.9f, 60.2f, 61.6f));
        femaleTemp.add(new HeightData("4月", 59.01f, 60.3f, 61.7f, 63.1f, 64.6f, 66f, 67.4f));
        femaleTemp.add(new HeightData("6月", 62.5f, 63.9f, 65.2f, 66.8f, 68.4f, 69.8f, 71.2f));
        femaleTemp.add(new HeightData("9月", 66.4f, 67.8f, 69.3f, 71f, 72.8f, 74.3f, 75.9f));
        femaleTemp.add(new HeightData("12月", 67f, 71.6f, 73.2f, 75f, 76.8f, 78.5f, 80.2f));
        femaleTemp.add(new HeightData("15月", 73.2f, 74.9f, 76.6f, 78.5f, 80.4f, 82.2f, 84f));
        femaleTemp.add(new HeightData("18月", 76f, 77.7f, 79.5f, 81.5f, 83.6f, 85.5f, 87.4f));
        femaleTemp.add(new HeightData("21月", 78.5f, 80.4f, 82.3f, 84.4f, 86.6f, 88.6f, 90.7f));
        femaleTemp.add(new HeightData("2", 80.9f, 82.9f, 84.9f, 87.2f, 89.6f, 91.7f, 93.9f));
        femaleTemp.add(new HeightData("2.5", 85.2f, 87.4f, 89.6f, 92.1f, 94.6f, 97f, 99.3f));
        femaleTemp.add(new HeightData("3", 88.6f, 90.8f, 93.1f, 95.6f, 98.2f, 100.5f, 102.9f));
        femaleTemp.add(new HeightData("3.5", 92.4f, 94.6f, 96.8f, 99.4f, 102f, 104.4f, 106.8f));
        femaleTemp.add(new HeightData("4", 95.8f, 98.1f, 100.4f, 103.1f, 105.7f, 108.2f, 110.6f));
        femaleTemp.add(new HeightData("4.5", 99.2f, 101.5f, 104f, 106.7f, 109.5f, 112.1f, 114.7f));
        femaleTemp.add(new HeightData("5", 102.3f, 104.8f, 107.3f, 110.2f, 113.1f, 115.7f, 118.4f));
        femaleTemp.add(new HeightData("5.5", 105.4f, 108f, 110.6f, 113.5f, 118.5f, 119.3f, 122f));
        femaleTemp.add(new HeightData("6", 108.1f, 110.8f, 113.5f, 116.6f, 119.7f, 122.5f, 125.4f));
        femaleTemp.add(new HeightData("6.5", 110.6f, 113.4f, 116.2f, 119.4f, 122.7f, 125.6f, 128.6f));
        femaleTemp.add(new HeightData("7", 113.3f, 116.2f, 119.2f, 122.5f, 125.9f, 129f, 132f));
        femaleTemp.add(new HeightData("7.5", 116f, 119f, 122.1f, 125.6f, 129.1f, 132.3f, 135.5f));
        femaleTemp.add(new HeightData("8", 118.5f, 121.6f, 124.9f, 128.5f, 132.1f, 135.4f, 138.7f));
        femaleTemp.add(new HeightData("8.5", 121f, 124.2f, 127.6f, 131.3f, 135.1f, 138.5f, 141.9f));
        femaleTemp.add(new HeightData("9", 123.3f, 126.7f, 130.2f, 134.1f, 138f, 141.6f, 145f));
        femaleTemp.add(new HeightData("9.5", 125.7f, 129.3f, 132.9f, 137f, 141.1f, 144.8f, 148.5f));
        femaleTemp.add(new HeightData("10", 128.3f, 132.1f, 135.9f, 140.1f, 144.4f, 148.2f, 152f));
        femaleTemp.add(new HeightData("10.5", 131.1f, 135f, 138.9f, 143.3f, 147.7f, 151.6f, 155.6f));
        femaleTemp.add(new HeightData("11", 134.2f, 138.2f, 142.2f, 146.6f, 151.1f, 155.2f, 159.2f));
        femaleTemp.add(new HeightData("11.5", 137.2f, 141.2f, 145.2f, 149.7f, 154.1f, 158.2f, 162.1f));
        femaleTemp.add(new HeightData("12", 140.2f, 144.1f, 148f, 152.4f, 156.7f, 160.7f, 164.5f));
        femaleTemp.add(new HeightData("12.5", 142.9f, 146.6f, 150.4f, 154.6f, 158.8f, 162.6f, 166.3f));
        femaleTemp.add(new HeightData("13", 145f, 148.6f, 152.2f, 156.3f, 160.3f, 164f, 167.6f));
        femaleTemp.add(new HeightData("13.5", 146.7f, 150.2f, 153.7f, 157.6f, 161.6f, 165f, 168.6f));
        femaleTemp.add(new HeightData("14", 147.9f, 151.3f, 154.8f, 158.6f, 162.4f, 165.9f, 169.3f));
        femaleTemp.add(new HeightData("14.5", 148.9f, 152.2f, 155.6f, 159.4f, 163.1f, 166.5f, 169.8f));
        femaleTemp.add(new HeightData("15", 149.5f, 152.8f, 156.1f, 159.8f, 163.5f, 166.8f, 170.1f));
        femaleTemp.add(new HeightData("15.5", 149.9f, 153.1f, 156.5f, 160.1f, 163.8f, 167.1f, 170.3f));
        femaleTemp.add(new HeightData("16", 149.8f, 153.1f, 156.4f, 160.1f, 163.8f, 167.1f, 170.3f));
        femaleTemp.add(new HeightData("16.5", 149.9f, 153.2f, 156.5f, 160.2f, 163.8f, 167.1f, 170.4f));
        femaleTemp.add(new HeightData("17", 150.1f, 153.4f, 156.7f, 160.3f, 164f, 167.3f, 170.5f));
        femaleTemp.add(new HeightData("18", 150.4f, 153.7f, 157f, 160.6f, 164.2f, 167.5f, 170.7f));
        FEMALE_DATA = Collections.unmodifiableList(femaleTemp);
    }


    // 获取不可修改的数据列表
    public static List<HeightData> getMaleData() {
        return MALE_DATA;
    }

    public static List<HeightData> getFemaleData() {
        return FEMALE_DATA;
    }
}