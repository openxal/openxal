import random, pickle
import sys
#import marshal
import time
from array import array
import itertools






numbs1 = array('d', [1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7]) # must be same type
numbs2 = array('d', [8.8, 9.9, 10.10, 11.11, 12.12, 13.13, 14.14]) # must be same type

fxy = open("/home/tg4/Documents/CCR-data/2020/Nov17/bigFile.txt",'wb')
fxy.write(numbs1)
fxy.write(numbs2)
fxy.close()

fxy = open("/home/tg4/Documents/CCR-data/2020/Nov17/bigFile.txt",'rb')

data = array('d')
data.fromfile(fxy, 7)
print data
data = array('d')
data.fromfile(fxy, 7)
print data
fxy.close()


sys.exit()

print 2*2

g = {}

iterables = [['a','b'],['c','d','e'],['f','g']]

new_dic = {}
new_dic["a"] = {}
new_dic["a"]["b"] = 7.7

dic = 7.13

while True:
    print new_dic.keys()
    if dic < 17:
        dic += 1
    else:
        break


print nested_get(new_dic, ('a', 'b'))
print nested_get(new_dic, ('a', 'b'))



sys.exit()



shots = 100
d={}
for ik in ["k-"+str(i) for i in range(8)]:
    d[ik] = {}
    for vid in ["v1","v2"]:
        d[ik][vid]={}
        for sid in ["shot-"+str(i) for i in range(shots)]:
            d[ik][vid][sid]={}
            for bpmid in ["bpm-"+str(i) for i in range(44)]:
                d[ik][vid][sid][bpmid]={}
                d[ik][vid][sid][bpmid]["waveform-x"]=[random.random() for i in range(40)]
                d[ik][vid][sid][bpmid]["waveform-y"]=[random.random() for i in range(40)]
                
                
"""                
print "start1"
now = time.time() 

for ik in ["k-"+str(i) for i in range(8)]:
    for vid in ["v1","v2"]:
        for sid in ["shot-"+str(i) for i in range(shots)]:
            for bpmid in ["bpm-"+str(i) for i in range(44)]:
                
                filex = open("/home/tg4/Documents/CCR-data/2020/Nov17/data/"+ik+vid+sid+bpmid+"x.txt","wb")
                filey = open("/home/tg4/Documents/CCR-data/2020/Nov17/data/"+ik+vid+sid+bpmid+"y.txt","wb")
                
                filex.write(str(d[ik][vid][sid][bpmid]["waveform-x"]))
                filey.write(str(d[ik][vid][sid][bpmid]["waveform-y"]))
                
                filex.close()
                filey.close()

    
print time.time() - now
"""



print "start2.0"
now = time.time()
filexy = open("/home/tg4/Documents/CCR-data/2020/Nov17/bigFile.txt", "wb")
for ik in ["k-" + str(i) for i in range(8)]:
    for vid in ["v1", "v2"]:
        for bpmid in ["bpm-" + str(i) for i in range(44)]:
            for sid in ["shot-" + str(i) for i in range(shots)]:
                filexy.write(
                    str(d[ik][vid][sid][bpmid]["waveform-x"]) + str(d[ik][vid][sid][bpmid]["waveform-y"]) + '\n')


filexy.close()
print time.time() - now

#print time.time() - now


print "start2.1"
now = time.time()
filexy = open("/home/tg4/Documents/CCR-data/2020/Nov17/bigFile.txt", "wb")
for ik in ["k-" + str(i) for i in range(8)]:
    for vid in ["v1", "v2"]:
        for bpmid in ["bpm-" + str(i) for i in range(44)]:
            for sid in ["shot-" + str(i) for i in range(shots)]:
                filexy.write(array('f',d[ik][vid][sid][bpmid]["waveform-x"]))
                filexy.write(array('f',d[ik][vid][sid][bpmid]["waveform-y"]))
                filexy.write('\n')



filexy.close()
print time.time() - now

"""

print "start3"
now = time.time() 
file = open("/home/tg4/Documents/CCR-data/2020/Nov17/test.txt","wb")
pickle.dump( d, file )
file.close()
print time.time() - now
"""