# A set of programming exercises given by Nyansa.

## URL Report
###### Usage Instructions
To use this program, compile with `javac URLReport.java` and run with `java URLReport`.
###### Complexity Analysis
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