
import matplotlib.image as mpimg
import sympy as sp
import function_f
import numpy as np
from scipy.optimize import fsolve
import matplotlib.pyplot as plt
from matplotlib.widgets import Button


# 读取图像
image_path = 'person.jpg'
image = plt.imread(image_path)
plt.imshow(image)
plt.title('Click four vertices of two known distances:')

# 初始化点的列表
points = []

# 定义鼠标点击事件处理函数
def onclick(event):
    global points
    if len(points) < 4:
        points.append((event.xdata, event.ydata))
        plt.plot(event.xdata, event.ydata, 'ro')  # 标记点
        plt.draw()  # 更新图像显示

# 连接鼠标点击事件到处理函数
cid = plt.gcf().canvas.mpl_connect('button_press_event', onclick)

# 显示图像
plt.imshow(image)
plt.title('Click on the image to select four points, then close the window to continue')

# 运行事件循环，等待用户点击
plt.show()

# 断开事件连接
plt.gcf().canvas.mpl_disconnect(cid)

# 确保我们得到了四个点
if len(points) != 4:
    raise ValueError("We need exactly four points.")

# 计算图像中心
width, height = image.shape[1], image.shape[0]
center_x, center_y = width / 2, height / 2

# 将点坐标转换为相对于图像中心的坐标
points = np.array(points) - np.array([center_x, center_y])

# 重力加速度估计值
a, b, c = -0.02, 0.96, -0.02
gravity = np.array([a, b, c])

# 输入已知的两段距离
L1 = float(input('Input the value of first known distance(cm): '))
L2 = float(input('Input the value of second known distance(cm): '))

# 假设你已经定义了 function_f

# 定义方程，用于求解焦距 f
def equation(f, points, L1, L2):
    F1 = function_f.Function_F(gravity, points[0], points[1], f)
    F2 = function_f.Function_F(gravity, points[2], points[3], f)
    return (F1 / F2 - L1 / L2)

# 求解焦距 f
initial_guess = 2000  # 初始猜测值
solution, = fsolve(equation, initial_guess, args=(points, L1, L2))

print(f'Estimated focal length: {solution}')