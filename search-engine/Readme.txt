[Compling]
$ cd search-engine
$ mkdir out
$ javac src/model/*.java src/utils/*.java src/indexer/*.java -d out/


[Running]
$ cd out
$ java indexer.Indexer tccorpus.txt index.out



[Alternative way using Shell script]
$ cd search-engine
$ sh index.sh tccorpus.txt index.out
