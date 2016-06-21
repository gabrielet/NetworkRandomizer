## Network Randomizer
Network Randomizer is a Cytoscape app for generating random networks, as well as randomizing the existing ones, by using multiple random network models. Further, it can process the statistical information gained from the these networks in order to pinpoint their  special, non-random characteristics. 

It covers many popular random network models: Erdős–Rényi, Watts–Strogatz, Barabási–Albert, Community Affiliation Graph, edge shuffle, degree preserving edge shuffle, but it also features a new model which is based on the node multiplication. 

The statistical module is based on the two-sample Kolmogorov-Smirnov test. It compares random and real networks finding the differences between them and thus providing insight into non-random processes upon which real networks are built.


### Brief explanation of the code

There are six main classes:
 - CyActivator - run the application, communicates with the cytoscape
 - RandomizerCore - used as a model of the current Cytoscape state (network handling etc.)
 - MenuAction - app initiation
 - OptionsMenu - user interface
 - ThreadEngine - multithread task handling
 - AbstractModel - abstract random network model which is implemented by all the models
 

To compile a Maven project, all the additional packages need to be pulled. This is automatically done by most IDEs (Eclipse, NetBeans, etc.).

To add a new model, create a child class of the AbstractModel, implement the missing methods, and add the handling code to the OptionsMenu, along with the additional GUI elements.