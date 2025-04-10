import numpy as np

# 假设的 .txt 文件路径
txt_file_path = r"D:\ceshi\0000000005.txt"

# 加载 .txt 文件
depth_data = np.loadtxt(txt_file_path)

# 图像的高度和宽度
height, width = depth_data.shape

# 示例：获取图像中第 100 行、第 200 列像素的深度值
row_index = 100
col_index = 300

if 0 <= row_index < height and 0 <= col_index < width:
    depth_value = depth_data[row_index, col_index]
    print(f"图像中第 {row_index} 行、第 {col_index} 列像素的深度值为: {depth_value}")
else:
    print("指定的像素位置超出了图像范围。")