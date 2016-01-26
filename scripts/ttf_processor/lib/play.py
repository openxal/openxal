from xal.tools.xml import XmlDataAdaptor

from java.net import URL

from java.net import URI

from java.io import File

import sys

import os

sys.path.insert(0,"/Users/j72/git/openxal/core/src/xal/tools")

from pythonTools import urlFinder

urlToFile = urlFinder.fetchURL("sns_TTF.xdxf") # Url for sns_TTF file
#-----------------------------------------------------------------------------------------------------
file_url = urlFinder.fetchURL("AndreiTTFData.DAT") # Url to Andrei's data

# Open the file and grab the names (n_arrn), the ttf data (ttf_arr), and the beta pairs (b_arr)
with open(file_url,'r') as f2r:
    i=0
    for line in f2r:
        if i == 0:       # line one: names
            n_arr = line
        elif i == 1:     # line two: ttf polys
            ttf_arr = line
        elif i == 2:     # line three: beta pairs SKIP
            print('')
        elif i == 3:     # line four: stf polys
            stf_arr = line
        elif i == 4:
            ttfp_arr = line
        elif i == 5:
            stfp_arr = line
        else:
            pass
        i+=1

n_arr = n_arr.split(',') # break the names up by commas
n_arr[-1]=n_arr[-1].rstrip() # strip newline character from last name in array

ttf_arr = ttf_arr.split(',')  # split the ttf by commas
ttf_arr[-1] = ttf_arr[-1].rstrip()  # strip newline character from last ttf in array

stf_arr = stf_arr.split(',')  # split the stf by commas
stf_arr[-1] = stf_arr[-1].rstrip()  # strip newline character from last stf in array

ttfp_arr = ttfp_arr.split(',')  # split the ttfp by commas
ttfp_arr[-1] = ttfp_arr[-1].rstrip()  # strip newline character from last ttfp in array

stfp_arr = stfp_arr.split(',')  # split the stfp by commas
stfp_arr[-1] = stfp_arr[-1].rstrip()  # strip newline character from last stfp in array

for name in n_arr:
    if name.startswith('MEBT'):
        new_name = name.replace(":","_")
        cav_id = new_name[4]
        gap_id = new_name[-1]
        new_name=new_name.replace(cav_id+"_",'_')
        new_name = new_name.replace("Rg"+gap_id,"RF:Bnch"+'0'+cav_id+":Rg0"+gap_id)
        n_arr[n_arr.index(name)] = new_name
    elif name.startswith('DTL'):
        new_name = name.replace(":","_")
        cav_id = new_name[3]
        try:
            int(new_name[-2])
            gap_id = new_name[-2]+new_name[-1]
        except Exception:
            gap_id = new_name[-1]
        new_name = new_name.replace(cav_id+"_","_")
        if len(gap_id)==2:
            new_name = new_name.replace('Rg'+gap_id,'RF:Cav0'+cav_id+':Rg'+gap_id)
        else:
            new_name = new_name.replace('Rg'+gap_id,'RF:Cav0'+cav_id+':Rg0'+gap_id)
        n_arr[n_arr.index(name)] = new_name
    elif name.startswith('CCL'):
        new_name = name.replace(":","_")
        cav_id = new_name[3]
        try:
            int(new_name[-2])
            gap_id = new_name[-2]+new_name[-1]
        except Exception:
            gap_id = new_name[-1]
        new_name = new_name.replace(cav_id+"_","_")
        if len(gap_id)==2:
            new_name = new_name.replace('Rg'+gap_id,'RF:Cav0'+cav_id+':Rg'+gap_id)
        else:
            new_name = new_name.replace('Rg'+gap_id,'RF:Cav0'+cav_id+':Rg0'+gap_id)
        n_arr[n_arr.index(name)] = new_name
    elif name.startswith('SCL'):
        new_name = name.replace('SCL','SCL_RF')
        n_arr[n_arr.index(name)] = new_name
    else:
        pass
for thing in n_arr:
    print(thing)
        
    