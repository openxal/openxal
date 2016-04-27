#!/usr/bin/env jruby
#
#  waveform-monitor.rb
#  Monitor and Display a selected waveform and its spectrum
#
#  Created by Tom Pelaia on 3/3/08.
#  Copyright (c) 2007 SNS. All rights reserved.
#

require 'java'
require 'socket'

import java.lang.System
import java.awt.Desktop
import java.net.URL
import java.util.ArrayList

java_import 'xal.extension.service.ServiceDirectory'
java_import 'xal.service.worker.Working'


# implement the Working service
class Worker
	include Working		# Working interface to implement

	def initialize( name )
		@LAUNCH_TIME = Java::java.util.Date.new

		# register this instance as a provider of the Working service
		service = ServiceDirectory.defaultDirectory.registerService( Working.java_class, name, self )
		puts "Working service is listening at: ws://#{Socket::gethostname}:#{service.getPort}"

		# Launch the browser to point to the address of this service
		folder = File.expand_path File.dirname( __FILE__ )		# directory where this script is located
		service_url = java.net.URL.new( "file://#{folder}/ws-demo-client.html?servport=#{service.getPort}&servname=#{name}&servhost=#{Socket::gethostname}" )	# URL to this service
		puts "Service URL: #{service_url}"
		Desktop.getDesktop().browse( service_url.toURI )	# launch the browser with the service URL
	end


	# add two doubles
	def add( summand, addend )
		return summand + addend
	end


	# sum the integers in the specified array
	def sumIntegers( summands )
		sum = 0
		summands.each { |summand| sum += summand }
		return sum
	end


	# get the launch time
	def getLaunchTime
		return @LAUNCH_TIME
	end


	# calculate the sinusoid waveform from zero to 1
	def generateSinusoid( amplitude, frequency, phase, numPoints )
		pi = Math.acos( -1 )
		omega = 2 * pi * frequency
		step = 1.0 / ( numPoints - 1 )

		x = 0
		waveform = []
		numPoints.times do |ignore|
			value = amplitude * Math.sin( omega * x + phase )
			waveform.push( value )
			x += step
		end

		return waveform.to_java :double
	end


	# say hello to the person with the specified name
	def sayHelloTo name
		return name != nil && name.length > 0 ? "Hello, #{name}!" : "Greetings!"
	end


	# shutdown the service
	def shutdown( code )
		ServiceDirectory.defaultDirectory.dispose()
		exit( code )
	end
end


Worker.new( "Math" )
