function [ ratio ] = computeMagnificationRatio( gravity, startPoint, endPoint, length, focalLength, para_d )
%COMPUTEMAGNIFICATIONRATIO Compute magnification ratio using a known distance (Please see the paper for details).
% INPUT: 
%       gravity: an vector include gx,gy,gz.
%       startPoint: location of the first point of the known distance.
%       endPoint: location of the second points of the known distance.
%       focalLength: the estimated focal length.
%       length: length of the known distance.
%       para_d: parameter d is introduced in ground equation and donot effect the measurement result.
% OUTPUT:
%       ratio: computed result of the magnification ratio.

gz = gravity(3);
f = focalLength;
Lpp = length;
s = abs(para_d + f*gz);
F = Function_F(gravity,startPoint,endPoint,f);
ratio = Lpp / s / F;
end

