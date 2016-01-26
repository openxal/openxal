To use pythonTools in python or jython, one must add the path to pythonTools to their sytem path in the python file.

Example: To import urlFinder, add the following code to your python/jython script.

sys.path.insert(0,"/Users/.../core/src/xal/tools")

from pythonTools import urlFinder