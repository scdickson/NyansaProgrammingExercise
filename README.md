# A set of programming exercises given by Nyansa.

## URL Report
###### Usage Instructions
To use this program, compile with `javac URLReport.java` and run with `java URLReport FILE.txt`.
###### Implementation and Complexity Analysis
```
URLs and their corresponding hit counts are stored in a custom URL object.

DateNode objects are used to store a particular date and all the URL objects accessed on 
the particular date. URLs are inserted into a HashMap where the key is the url as a String 
and the value is a URL object. Since we will insert many URLs found in the input file, it is 
important to ensure that insert operations are O(1). This is why a HashMap was chosen as the 
underlying data structure. When we want to print out URL objects by order of their hit count, 
we need to retrieve the objects in the HashMap and sort them. Since this only has to be done 
once when we are finished reading input from the file, it is okay to incur some extra cost. 
For sorting, each URL object is inserted into a PriorityQueue in ascending (or descending) 
order. While the PriorityQueue is not full, each URL is removed and printed. The time 
complexity for this step is O(n log n) since each addition to the heap and removal from 
the heap requires O(log n) time and we are processing n elements.

Since we want to print dates in sorted order as well, we need to make sure the dates are sorted. 
Since the number of unique dates in the file (ignoring time) is far fewer than the number of URLs, 
it is okay to incur an O(n) insert time into our data structure. We will be reusing many of the 
DateNode objects, so we want to make sure that lookup of each DateNode is done in O(1) time. 
I've chosen to use a sorted doubly linked list to order the DateNode objects and a HashMap of 
LocalDate to DateNode objects. The HashMap is used to ensure O(1) retrieval of existing dates 
in our linked list. When we encounter a date we don't have in our HashMap, we incur O(n) cost 
to insert a new DateNode element in the correct position of our linked list.

Overall, processing of our file is done in O(n^2) time. The worst case occurs when every single 
date in the input file is unique (ignoring time), since we have to insert n unique dates into 
our sorted linked list. This might look bad, but it is far more likely that the number of URLs 
for a given date is far greater than the number of unique dates in the file. Printing the daily 
report output is done in O(n log n) time, giving an overall runtime complexity of O(n^2). Space 
complexity is O(n).
```
###### Exercise Instructions (for context)
![instructions](https://github.com/scdickson/NyansaProgrammingExercise/raw/master/Images/url_report_instructions.png)

## Producer Consumer Exercise
###### Usage Instructions
To use this program, compile with `javac ProducerConsumer.java` and run with `java ProducerConsumer`.
###### Bug Description
```
The existing producer/consumer code suffers from deadlock if a context switch occurs during the call to signal in the
producer and then again before the call to await in the consumer.

A simple solution to this problem is to put the call to condition.signal() inside the critical section of the producer.
This way, the producer is guaranteed to hold the lock for the condition when it is called. Consumer threads will have to
wait until the producer thread has released its lock before they can run, but this is better than a deadlock situation.

Additional problems arise in exception handling. If an exception occurs in the producer and consumer threads while the
lock is still held, the lock will never be released, causing deadlock. This is fixed by adding a "finally" block in which
the lock is released even if an exception occurs. Finally, the logic for checking if the queue is empty in the consumer is
incorrect. This should be a while loop until the queue is not empty, in case of a spurious wakeup of a consumer thread occurs
when there are no items on the queue.

See the ProducerConsumer.java file for an implementation of the aforementioned fixes. 
```
###### Exercise Instructions (for context)
![instructions](https://github.com/scdickson/NyansaProgrammingExercise/raw/master/Images/producer_consumer_instructions.png)