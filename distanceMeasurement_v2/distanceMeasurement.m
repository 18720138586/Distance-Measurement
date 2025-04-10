%%
% This software is the matlab re-implementation, simplified version of the algorithm in the following IEEE-TCy paper.
% Please run the demo code "distanceMeasurement.m" for measuring various types of distances.
% *********************************************************************************
% If you are using this code in your publication, please cite our paper, Thanks.
% S. Chen X. Fang J. Shen L. Wang L. Shao, Single-Image Distance Measurement by a Smart Mobile Device.
%   IEEE Trans. on Cybernetics, vol.PP, no.99, pp.1-12, 2016
% *********************************************************************************
% Note that all the experimental results in the above paper are based on the
% original IOS/Objective C implementation, and we are currently not allowed to 
% distribute that original code.
% 
% NOTE THAT the measurement result is sensitive to the clicked positions which may be difficult to specify exactly.
%%
clear;clc;close all
image = imread('indoor.JPG');
figure(99),imshow(image),title('Click two vertices of one known distance:');
[X,Y] = ginput(2); 
point = [X Y];
close(99)

% Gravity estimated via the mobile device. 
a = -0.02;b = 0.66;c = -0.32; 
gravity = [a;b;c];
gravity = gravity / sum(abs(gravity));

% f can be estimated in focalCalibration.m
f = 2300;

C = [0;0;f];

% Simply set d = 0.
d = 0;

[height,width] = size(image(:,:,1));
width = width/2;
height = height/2;

point = point - repmat([width,height],size(point,1),1);

L = input('Input the value of the known distance(cm)£º');

%% Compute the Magnification Ratio.
e = computeMagnificationRatio(gravity,point(1,:)',point(2,:)', L, f, d);
s = abs(d + f*gravity(3));

%% Measure different types of distances, e.g., ground distance, depth, and height, can be measured 
% Ground distance
figure(99),imshow(image),title('Ground Distance:Click two vertices to measure length:');
[X,Y] = ginput(2);
point = [X Y];
point = point - repmat([width,height],size(point,1),1);
F = Function_F(gravity, point(1,:)', point(2,:)', f);
p1p2 = s * F;
res = e * p1p2;
display(['The ground distance between inputs points is: ',num2str(res) '(cm)']);

% Depth 
title('Depth:Click one point to measure depth:');
[X,Y] = ginput(1);
point = [X,Y];
point = point - repmat([width,height],size(point,1),1);
tp = -c * f/(a*point(1)+b*point(2)-c*f);
cp = [0;0;f] - [point(1)*tp;point(2)*tp;-tp*f+f];
CP = sqrt(sum(cp.^2));
res = e * CP;
display(['The depth of input point is: ',num2str(res) '(cm)']);

% Height 
title('Height:Click the top-vertex of height:');
[X,Y] = ginput(1);
point = [X Y];
title('Height:Click the bottom-vertex of height:');
[X,Y] = ginput(1);
point = [point;X Y];
point = point - repmat([width,height],size(point,1),1);
F = Function_F(gravity, point(1,:)', point(2,:)', f);
p1p3 = s * F;
tp = -c * f/(a*point(1)+b*point(2)-c*f);
cp = [point(1,1)*tp;point(1,2)*tp;-tp*f+f] - C;
theta = pi/2 - acos(cp'*gravity/sqrt(sum(cp.^2))/sqrt(sum(gravity.^2)));
res = e * p1p3 * tan(theta);
display(['The height between inputs points is: ',num2str(res) '(cm)']);
close(99)

