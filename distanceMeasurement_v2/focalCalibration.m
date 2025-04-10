clear;clc;

syms f;
image = imread('indoor.JPG');
figure(99),imshow(image),title('Click four vertices of two known distance:');
[X,Y] = ginput(4);
points = [X,Y];
close 99

% Set the image centre
[height,width] = size(image(:,:,1));
width = width/2;
height = height/2;
points = points - repmat([width,height],size(points,1),1);

% Gravity estimated via the mobile device
a = -0.02;b = 0.66;c = -0.32; 
gravity = [a;b;c];

L1 = input('Input the value of first known distance(cm)£º');
L2 = input('Input the value of second known distance(cm)£º');

F1 = Function_F(gravity,points(1,:)',points(2,:)', f);
F2 = Function_F(gravity,points(3,:)',points(4,:)', f);
fu = F1 / F2 - L1 / L2;

% Compute fu
fu = matlabFunction(fu);
res = fzero(fu,2000);
















