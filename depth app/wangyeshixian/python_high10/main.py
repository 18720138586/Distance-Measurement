from fastapi import FastAPI, File, UploadFile, Form
from typing import List
import numpy as np
import cv2
import os
import computeMagnificationRatio
import function_f
import PEDESTRIAN

app = FastAPI()


@app.post("/measure_height/")
async def measure_height(
        image: UploadFile = File(...),
        points: str = Form(...),
        gravity: str = Form(...),
        known_distance: float = Form(...)
):
    # 保存上传的图片
    image_path = "temp_image.jpg"
    with open(image_path, "wb") as buffer:
        buffer.write(image.file.read())

    # 解析点击点和重力数据
    points_list = [[float(p[0]), float(p[1])] for p in eval(points)]
    gravity_array = np.array(eval(gravity), dtype=np.float64)
    gravity_array = gravity_array / np.sum(np.abs(gravity_array))  # 归一化

    # 加载图像
    image = cv2.imread(image_path)
    height, width = image.shape[:2]
    width /= 2
    height /= 2

    # 创建中心点坐标数组
    center = np.array([width, height], dtype=np.float64)
    center_repeated = np.tile(center, (len(points_list), 1))
    points = np.array(points_list, dtype=np.float64) - center_repeated

    # 计算放大率e
    f = 2200 # 焦距
    d = 0  # 距离参数
    e = computeMagnificationRatio.computeMagnificationRatio(gravity_array, points[0], points[1], known_distance, f, d)
    s = np.abs(d + f * gravity_array[2])

    # 行人检测
    hights = PEDESTRIAN.PEDESTRIAN(image_path)  # 确保函数调用正确
    print(hights)
    point_top = np.array([0, hights[1]], dtype=np.float64)
    point_bottom = np.array([2, hights[3]], dtype=np.float64)
    point = np.vstack((point_top, point_bottom))

    # 确保tile的数据类型与point一致
    tile = np.tile([width, height], (point.shape[0], 1)).astype(point.dtype)
    point -= tile

    # 计算功能函数F
    F = function_f.Function_F(gravity_array, point[0, :], point[1, :], f)
    p1p3 = s * F

    # 计算角度θ
    C = np.array([0, 0, f], dtype=np.float64)
    a, b, c = gravity_array
    tp = -c * f / (a * point[0, 0] + b * point[0, 1] - c * f)
    cp = np.array([
        point[0, 0] * tp,
        point[0, 1] * tp,
        -tp * f + f
    ], dtype=np.float64) - C
    theta = np.pi / 2 - np.arccos(
        np.dot(cp, gravity_array) / (
                np.sqrt(np.sum(cp ** 2)) * np.sqrt(np.sum(gravity_array ** 2))
        )
    )

    # 计算最终高度
    res = e * p1p3 * np.tan(theta)
    res = abs(round(res, 2))
    # 删除临时图片
    os.remove(image_path)

    return {"height": res}