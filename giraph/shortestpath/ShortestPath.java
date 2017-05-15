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
 
public class ShortestPath extends BasicComputation<
    LongWritable, DoubleWritable, NullWritable, DoubleWritable> {
 
  public static final int MAX_SUPERSTEPS = 20;

  public static final LongConfOption SOURCE_ID =
      new LongConfOption("SimpleShortestPathsVertex.sourceId", 1,
          "The shortest paths id");

    /**
    * Is this vertex the source id?
    *
    * @param vertex Vertex
    * @return True if the source id
    */
    private boolean isSource(Vertex<LongWritable, ?, ?> vertex) {
        return vertex.getId().get() == SOURCE_ID.get(getConf());
    }
   
  @Override
  public void compute(Vertex<LongWritable, DoubleWritable, NullWritable> vertex, 
      Iterable<DoubleWritable> messages) throws IOException {
    if (getSuperstep() == 0) {
      vertex.setValue(new DoubleWritable(Double.MAX_VALUE));
    }
    double minDist = isSource(vertex) ? 0d : Double.MAX_VALUE;
    for (DoubleWritable message : messages) {
      minDist = Math.min(minDist, message.get());
    }

    if (minDist < vertex.getValue().get()) {
      vertex.setValue(new DoubleWritable(minDist));
      for (Edge<LongWritable, NullWritable> edge : vertex.getEdges()) {
        double distance = minDist + 1;//edge.getValue().get();
        sendMessage(edge.getTargetVertexId(), new DoubleWritable(distance));
      }
    }
    vertex.voteToHalt();
  }  
}
