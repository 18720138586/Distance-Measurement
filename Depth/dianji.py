import os
from PIL import Image
import numpy as np
import matplotlib
matplotlib.use('TkAgg')
import matplotlib.pyplot as plt

def depth_read(filename):
    """
    从 PNG 文件中读取深度图，并将其转换为深度值数组
    :param filename: 深度图 PNG 文件的路径
    :return: 深度值数组
    """
    depth_png = np.array(Image.open(filename), dtype=int)
    assert(np.max(depth_png) > 255)
    depth = depth_png.astype(float) / 256.
    depth[depth_png == 0] = -1.
    return depth

def convert_depth_maps(folder_path):
    """
    遍历指定文件夹中的所有深度图文件，将其转换为深度值数组
    :param folder_path: 包含深度图文件的文件夹路径
    :return: 一个字典，键为文件名，值为对应的深度值数组
    """
    depth_maps = {}
    for filename in os.listdir(folder_path):
        if filename.endswith('.png'):
            file_path = os.path.join(folder_path, filename)
            depth_map = depth_read(file_path)
            depth_maps[filename] = depth_map
    return depth_maps

# 定义要处理的文件夹路径
base_folder = r"D:\Python\train\2011_09_26_drive_0001_sync\proj_depth\groundtruth"
camera_folders = ['image_02', 'image_03']

# 定义保存 .txt 文件的目标文件夹
target_folder = r"D:\ceshi"
if not os.path.exists(target_folder):
    os.makedirs(target_folder)

# 处理所有图片并保存深度数据为 .txt 文件
for camera_folder in camera_folders:
    folder_path = os.path.join(base_folder, camera_folder)
    print(f"Processing {folder_path}...")
    depth_maps = convert_depth_maps(folder_path)
    for filename, depth_map in depth_maps.items():
        print(f"Processed {filename}, shape: {depth_map.shape}")
        txt_filename = filename.replace('.png', '.txt')
        txt_save_path = os.path.join(target_folder, txt_filename)
        np.savetxt(txt_save_path, depth_map, fmt='%.6f')
        print(f"Saved {txt_save_path}")

# 选择一张图片进行交互
# 这里简单选择 image_02 文件夹下的第一张图片为例
image_folder = os.path.join(base_folder, 'image_02')
image_files = [f for f in os.listdir(image_folder) if f.endswith('.png')]
if image_files:
    first_image_file = image_files[0]
    image_path = os.path.join(image_folder, first_image_file)
    txt_filename = first_image_file.replace('.png', '.txt')
    txt_path = os.path.join(target_folder, txt_filename)

    # 读取图片和对应的深度数据
    image = Image.open(image_path)
    depth_data = np.loadtxt(txt_path)

    def onclick(event):
        if event.inaxes is not None:
            x, y = int(event.xdata), int(event.ydata)
            if 0 <= x < depth_data.shape[1] and 0 <= y < depth_data.shape[0]:
                depth_value = depth_data[y, x]
                print(f"你点击了图片中坐标为 ({x}, {y}) 的点，对应的深度值为: {depth_value}")
            else:
                print("点击的位置超出了深度数据的范围。")

    # 显示图片
    fig, ax = plt.subplots()
    ax.imshow(image)
    fig.canvas.mpl_connect('button_press_event', onclick)
    plt.show()
else:
    print("未找到图片文件。")