# sextante-extension
a collection plugins providing [sextante functionality](https://ojwiki.soldin.de/index.php?title=Sextante) for OpenJUMP. 

## multimodule maven project

This project is made of 4 modules

### sextante-math

Support library containing code for statistics

Only depends on jama 1.0.3 (jsi library is a dependency of sextante-algorithm)

### sextante-core

Core Sextante library refactored to support locationtech version of JTS.
It is renamed 2.0.0-SNAPSHOT version.

sextante-core has the following dependencies :
- jts 1.18.1 (provided by OJ2)
- freechart 1.5.3
- kxml2 2.3.0
- sextante-math

### sextante-gui

GUI components of Sextante.

It depends on :
- freechart 1.5.3 (provided by sextante-core via OJ2)
- bsh 2.0b5 (provided by sextante-core via OJ2)
- table-layout 4.3.0
- japura-gui 7.5.2
- jgraph 5.13.0.0
- sextante-core

### sextante-algorithm

Algorithms for Sextante.

Depends on
- table-layout 4.3.0 (provided by sextante-core via OJ2)
- jep 2.4.2
- jsi 1.0.0
- gishur_core 2.5
- gishur_x 2.5
- sextante-core 
- sextante-gui


## development

### mini-howto

1. **clone** [OpenJUMP2(OJ2) repo](https://github.com/openjump-gis/openjump) and **import** as a Maven project
2. **clone** this repo and **import** as a Maven project
3. make sure the OpenJUMP dependency version in this project's `pom.xml` matches the OpenJUMP checkout. if not switch OpenJUMP branch or temporarily replace dependency with the OJ2 project.
3. run at least maven goal **compile** succesfully on both projects
4. create a **java run configuration** in the OpenJUMP project with
  - java8 or later compatibility
  - vm arguments `-Djava.system.class.loader=com.vividsolutions.jump.workbench.plugin.PlugInClassLoader`
  - program arguments `-default-plugins scripts\default-plugins.xml -I18n en_DE -v debug -extensions-directory ${workspace_loc:sextante-math}/target/classes  -jars-directory ${workspace_loc:sextante-math}/target/libs -extensions-directory ${workspace_loc:sextante-core}/target/classes  -jars-directory ${workspace_loc:sextante-core}/target/libs -extensions-directory ${workspace_loc:sextante-gui}/target/classes  -jars-directory ${workspace_loc:sextante-gui}/target/libs -extensions-directory ${workspace_loc:sextante-algorithm}/target/classes  -jars-directory ${workspace_loc:sextante-algorithm}/target/libs  -jars-directory ${workspace_loc:sextante-openjump}/target/libs -limit-ext-lookup false -extensions-directory ${workspace_loc:sextante-openjump}/target/classes`
  - NOTE: default-plugins, i18n, verbosity are suggested values but can be adopted to your specific needs
5. run/debug the configuration

**NOTE:** the above is an example for the Eclipse IDE. your preferred software may have another way of defining placeholders. be aware and adapt accordingly.

## create a release

1. run against the OJ2 project version you want to release for (usually a release version or <version>-main-SNAPSHOT)
  - check for problems, fix ...
2. run maven goal **package** creates a `distro.zip` and checksum files in `sextante-openjump/target/`
