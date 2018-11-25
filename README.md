# logs-reader


## How to start the app:
Run command:
```
gradle clean fatjar
```
Start created jar in the created directory `build/libs` using command:
```
java -jar JAR_NAME LOGS_FILE_PATH
```
For example:
```
java -jar logs-reader-1.0.jar "C:/Logs/logs.txt"
```
If you want to start the jar from different directory then you have to first start the database in directory `build\resources\main\hsqldb\lib` using command:
```
java -cp hsqldb.jar org.hsqldb.server.Server --database.0 file:DB_DIRECTORY/mydb --dbname.0 Test
```
e.g.
```
java -cp hsqldb.jar org.hsqldb.server.Server --database.0 file:C:/Logs/mydb --dbname.0 Test
```

## TODO list:
- Save last read line location and start from this point next time
- Optimize parameters like `BATCHES_QUEUE_SIZE` or `BUFFER_SIZE`
