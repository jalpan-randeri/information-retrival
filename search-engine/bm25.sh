#!/usr/bin/env bash
javac src/model/*.java src/utils/*.java src/indexer/*.java src/ranker/*.java -d out/
cd out
java ranker.BM25Ranker ../$1 ../$2 $3