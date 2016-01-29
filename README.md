Brief explanation about the code

There are six classes:
	- CyActivator
	- MenuAction
	- OptionsMenu
	- RandomizerCore
	- ThreadEngine
	- SimulationAlgorithm

To run a Maven project for Cytoscape i think (if i understood the documentation properly) that we need two basic classes which are CyActivator and MenuAction.

From here i added a RandomizerCore, since Giovanni is used to start a core class i decided to follow his structure, which creates the OptionsMenu. From here it is possible to add all the buttons we require. This means that, for each algorithm (ER, WS, BA etc) you could add a button which launch the imlemented model by using a thread. The class ThreadEngine is used to start a thread which basically performs the algorithm.
