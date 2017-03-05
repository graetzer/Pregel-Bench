// Sources:
// http://buttercola.blogspot.ch/2015/04/giraph-page-rank.html
// https://www.safaribooksonline.com/blog/2014/02/12/understanding-apache-giraph-application/

import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.conf.LongConfOption;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.log4j.Logger;
 
import java.io.IOException;
 
public class PageRank extends BasicComputation<
    LongWritable, DoubleWritable, NullWritable, DoubleWritable> {
 
  public static final int MAX_SUPERSTEPS = 20;
   
  @Override
  public void compute(Vertex<LongWritable, DoubleWritable, NullWritable> vertex, 
      Iterable<DoubleWritable> messages) throws IOException {
    double value = 0;
    if (getSuperstep == 0) {
      value = 1.0 / getTotalNumVertices();
    } else {
      double sum = 0;
      for (DoubleWritable message : messages) {
        sum += message.get();
      }
      value = 0.15 / getTotalNumVertices() + 0.85 * sum;
      //vertex.setValue(new DoubleWritable(sum));
    }
    vertex.setValue(new DoubleWritable(value));

    if (getSuperstep() < MAX_SUPERSTEPS) {
      int numEdges = vertex.getNumEdges();
      DoubleWritable message = new DoubleWritable(value / numEdges);
      for (Edge<LongWritable, NullWritable> edge: vertex.getEdges()) {
        sendMessage(edge.getTargetVertexId(), message);
      }
    }
    else {
      vertex.voteToHalt();
    }
  }  
}
