DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
XAL_HOME=$DIR/../../../../../
java -cp $XAL_HOME/build/jar/ext.jar:$XAL_HOME/build/jar/xal.jar:$XAL_HOME/build/jar/apps/emittanceanalysis.jar gov.sns.apps.emittanceanalysis.scanner.DataReader $*
