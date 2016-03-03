# NetBlox readme
NetBlox is an extendible Java-based tool for studying networks and communities (grouped network nodes), both overlapping and not, and exploring the algorithms that work with them. As a system it is designed to deal with a number of tasks:
* generation of parametrised graphs (or rather graph provision: the user can upload already existing graphs to run computations on them);
* graph mining;
* evaluation of numeric characteristics for a graph, groups of its nodes or minining algorithms results;
* gathering performance statistics for graph mining algorithms;
* plotting the computed and gathered statistics (numeric characteristics);
* graphs visualisation (with or without communities).

All graph algorithms (graph generation/provision and mining, characterictis computation) are implemented as plug-ins, which are the basic unit of system extendibility, and new algorithms can be easily added to the system (see [User's guide](https://github.com/ispras/NetBlox/wiki/User's-guide) in wiki). The specific tasks to run are specified via scenario files.

This repository contains the source code for NetBlox system (host module) along with necessary configuration, schema and resources files. It does not contain now the external libraries required by the system or plug-ins. The source code for some of NetBlox plug-ins is provided in [NetBlox plug-ins](https://github.com/ispras/NetBlox-plug-ins) repository.

## Running NetBlox
To run NetBlox you need to download the source code from here and compile it on your machine. NetBlox is designed to be easily extendible and is based on Eclipse RCP as plug-ins framework, so we recommend you to build it in Eclipse IDE which will help you to add necessary RCP libs (we will provide a building script independent from IDE later). NetBlox also requires a number of apache and graphic libraries that are freely distributed by corresponding companies:
* commons-configuration-1.9.jar
* commons-lang-2.6.jar
* commons-logging-1.1.1.jar
* gephi-toolkit.jar
* gephi-layout-plugin-circularlayout.jar
* jcommon-1.0.23.jar
* jfreechart-1.0.19.jar

The source code for some of NetBlox plug-ins can be downloaded from [NetBlox plug-ins](https://github.com/ispras/NetBlox-plug-ins) (see more details about specific plug-ins in their documentation).

NetBlox can be launched both right from Eclipse IDE and as a standalone application (after it is compiled). See more details in [User's guide](https://github.com/ispras/NetBlox/wiki/User's-guide) in wiki.

## License
NetBlox is released under Apache 2.0 license. The file with license text is added to the repository.

## Questions
If you have some questions considering NetBlox, please feel free to ask us!
