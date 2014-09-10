# The contsants

import sys
import os
import math
import types
import time
import random

from java.lang import *

from xal.extension.widgets.plot import FunctionGraphsJPanel

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

#default graph panel legend key
GRAPH_LEGEND_KEY = FunctionGraphsJPanel().getLegendKeyString()

const_path_dict = {}
const_path_dict["XAL_XML_ACC_FILES_DIRS_PATH"] = "/home/shishlo/xaldev_xml"
const_path_dict["OPENXAL_XML_ACC_FILES_DIRS_PATH"] = "/home/shishlo/openxal/core/test/resources/config"
const_path_dict["LINAC_WIZARD_FILES_DIR_PATH"] = "/home/shishlo/SHISHLO/ACC_PHYSICS/OPEN_XAL_Projects/LINAC_Tune_Wizard/data"
