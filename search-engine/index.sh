#!/usr/bin/env bash
javac src/model/*.java src/utils/*.java  src/indexer/*.java -d out/
cd out
java indexer.Indexer ../$1 ../$2