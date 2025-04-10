import matplotlib
matplotlib.use('TkAgg')
import h5py
import numpy as np
import matplotlib.pyplot as plt

plt.rcParams['font.sans-serif'] = ['SimHei']
plt.rcParams['axes.unicode_minus'] = False

mat_file_path = 'D:/download/Chrome/nyu_depth_v2_labeled.mat'

with h5py.File(mat_file_path, 'r') as f:
    images = f['images'][:]
    depths = f['depths'][:]
    accelData = f['accelData'][:]  # 加速度数据，维度为 (4, N) 或 (3, N)

# 取第一个样本
image = np.transpose(images[0], (1, 2, 0))
depth = depths[0]
accel = accelData[:3, 0]  # 仅获取前三个加速度值

# 计算与重力方向的夹角（与水平面的角度）
g = np.array([0, 0, -1])  # 世界坐标系中竖直方向
a_norm = accel / np.linalg.norm(accel)
angle_rad = np.arccos(np.clip(np.dot(a_norm, g), -1.0, 1.0))
angle_deg = np.degrees(angle_rad)

# 图形展示
fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 6))
ax1.imshow(image)
ax1.set_title('RGB 图像')
im = ax2.imshow(depth, cmap='jet')
ax2.set_title('深度图像')
fig.colorbar(im, ax=ax2, fraction=0.046, pad=0.04)

def onclick(event):
    x, y = int(event.xdata), int(event.ydata)
    if 0 <= x < depth.shape[1] and 0 <= y < depth.shape[0]:
        depth_value = depth[y, x]
        print(f'位置 ({x}, {y}) 的深度值为: {depth_value:.2f} 米')
        print(f'相机与水平面的夹角为: {angle_deg:.2f}°')
    else:
        print('点击位置超出图像范围')

fig.canvas.mpl_connect('button_press_event', onclick)
plt.tight_layout()
plt.show()
