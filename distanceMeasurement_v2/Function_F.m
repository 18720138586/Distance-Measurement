function [ Function_F ] = Function_F( gravity, firstPoint, secondPoint, focalLength)
%FUNCTION_F Compute the F(p,p',f) function described in the paper.
%   Function_F is a function of p, p' and f. Detailed explanation is shown in the paper.

gx = gravity(1);
gy = gravity(2);
gz = gravity(3);
px = firstPoint(1);
py = firstPoint(2);
pxp = secondPoint(1);
pyp = secondPoint(2);
f = focalLength;

A = (gx^2+gz^2)*(px-pxp)^2 + (gy^2+gz^2)*(py-pyp)^2 + 2*gx*gy*(pxp-px)*(pyp-py);
B = 2*gz*(pxp*py-px*pyp) * (gy*(px-pxp) + gx*(pyp-py));
C = (gx^2+gy^2)*(pxp*py - px*pyp)^2;
D = gz^2;
E = -(gx*gz*pxp + gy*gz*pyp + gx*gz*px + gy*gz*py);
F = gx^2*px*py + gx*gy*pxp*py + gx*gy*px*pyp + gy^2*py*pyp;

Function_F = sqrt(A*f^2 + B*f + C) / abs(D*f^2 + E*f + F);
end

