//
//  main.cpp
//  PageRank
//
//  Created by Simon Grätzer on 18.02.17.
//  Copyright © 2017 Simon Grätzer. All rights reserved.
//


#include <algorithm>
#include <fstream>
#include <iostream>
#include <vector>
#include <sstream>
#include <chrono>

struct Edge {
  size_t vertex1;
  size_t vertex2;
  Edge(size_t v1, size_t v2) : vertex1(v1), vertex2(v2) {}
};

bool edge_comp(Edge const& i, Edge const& j) {
  return i.vertex1 <  j.vertex1;
}

int main(int argc, char* argv[]) {
  if (argc < 3) {
    std::cout << "Usage: " << argv[0] << " <edge file>  [<out>]";
    return 1;
  }
  
  std::vector<Edge> edges;
  std::vector<float> a;
  std::vector<float> b;  //
  std::vector<float> d;  // degree
  
  auto t1 = std::chrono::high_resolution_clock::now();
  
  // parse the data
  std::ifstream file;
  file.open(argv[1]);
  //file.open("/Users/simon/Downloads/soc-pokec-relationships.txt");
  
  std::string line = "";
  while (std::getline(file, line)) {
    std::stringstream linestream(line);
    std::string vertex1, vertex2;
    std::getline(linestream, vertex1, '\t');
    std::getline(linestream, vertex2, '\t');

    edges.emplace_back(std::stoi(vertex1) - 1, std::stoi(vertex2) - 1);
  }
  std::sort(edges.begin(), edges.begin(), edge_comp);
  file.close();
  
  auto t2 = std::chrono::high_resolution_clock::now();
  auto duration = std::chrono::duration_cast<std::chrono::seconds>(t2 - t1).count();
  std::cout << "Loading time " << duration << "\n";;
  
  size_t vCount = edges.back().vertex1 + 1;
  if (edges.back().vertex1 < 1 || edges[0].vertex1 != 0) {
    std::cout << "WTF?!";
    return 1;
  }
  
  a.resize(vCount);
  std::fill(a.begin(), a.end(), 1.0 / vCount);
  b.resize(vCount);
  std::fill(b.begin(), b.end(), 0);
  d.resize(vCount);
  std::fill(d.begin(), d.end(), 0);
  for (Edge const& e : edges) {
    d[e.vertex1] += 1;
  }
  
  for (int i = 0; i < 20; i++) {
    for (size_t x = 0; x < vCount; x++) {
      b[x] = (0.85 * a[x] + 0.15 / vCount) / d[x];
      a[x] = 0.15 / vCount;// 1f32 - alpha;
    }
    for (Edge const& e : edges) {
      a[e.vertex2] += b[e.vertex1];
    }
  }
  
  auto t3 = std::chrono::high_resolution_clock::now();
  duration = std::chrono::duration_cast<std::chrono::seconds>(t3 - t2).count();
  std::cout << "Computation time " << duration << "\n";;
  
  std::ofstream out;
  out.open(argv[2]);
  for (size_t x = 0; x < vCount; x++) {
    out << (x + 1) << "\t" << a[x] << "\n";
  }
  out.close();
  
  t2 = std::chrono::high_resolution_clock::now();
  duration = std::chrono::duration_cast<std::chrono::seconds>(t2 - t1).count();
  std::cout << "Total " << duration << "\n";;
}
