import os
from PIL import Image
import numpy as np

def depth_read(filename):
    """
    从 PNG 文件中读取深度图，并将其转换为深度值数组
    :param filename: 深度图 PNG 文件的路径
    :return: 深度值数组
    """
    # 使用 PIL 的 Image.open 函数打开指定的 PNG 文件，并将其转换为 NumPy 数组
    depth_png = np.array(Image.open(filename), dtype=int)
    # 确保处理的是一个合适的 16 位深度图，而不是 8 位深度图
    assert(np.max(depth_png) > 255)
    # 将 depth_png 数组的数据类型转换为浮点数，并除以 256.0
    # 修改此处，将 np.float 替换为 float
    depth = depth_png.astype(float) / 256.
    # 将 depth_png 数组中值为 0 的元素对应的 depth 数组元素设置为 -1
    depth[depth_png == 0] = -1.
    return depth

def convert_depth_maps(folder_path):
    """
    遍历指定文件夹中的所有深度图文件，将其转换为深度值数组
    :param folder_path: 包含深度图文件的文件夹路径
    :return: 一个字典，键为文件名，值为对应的深度值数组
    """
    depth_maps = {}
    # 遍历文件夹中的所有文件
    for filename in os.listdir(folder_path):
        if filename.endswith('.png'):
            file_path = os.path.join(folder_path, filename)
            # 调用 depth_read 函数读取深度图并转换为深度值数组
            depth_map = depth_read(file_path)
            depth_maps[filename] = depth_map
    return depth_maps

# 定义要处理的文件夹路径
base_folder = r"D:\Python\train\2011_09_26_drive_0001_sync\proj_depth\groundtruth"
camera_folders = ['image_02', 'image_03']

# 定义保存 .txt 文件的目标文件夹
target_folder = r"D:\ceshi"
# 确保目标文件夹存在，如果不存在则创建
if not os.path.exists(target_folder):
    os.makedirs(target_folder)

for camera_folder in camera_folders:
    folder_path = os.path.join(base_folder, camera_folder)
    print(f"Processing {folder_path}...")
    # 调用 convert_depth_maps 函数处理指定文件夹中的深度图
    depth_maps = convert_depth_maps(folder_path)
    for filename, depth_map in depth_maps.items():
        print(f"Processed {filename}, shape: {depth_map.shape}")
        # 构建保存的 .txt 文件路径
        txt_filename = filename.replace('.png', '.txt')
        txt_save_path = os.path.join(target_folder, txt_filename)
        # 将深度值数组保存为 .txt 文件
        np.savetxt(txt_save_path, depth_map, fmt='%.6f')
        print(f"Saved {txt_save_path}")