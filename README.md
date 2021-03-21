# sextante-extension
OpenJUMP extension for Sextante plugins

This project is made of 4 modules

## sextante-math

Support library containing code for statistics

Only depends on jama 1.0.3 (jsi library is a dependency of sextante-algorithm)

## sextante-core

Core Sextante library refactored to support locationtech version of JTS.
It is renamed 2.0.0-SNAPSHOT version.

sextante-core has the following dependencies :
- jts 1.18.1
- freechart 1.5.3
- kxml2 2.3.0
- sextante-math

## sextante-gui

GUI components of Sextante. It depends on :
- freechart 1.5.3
- table-layout 4.3.0
- bsh 2.0b5 (make it identical to OpenJUMP version)
- japura-gui 7.5.2
- jgraph 5.13.0.0
- sextante-core

## sextante-algorithm

Algorithms for Sextante. Depends on
- table-layout 4.3.0
- jep 2.4.2
- jsi 1.0.0
- gishur_core 2.5
- gishur_x 2.5
- sextante-core 
- sextante-gui


