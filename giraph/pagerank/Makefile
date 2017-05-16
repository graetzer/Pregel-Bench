all: myjar.jar

PageRank.class: PageRank.java 
	javac -cp $GIRAPH_HOME/giraph-examples/target/giraph-examples-1.2.0-for-hadoop-2.5.1-jar-with-dependencies.jar:$(shell ${HADOOP_HOME}/bin/hadoop classpath) PageRank.java
	
DoubleSumCombiner.class: DoubleSumCombiner.java
	javac -cp $GIRAPH_HOME/giraph-examples/target/giraph-examples-1.2.0-for-hadoop-2.5.1-jar-with-dependencies.jar:$(shell ${HADOOP_HOME}/bin/hadoop classpath) DoubleSumCombiner.java

myjar.jar: PageRank.class DoubleSumCombiner.class
	cp $GIRAPH_HOME/giraph-examples/target/giraph-examples-1.2.0-for-hadoop-2.5.1-jar-with-dependencies.jar ./myjar.jar
	jar uf myjar.jar PageRank.class DoubleSumCombiner.class

tidy:
	rm -f *class

clean: tidy
	rm -f *.jar
