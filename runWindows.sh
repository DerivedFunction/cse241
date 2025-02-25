rm -rf App.jar *.class
javac *.java
jar cfmv App.jar App.manifest *.class
# export logging=true
java -jar App.jar