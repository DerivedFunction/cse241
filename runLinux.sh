rm -rf App.jar *.class
javac *.java
jar cfmv App.jar App.manifest *.class
java -jar App.jar