all: myjar.jar

PageRank.class: PageRank.java
	javac -cp /graetzer/giraph/giraph-1.2.0-hadoop2-for-hadoop-2.5.1/giraph-core-1.2.0-hadoop2.jar:/graetzer/giraph/giraph-1.2.0-hadoop2-for-hadoop-2.5.1/giraph-examples-1.2.0-hadoop2.jar:$(shell ${HADOOP_HOME}/bin/hadoop classpath) PageRank.java

myjar.jar: PageRank.class
	cp /graetzer/giraph/giraph-1.2.0-hadoop2-for-hadoop-2.5.1/giraph-examples-1.2.0-hadoop2.jar ./myjar.jar
	jar uf myjar.jar PageRank*class


tidy:
	rm -f *class

clean: tidy
	rm -f *.jar
