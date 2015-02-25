# Timer field class

import sys
import math
import types
import time
import random

from java.lang import *
from javax.swing import *
from java.text import SimpleDateFormat
from java.util import Date

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

class DateAndTimeText:
	
	def __init__(self):
		self.dFormat = SimpleDateFormat("'Time': MM.dd.yy HH:mm ")
		self.dateTimeField = JFormattedTextField(self.dFormat)
		self.dateTimeField.setEditable(false)
		thr = Thread(Timer(self.dateTimeField))
		thr.start()

	def getTime(self):
		return self.dateTimeField.getText()
		
	def getTimeTextField(self):
		return self.dateTimeField
		
	def getNewTimeTextField(self):
		newText = JTextField()
		newText.setDocument(self.dateTimeField.getDocument())
		newText.setEditable(false)
		return newText


class Timer(Runnable):
	
	def __init__(self,dateTimeField):
		self.dateTimeField = dateTimeField
	
	def run(self):
		while(true):
			self.dateTimeField.setValue(Date())
			time.sleep(10.)
