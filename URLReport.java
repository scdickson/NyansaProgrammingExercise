//Sam Dickson, 2018
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Scanner;

import java.time.LocalDate;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.io.FileInputStream;

public class URLReport {

    /* Constants */
    public static final boolean SORT_DESCENDING = true; //Specify whether or not to sort URLs by hit count descending or ascending
    public static final String TIME_ZONE = "GMT"; //Time zone to use for generating daily report. See https://docs.oracle.com/javase/8/docs/api/java/time/ZoneId.html for list of valid zones.
    public static final String DATE_FORMAT = "MM/dd/yyyy"; //Date format for printing dates in daily report

    /*
        Comparator for sorting URL objects.
        URLs are compared by hit count in DESCENDING order. If the hit counts are equal for two URLs, the URL strings are compared.
    */
    class URLComparatorDescending implements Comparator<URL> {
        public int compare(URL a, URL b) {
            if(a.hitCount < b.hitCount) return 1;
            else if(a.hitCount > b.hitCount) return -1;
            return a.urlString.compareTo(b.urlString);
        }
    }

    /*
        Comparator for sorting URL objects.
        URLs are compared by hit count in ASCENDING order. If the hit counts are equal for two URLs, the URL strings are compared.
    */
    class URLComparatorAscending implements Comparator<URL> {
        public int compare(URL a, URL b) {
            if(a.hitCount < b.hitCount) return -1;
            else if(a.hitCount > b.hitCount) return 1;
            return a.urlString.compareTo(b.urlString);
        }
    }

    /*
        Class to represent a URL object. Contains a string and a hit count. 
        URLs are compared without a leading "http://" or "www."
        URL objects contain:
            -a String representing a web url
            -a normalized web url for comparison
            -a hit count
    */
    class URL {
        public String urlString;
        public String normURLString;
        public int hitCount;

        /* Constructor. Takes a URL as a String. */
        public URL(String urlString) {
            this.urlString = urlString;
            this.normURLString = normalizeURL(urlString);
            this.hitCount = 1;
        }

        /* Normalizes URL by removing leading "http://" and "www.". Used to compare URL objects. */
        private String normalizeURL(String urlString) {
            urlString = urlString.toLowerCase();
            if(urlString.startsWith("http://")) {
                urlString = urlString.substring(8);
            }
            if(urlString.startsWith("www.")) {
                urlString = urlString.substring(5);
            }
            return urlString;
        }

        /* Returns true if two URL objects are equal, false otherwise. */
        public boolean equals(Object other) {
            if(other instanceof URL) {
                URL otherURL = (URL) other;
                return normURLString.equals(otherURL.normURLString);
            }
            return false;
        }
    }

    /*
        Class to represent a node in a sorted doubly linked list. This ensures dates are sorted.
        DateNode objects contain:
            -a LocalDate
            -a Map of (URL String -> URL object)
            -a next DateNode object
            -a previous DateNode object
    */
    class DateNode implements Comparable<DateNode> {
        public LocalDate date;
        public HashMap<String, URL> urlMap; //A Map containing a mapping of url String -> URL objects
        public DateNode _prev, _next;

        /* Constructor. Takes a LocalDate object. */
        public DateNode(LocalDate date) {
            this.date = date;
            urlMap = new HashMap<>();
        }

        /* DateNodes are compared by their LocalDate objects. */
        public int compareTo(DateNode other) {
            return date.compareTo(other.date);
        }

        /*  Iterates over the urlMap entry set and inserts each URL object into a Priority Queue based on hit count using
            a specified comparator. Each URL object is then removed from the root of the Priority Queue and printed. This
            ensures each URL is printed in some order based on hit count.
         */
        public void printURLStats() {
            PriorityQueue<URL> pq = new PriorityQueue<>(
                urlMap.size(),
                SORT_DESCENDING ? (new URLComparatorDescending()) : (new URLComparatorAscending())
            );
            urlMap.forEach((k,v) -> pq.offer(v));
            while(!pq.isEmpty()) {
                URL url = pq.poll();
                System.out.println(String.format("%s %d", url.urlString, url.hitCount));
            }
        }

        /* Increments a URL object's hit count if it exists in the urlMap. Otherwise, a new URL object is added to the urlMap. */
        public void setOrIncrementURL(String urlString) {
            if(urlMap.containsKey(urlString)) {
                URL url = urlMap.get(urlString);
                url.hitCount += 1;
            }
            else {
                URL url = new URL(urlString);
                urlMap.put(urlString, url);
            }
        }
    }

    /* URLReport instance variables */
    private HashMap<LocalDate, DateNode> dateMap; //Map containing Date -> DateNode values. Used for O(1) lookup of dates in our sorted linked list of dates.
    private DateNode head; //Head our sorted doubly linked list of DateNodes.
    private DateTimeFormatter dateFormatter; //Formatter for printing dates in daily report.

    /* Constructor. Takes no arguments. */
    public URLReport() {
        dateMap = new HashMap<>();
        dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
    }

    /* Method to print usage if no arguments are given to the program. */
    public static void printUsage() {
        System.err.println("Usage: 'java URLReport INPUT_FILE'\nExample: java URLReport input.txt");
    }

    /* Prints out each date in the sorted Linked List of dates and the sorted list of URLs under each date. */
    public void printDateStats() {
        DateNode tmp = head;
        while(tmp != null) {
            System.out.println(String.format("%s %s", tmp.date.format(dateFormatter), TIME_ZONE));
            tmp.printURLStats();
            tmp = tmp._next;
        }
    } 

    /*  Parses a line from the input file. Creates a new DateNode object if one doesn't already exist and
        inserts it into a sorted doubly linked list. If a DateNode already exists for a given date, it is 
        retrieved in O(1) time from the dateMap. Information for the given URL is updated in the HashMap
        contained in the DateNode object. Returns true if a URL was successfully updated and false 
        otherwise (in case of incorrect formatting in the input file).
     */
    public boolean addURL(String inputLine) {
        try {
            String[] data = inputLine.split("\\|");
            long timestamp = Long.parseLong(data[0].trim());
            String urlString = data[1].trim();
            LocalDate date = normalizeDate(timestamp);
            DateNode dateNode = null;
            if(dateMap.containsKey(date)) {
                dateNode = dateMap.get(date);
                dateNode.setOrIncrementURL(urlString);
            }
            else {
                dateNode = new DateNode(date);
                dateNode.setOrIncrementURL(urlString);
                insertDateNodeOrdered(dateNode);
                dateMap.put(date, dateNode);
            }
        }
        catch(Exception e) { //If anything goes wrong, return false
            return false;
        }
        return true;
    }

    /*  Normalizes a unix timestamp by converting it to a LocalDate object at a specified timezone. 
        Using a LocalDate object means we aren't concerned with the time component of the given timestamp.
        LocalDates only store day, month, and year.
    */
    private LocalDate normalizeDate(long timestamp) {
        return Instant.ofEpochMilli(timestamp*1000).atZone(ZoneId.of(TIME_ZONE)).toLocalDate();
    }

    /*  Inserts a new DateNode object in a doubly linked list sorted by date. */
    private void insertDateNodeOrdered(DateNode dateNode) {
        if(head == null) {
            head = dateNode;
        }
        else if(head.compareTo(dateNode) > 0) {
            head._prev = dateNode;
            dateNode._next = head;
            head = dateNode;
        }
        else {
            DateNode tmp = head;
            while(tmp._next != null && tmp._next.compareTo(dateNode) < 0) {
                    tmp = tmp._next;
            }
            dateNode._next = tmp._next;
            if(tmp._next != null) {
                dateNode._next._prev = dateNode;
            }
            tmp._next = dateNode;
            dateNode._prev = tmp;
        }
    }

    /*  Main method. Reads each line from an input file using a Scanner, meaning we don't have to store the
        entire contents of the file at once. For each line, we parse the URL and date and store them in our
        data structures. Once the entire file is processed, stats for each date are printed using a helper method.
    */
    public static void main(String args[]) {
        if(args.length != 1) {
            printUsage();
            System.exit(1);
        }
        String inputFileName = args[0];
        FileInputStream inputStream = null;
        Scanner in = null;
        URLReport report = new URLReport();

        try {
            inputStream = new FileInputStream(inputFileName);
            in = new Scanner(inputStream);
            while(in.hasNextLine()) {
                String line = in.nextLine();
                if(!report.addURL(line)) {
                    System.err.println("Invalid data format for \"" + line + "\". Ignoring."); //Ignore improperly formatted lines and move on.
                }
            }
        }
        catch(Exception e) {
            System.err.println("Error reading file: " + e.getMessage()); //If the file is missing or something terrible happened, let the user know.
        }
        finally { //Even if something goes wrong, make sure to close our scanner and input stream.
            try {
                if(inputStream != null) {
                    inputStream.close();
                }
                if (in != null) {
                    in.close();
                }
            }
            catch(Exception e) {
                System.err.println("Error closing file IO: " + e.getMessage());
            }
        }

        report.printDateStats();
    }
}