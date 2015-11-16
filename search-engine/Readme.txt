[Compling]
$ cd search-engine
$ mkdir out
$ javac src/model/*.java src/utils/*.java src/indexer/*.java src/ranker/*.java -d out/


[Running]
$ cd out
$ java indexer.Indexer tccorpus.txt index.out   // for indexing
$ java ranker.BM25Ranker index.out queries.txt 100   // for ranking



[Alternative way using Shell script]
$ cd search-engine
$ sh index.sh tccorpus.txt index.out   // indexing
$ sh bm25.sh index.out queries.txt 100 >  results.eval  // ranking