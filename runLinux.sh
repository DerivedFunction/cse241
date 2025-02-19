rm -rf App.jar *.class
javac *.java
jar cfmv App.jar App.manifest *.class
export logging=true
java --class-path "ojdbc11.jar:." App
java -jar App.jar