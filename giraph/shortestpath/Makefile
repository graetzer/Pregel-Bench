all: myjar.jar

ShortestPath.class: ShortestPath.java 
	javac -cp /graetzer/giraph/giraph/giraph-examples/target/giraph-examples-1.2.0-for-hadoop-2.5.1-jar-with-dependencies.jar:$(shell ${HADOOP_HOME}/bin/hadoop classpath) ShortestPath.java
	
DoubleMinCombiner.class: DoubleMinCombiner.java
	javac -cp /graetzer/giraph/giraph/giraph-examples/target/giraph-examples-1.2.0-for-hadoop-2.5.1-jar-with-dependencies.jar:$(shell ${HADOOP_HOME}/bin/hadoop classpath) DoubleMinCombiner.java

myjar.jar: ShortestPath.class DoubleMinCombiner.class
	cp /graetzer/giraph/giraph/giraph-examples/target/giraph-examples-1.2.0-for-hadoop-2.5.1-jar-with-dependencies.jar ./myjar.jar
	jar uf myjar.jar ShortestPath.class DoubleMinCombiner.class

tidy:
	rm -f *class

clean: tidy
	rm -f *.jar
