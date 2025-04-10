import matplotlib
matplotlib.use('TkAgg')  # 或者使用 'Agg'
import h5py
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.font_manager import FontProperties

# 设置全局字体为 SimHei（黑体），以支持中文显示
plt.rcParams['font.sans-serif'] = ['SimHei']
# 解决负号显示为方块的问题
plt.rcParams['axes.unicode_minus'] = False

# 读取数据
mat_file_path = 'D:/download/Chrome/nyu_depth_v2_labeled.mat'

with h5py.File(mat_file_path, 'r') as f:
    images = f['images'][:]  # RGB图像，维度为Nx3xHxW
    depths = f['depths'][:]  # 深度图像，维度为NxHxW

# 假设使用第一个样本
image = np.transpose(images[0], (1, 2, 0))  # 转换维度为HxWx3
depth = depths[0]  # 深度图像

# 创建图形和轴
fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 6))
ax1.imshow(image)
ax1.set_title('RGB 图像')
im = ax2.imshow(depth, cmap='jet')
ax2.set_title('深度图像')
fig.colorbar(im, ax=ax2, fraction=0.046, pad=0.04)

# 定义点击事件的回调函数
def onclick(event):
    # 获取点击位置的像素坐标
    x, y = int(event.xdata), int(event.ydata)
    if 0 <= x < depth.shape[1] and 0 <= y < depth.shape[0]:
        depth_value = depth[y, x]
        print(f'位置 ({x}, {y}) 的深度值为: {depth_value:.2f} 米')
    else:
        print('点击位置超出图像范围')

# 连接点击事件
fig.canvas.mpl_connect('button_press_event', onclick)

plt.tight_layout()
plt.show()

