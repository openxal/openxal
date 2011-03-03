#!/bin/bash
# Publish the web content to the Open XAL web server to appear on http://xaldev.sourceforge.net/

# home is the directory in which this script exists
script_home=${0%/*}     # need to close what the editor thinks is a comment */


# publish the local website to the remote server
function publishSite {
    # get the path to the local web content relative to this script
    source_path=${0%/*}/root/   # need to close what the editor thinks is a comment */
    echo "Path to web content: ${source_path}"

    # verify that the path is valid
    if [ -d ${source_path} ] ; then
        echo "Enter SourceForge username: "
        read username
        rsync -ave ssh --delete --exclude=".DS_Store" --delete-excluded ${source_path} ${username},xaldev@web.sourceforge.net:htdocs/
    else
        echo "${source_path} is not a valid directory"
        exit
    fi
}


# publish the website
publishSite
