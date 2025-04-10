import math
import numpy as np
def Function_F(gravity, firstPoint, secondPoint, focalLength):
    """
    Compute the F(p, p', f) function described in the paper.
    Function_F is a function of p, p' and f. Detailed explanation is shown in the paper.
    """
    
    gx, gy, gz = gravity[0],gravity[1],gravity[2]
   
    px, py = firstPoint[0],firstPoint[1]
    pxp, pyp = secondPoint[0],secondPoint[1]
    f = focalLength

    A = (gx**2 + gz**2)*(px - pxp)**2 + (gy**2 + gz**2)*(py - pyp)**2 + 2*gx*gy*(pxp - px)*(pyp - py)
    B = 2*gz*(pxp*py - px*pyp) * (gy*(px - pxp) + gx*(pyp - py))
    C = (gx**2 + gy**2)*(pxp*py - px*pyp)**2
    D = gz**2
    E = -(gx*gz*pxp + gy*gz*pyp + gx*gz*px + gy*gz*py)
    F = gx**2*px*py + gx*gy*pxp*py + gx*gy*px*pyp + gy**2*py*pyp

    Function_F = math.sqrt(A*f**2 + B*f + C) / abs(D*f**2 + E*f + F)
    
    return Function_F
##point=[(-597.64285714 , 461.84920635),( 397.5952381 ,  468.83333333)]
##a, b, c = -0.02, 0.66, -0.32
##gravity = np.array([a, b, c])
##gravity = gravity/np.sum(np.abs(gravity)) # 归一化gravity，确保其模长为1
##Function_F(gravity, point[0],point[1], 2300)