#!/bin/sh

[ -d bin ] || mkdir bin
javac -d bin -cp src src/dcpu/AssemblerTest.java
java -cp bin dcpu.AssemblerTest
