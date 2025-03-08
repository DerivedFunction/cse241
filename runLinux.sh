rm -rf App.jar del226/cse241/project/*.class
javac -cp "ojdbc8.jar:." del226/cse241/project/*.java
jar cfmv App.jar App.manifest del226
# export logging=true
java -jar App.jar