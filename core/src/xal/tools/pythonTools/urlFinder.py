'''
Created on June 1, 2015

@author: James Ghawaly Jr.
'''

import os
from test.test_xmlrpc import URL

'''
This function finds the URL of the file in question in the OpenXal project.

inputs:
    filename: Name of the file being searched for.
    top: Basepath of the file you are looking for. Example: If you know the file is somewhere in /Users/you/Documents, use that.
outputs:
    URL: File URL
'''
def fetchURL(filename):

    cwd=os.getcwd()
    split_cwd = cwd.split("/")
    file_found=False
    xal_exists=False
    
    for root, dirs, files in os.walk("/"+split_cwd[1],topdown=False):
        
        if "openxal" in dirs:
            top = root+"/"+"openxal"
            xal_exists=True

    if xal_exists:
        
        for root, dirs, files in os.walk(top,topdown=False):
            
            if filename in files:
                url=root+"/"+filename
                file_found=True
                return url
            
        if not file_found:
            print("Error: No file of that name found in current directory tree.")
    else:
        print("Error: No folder in file system named openxal")