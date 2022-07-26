# FacetHDT
Proof of concept to check how performant HDT can be for building navigation steps and facets of an RDF graph. 

The first implementation is not parallelized at all to check the upper limit. Given that the java library to deal with HDT files is considered to be thread-safe (read only operations all the time), the parallelization is in the roadmap. 

The current version includes two type of operations:
  - navigational steps: given a resource, calculate the reachable resources / literals grouped by property. We can setup the direction of the traversal (INCOMING or OUTGOING edges, or BOTH). 
  - facet building: given a resource and a property, calculate all the facets of the set of reachable sources. This allows to perform searches/filter results when dealing with high-degree nodes. 

Note that everything, as we are dealing with HDT, is calculated at distance 1 (just one hop away from the resources being considered at each time). Indeed, the amount of resources to be processed grows exponentially with the number of hops and the degree of the nodes, so a *handle with care* label should be sticked here. 
