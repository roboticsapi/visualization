Robotics API Visualization
==========================

This repository contains the Robotics API Visualization, a JavaFX application to visualize 
robots controlled by the Robotics API.

This branch is meant for use with `Oracle Java SE 8`, and will not work with Java 11 or higher 
because Oracle no loger ships JavaFX along with its Java Runtime Environment.

Use the branch `modular-java` for a version compatible with Java 11+.


Background: Robotics API - "Robotics as an API in a modern programming language"
--------------------------------------------------------------------------------

With the Robotics API, you can program complex, real-time critical tasks for your (industrial) robots in Java. 
A convenient, flexible programming model enables you to develop sophisticated applications easy and fast. 
The innovative software architecture of the Robotics API Development Platform ensures reliable execution 
of robotic applications and guarantees exact timing of critical operations.

Using Java as a robot language instead of vendor-specific languages has various advantages. 
You will profit from a modern, object-oriented programming language, hundreds of existing libraries and 
elaborate Integrated Development Environments. Development of the Java ecosystem is actively driven by a 
large community, ensuring that the language will evolve in the future.

The Robotics API Development Platform consists of two separate architectural tiers: Applications are running 
on top of the Java-based Robotics API, while real-time critical device control is done by a Robot Control Core (RCC). 
The RCC has to run on a real-time capable operating system which can communicate with all hardware devices via 
the respective buses and protocols. Robotics API applications can run on any Java-capable operating system on any 
hardware, as long as it can communicate with the RCC via network. For simulation purposes, and to control devices 
that do not require hard real-time control, a Java implementation of a Robot Control Core is bundled with the Robotics API. 

For more details, visit https://www.roboticsapi.org/


License
-------
The Robotics API Visualization is licensed under the Mozilla Public License (MPL) v2.
It uses the JavaFX 3D Model Importer for COLLADA by Java InteractiveMesh, http://www.interactivemesh.org