/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.graph.social;

import org.apache.flink.api.common.ProgramDescription;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Graph;
import org.apache.flink.graph.Vertex;
import org.apache.flink.graph.examples.data.SingleSourceShortestPathsData;
import org.apache.flink.graph.pregel.ComputeFunction;
import org.apache.flink.graph.pregel.MessageCombiner;
import org.apache.flink.graph.pregel.MessageIterator;
import org.apache.flink.graph.utils.Tuple3ToEdgeMap;
import org.apache.flink.types.NullValue;

public class SSSP implements ProgramDescription {
  
  public static void main(String[] args) throws Exception {
    
    if (!parseParameters(args)) {
      return;
    }
    
    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
    
    //DataSet<Edge<Long, Double>> edges = getEdgesDataSet(env);
    //Graph<Long, Double, Double> graph = Graph.fromDataSet(edges, new InitVertices(), env);
    DataSet<Tuple2<Long, Long>> edges = getEdgesDataTupleSet(env);
    Graph<Long, Long, NullValue> graph = Graph.fromTuple2DataSet(edges, new InitVertices(), env);
    
    // Execute the vertex-centric iteration
    Graph<Long, Long, NullValue> result = graph.runVertexCentricIteration(
                                                                         new SSSPComputeFunction(srcVertexId), new SSSPCombiner(),
                                                                         maxIterations);
    
    // Extract the vertices as the result
    DataSet<Vertex<Long, Long>> singleSourceShortestPaths = result.getVertices();
    
    // emit result
    if (fileOutput) {
      singleSourceShortestPaths.writeAsCsv(outputPath, "\n", ",");
      env.execute("Pregel Single Source Shortest Paths Example");
    } else {
      singleSourceShortestPaths.print();
    }
    
  }
  
  // --------------------------------------------------------------------------------------------
  //  Single Source Shortest Path UDFs
  // --------------------------------------------------------------------------------------------
  
  @SuppressWarnings("serial")
  private static final class InitVertices implements MapFunction<Long, Long> {
    
    public Long map(Long id) { return Long.MAX_VALUE; }
  }
  
  /**
   * The compute function for SSSP
   */
  @SuppressWarnings("serial")
  public static final class SSSPComputeFunction extends ComputeFunction<Long, Long, NullValue, Long> {
    
    private final long srcId;
    
    public SSSPComputeFunction(long src) {
      this.srcId = src;
    }
    
    public void compute(Vertex<Long, Long> vertex, MessageIterator<Long> messages) {
      
      Long minDistance = (vertex.getId().equals(srcId)) ? 0 : Long.MAX_VALUE;
      
      for (Long msg : messages) {
        minDistance = Math.min(minDistance, msg);
      }
      
      if (minDistance < vertex.getValue()) {
        setNewVertexValue(minDistance);
        for (Edge<Long, NullValue> e: getEdges()) {
          sendMessageTo(e.getTarget(), minDistance + 1);//e.getValue()
        }
      }
    }
  }
  
  /**
   * The messages combiner.
   * Out of all messages destined to a target vertex, only the minimum distance is propagated.
   */
  @SuppressWarnings("serial")
  public static final class SSSPCombiner extends MessageCombiner<Long, Long> {
    
    public void combineMessages(MessageIterator<Long> messages) {
      
      long minMessage = Long.MAX_VALUE;
      for (Long msg: messages) {
        minMessage = Math.min(minMessage, msg);
      }
      sendCombinedMessage(minMessage);
    }
  }
  
  // ******************************************************************************************************************
  // UTIL METHODS
  // ******************************************************************************************************************
  
  private static boolean fileOutput = false;
  
  private static Long srcVertexId = 1l;
  
  private static String edgesInputPath = null;
  
  private static String outputPath = null;
  
  private static int maxIterations = 5;
  
  private static boolean parseParameters(String[] args) {
      if(args.length != 4) {
        System.err.println("Usage: PregelSSSP <source vertex id>" +
                           " <input edges path> <output path> <num iterations>");
        return false;
      }
      
      fileOutput = true;
      srcVertexId = Long.parseLong(args[0]);
      edgesInputPath = args[1];
      outputPath = args[2];
      maxIterations = Integer.parseInt(args[3]);
    return true;
  }
  /*
  private static DataSet<Edge<Long, Double>> getEdgesDataSet(ExecutionEnvironment env) {
    if (fileOutput) {
      return env.readCsvFile(edgesInputPath)
      .lineDelimiter("\n")
      .fieldDelimiter("\t")
      .ignoreComments("%")
      .types(Long.class, Long.class, Double.class)
      .map(new Tuple3ToEdgeMap<Long, Double>());
    } else {
      return SingleSourceShortestPathsData.getDefaultEdgeDataSet(env);
    }
  }*/
  
  private static DataSet<Tuple2<Long, Long>> getEdgesDataTupleSet(ExecutionEnvironment env) {
    if (fileOutput) {
      return env.readCsvFile(edgesInputPath)
      .lineDelimiter("\n")
      .fieldDelimiter(" ")
      .ignoreComments("%")
      .types(Long.class, Long.class);
    } else {
      return null;//SingleSourceShortestPathsData.getDefaultEdgeDataSet(env);
    }
  }
  
  @Override
  public String getDescription() {
    return "Vertex-centric Single Source Shortest Paths";
  }
}
