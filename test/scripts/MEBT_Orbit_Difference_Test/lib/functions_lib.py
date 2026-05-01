# The functions

import sys
import os
import math
import types
import time
import random

def calculateAvgErr(val_arr):
	#calculates average and statistical errors of the value from the array of values
	n_vals = len(val_arr)
	if(n_vals == 0): return (0.,0.)
	if(n_vals == 1): return (val_arr[0],0.)
	avg = 0.
	avg2 = 0.
	for val in val_arr:
		avg += val
		avg2 += val*val
	avg /= n_vals
	avg2 /= n_vals
	err = math.sqrt(math.fabs(avg2 - avg*avg)/(n_vals-1))
	return (avg,err)
