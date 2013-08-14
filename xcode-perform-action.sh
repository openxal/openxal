#!/bin/bash
#
# Perform an action based on the Xcode ACTION build variable and other related target build settings prefixed with "XCPA_".
#
# Created by Tom Pelaia on August 27, 2012, 8:48 AM
# Copyright 2012 Oak Ridge National Lab. All rights reserved.
#


# Xcode passes an empty string for the default build action so we create a local variable to hold the action transformed as needed
xcpa_action=${ACTION}
if [ "${ACTION}" = "" ] ; then
	xcpa_action="build"
fi

# root for build variables will be the action in upper case: "build" -> "XCPA_BUILD", "clean" -> "XCPA_CLEAN", etc.
xcpa_root=$( echo "XCPA_${xcpa_action}" | tr '[a-z]' '[A-Z]' )


# name for the action's corresponding command build setting: "XCPA_BUILD_COMMAND", "XCPA_CLEAN_COMMAND", etc.
action_command_name="${xcpa_root}_COMMAND"

xcpa_command=${XCPA_DEFAULT_COMMAND}	# use the default command if no action specific command has been specified
# if an action specific command has been specified then it is used
if [ "${!action_command_name}" != "" ] ; then
	xcpa_command=${!action_command_name}
#	echo "Action Command Name: ${action_command_name}"
#	echo "Action Command: ${!action_command_name}"
fi

# check that a suitable command was found
if [ "${xcpa_command}" = "" ] ; then
	echo "Error: No command has been specified for the action: ${xcpa_action}. Could not find build setting XCPA_DEFAULT_COMMAND or ${action_command_name}."
	exit -1
fi


# name for the action's corresponding arguments build setting: "XCPA_BUILD_ARGS", "XCPA_CLEAN_ARGS", etc.
action_args_name="${xcpa_root}_ARGS"
xcpa_action_args=${!action_args_name}	# get the value from the args build setting
# if the args is empty, then default to the action as the args
if [ "${xcpa_action_args}" = "" ] ; then
	xcpa_action_args=${ACTION}
fi


# display and execute the command passing the build args and action specific args
echo "${xcpa_command} ${xcpa_action_args}"
${xcpa_command} ${xcpa_action_args}
