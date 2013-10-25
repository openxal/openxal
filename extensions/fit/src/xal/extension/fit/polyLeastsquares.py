#
# A simple demo of the 2-D x-y ploter
#


from xal.extension.fit import *
from java.util import *
from jarray import zeros, array

# Simple single curve x/y plot:

xs = [1., 2., 3., 4. ]
ys = [1.1, 1.9, 3.15, 4.22]

k=3

lsf = PolyLeastsquares(xs, ys, k);

for i in range(4):
    print i,' ', ys[i], ' ' , lsf.getValue(xs[i])
    
print lsf.Correlation()

print lsf.Equation()

