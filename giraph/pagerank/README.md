# hadoop2

Advanced commands to get a giraph program running. Adjust paths accordingly

Download hadoop
```
wget http://archive.apache.org/dist/hadoop/core/hadoop-2.5.1/hadoop-2.5.1.tar.gz
tar -zxvf  hadoop-2.5.1.tar.gz && rm hadoop-2.5.1.tar.gz
```

Download and compile giraph
```
wget https://github.com/apache/giraph/archive/rel/1.2.0-RC1.tar.gz
tar -zxvf   1.2.0-RC1.tar.gz
mvn package -DskipTests -Phadoop_2
```

Set enviroment variables for example
```
export JAVA_HOME=/usr/lib/jvm/default-java
export HADOOP_HOME=/graetzer/hadoop-2.5.1
export HADOOP_PREFIX=/graetzer/hadoop-2.5.1
export HADOOP_OPTS=-Djava.net.preferIPv4Stack=true
export GIRAPH_HOME=/graetzer/giraph-1.2/
```

Now you can run `make all` to compile the pagerank code. This will bundle your
code together with the giraph JAR with **all** dependencies. This jar can then be deployed to a 
yarn cluster as it is.


Deploy program to yarn cluster:
```
$HADOOP_HOME/bin/hdfs dfs -rmr /user/graetzer/input
$HADOOP_HOME/bin/hdfs dfs -mkdir /user
$HADOOP_HOME/bin/hdfs dfs -mkdir /user/graetzer
$HADOOP_HOME/bin/hdfs dfs -put ~/testdata/orkut/edges-adj.txt /user/graetzer/input

$HADOOP_HOME/bin/hadoop jar myjar.jar org.apache.giraph.GiraphRunner PageRank --yarnjars myjar.jar --workers 1 \
--vertexInputFormat org.apache.giraph.examples.LongDoubleNullTextInputFormat --vertexInputPath /user/graetzer/input \
--vertexOutputFormat org.apache.giraph.io.formats.IdWithValueTextOutputFormat --outputPath /user/graetzer/output \
--combiner DoubleSumCombiner -ca giraph.SplitMasterWorker=false
```
