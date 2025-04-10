import torch
import cv2
import  ultralytics
# 加载YOLOv5模型
def PEDESTRIAN( image_path ):

  model = torch.hub.load('ultralytics/yolov5', 'yolov5s')

# 行人标
  PEDESTRIAN_LABEL = 'person'


# 检测行人函数
  def detect_pedestrians(image):
    # 执行推理
    results = model(image)

    # 提取边界框、标签和置信度
    bboxes = results.xyxy[0].numpy()  # 边界框以xyxy格式
    labels = results.names  # 标签
    detections = []

    for *bbox, conf, cls in bboxes:
        if labels[int(cls)] == PEDESTRIAN_LABEL:
            detections.append((bbox, conf))

    return detections


# 读取图像
  image_path1 =    image_path  # 替换为你的图像路径
  image1 = cv2.imread(image_path1)

# 检测行人
  detections = detect_pedestrians(image1)

# 在检测到的行人周围绘制边框
  for (bbox, conf) in detections:
    x1, y1, x2, y2 = map(int, bbox)
    cv2.rectangle(image1, (x1, y1), (x2, y2), (0, 255, 0), 2)
    cv2.putText(image1, f'{PEDESTRIAN_LABEL} {conf:.2f}', (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.9, (0, 255, 0), 2)

    # 显示图像
  # 显示部分完整代码
  winname = 'Pedestrian Detection'
  cv2.namedWindow(winname, cv2.WINDOW_KEEPRATIO | cv2.WINDOW_GUI_NORMAL)
  cv2.imshow(winname, image1)
  cv2.destroyAllWindows()

# 如果需要保存检测结果，可以使用以下代码
  output_image_path = 'person_det.jpg'  # 替换为你要保存的路径
  cv2.imwrite(output_image_path, image1)


  return(x1,y1,x2,y2)

