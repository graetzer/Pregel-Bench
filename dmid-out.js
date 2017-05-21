/**
 * (C) 2017 Simon Grätzer
 * 
 * Export script to convert graphs into the DMID modularity format.
 * Will export community cover too, if available
 */


var fs = require("fs");
var db = require("internal").db;
//var graph_module = require("@arangodb/general-graph");


function exportGraph(vertices, edges, file1, file2) {
  var vc = db[vertices];
  var ec = db[edges];
  if (!vc || !ec) {
    throw "collection not found";
  }
  var cursor = vc.all();
  while (cursor.hasNext()) {
    var v = cursor.next();
    var communities = "[" + v._key + ",";
    if (typeof v.result === 'object') {
      communities += JSON.stringify(v.result);
    } else {
      communities += "[[" + v.result + ",1]]";
    }
    communities += "]\n";
    fs.append(file2, communities);

    var connections = "[" + v._key + ",[";
    var edges = ec.outEdges(v._id);
    for (i = 0; i < edges.length; i++) {
      var to = edges[i]._to;
      to = to.substr(to.indexOf("/")+1);
      connections += "[" + to + ",1]";
      if (i < edges.length-1) {
        connections += ",";
      }
    }
    connections += "]]\n";
    fs.append(file1, connections);
  }
}

module.exports = exportGraph;