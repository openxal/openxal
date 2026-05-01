 XAL_HOME=$HOME/az9/gitrepo/xal
java -cp $XAL_HOME/build/jar/ext.jar:$XAL_HOME/build/jar/xal.jar:$XAL_HOME/build/jar/apps/emittanceanalysis.jar gov.sns.apps.emittanceanalysis.scanner.Calibrator $1
