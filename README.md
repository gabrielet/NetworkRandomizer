## Network Randomizer
Network Randomizer is a Cytoscape app for generating random networks, as well as randomizing the existing ones, by using multiple random network models. Further, it can process the statistical information gained from the these networks in order to pinpoint their  special, non-random characteristics. 

It covers many popular random network models: Erdős–Rényi, Watts–Strogatz, Barabási–Albert, Community Affiliation Graph, edge shuffle, degree preserving edge shuffle, but it also features a new model which is based on the node multiplication. 

The statistical module is based on the two-sample Kolmogorov-Smirnov test. It compares random and real networks finding the differences between them and thus providing insight into non-random processes upon which real networks are built.

The app was developed by [Gabriele Tosadori](https://github.com/gabrielet) and [Ivan Bestvina](https://github.com/ibestvina), as a project for the [National Resource for Network Biology](http://nrnb.org/).

The paper is available at: https://doi.org/10.12688/f1000research.9203.3

license: [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/)

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
