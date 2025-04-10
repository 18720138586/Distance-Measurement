import function_f
def computeMagnificationRatio(gravity, startPoint, endPoint,length,focalLength,para_d):
    """
    Compute magnification ratio using a known distance.
    """
    gz = gravity[2]
    f = focalLength
    Lpp = length
    s = abs(para_d + f*gz)
    F = function_f.Function_F(gravity, startPoint, endPoint, f)
    ratio = Lpp/s/F
    return ratio
