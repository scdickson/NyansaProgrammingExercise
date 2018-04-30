import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.Executors;
import java.util.Deque;
import java.util.LinkedList;

public class ProducerConsumer {
    private ReentrantLock lock;
    private Condition cond;
    private Deque<String> queue;

    public ProducerConsumer() {
        lock = new ReentrantLock();
        cond = lock.newCondition();
        queue = new LinkedList<>();
        ExecutorService exec = Executors.newFixedThreadPool(8);
        
        Runnable consumer = () -> {
            while(true) {
                lock.lock();
                try {
                    while (queue.isEmpty()) {
                        cond.await();
                    }
                    String item = queue.removeFirst();
                    System.out.println("REMOVE " + item);
                }
                catch(Exception e){e.printStackTrace();}
                finally {
                    lock.unlock();
                }
            }
        };
        
        Runnable producer = () -> {
            while(true) {
                lock.lock();
                try {
                    String item = String.valueOf(System.currentTimeMillis());
                    queue.addLast(item);
                    System.out.println("ADD " + item);
                }
                catch(Exception e){e.printStackTrace();}
                finally {
                    cond.signal();
                    lock.unlock();     
                }
            }
        };
        
        exec.submit(producer);
        exec.submit(consumer);
        exec.submit(consumer);
        exec.submit(consumer);
        exec.submit(consumer);
    }

    public static void main(String args[]) {
        ProducerConsumer pc = new ProducerConsumer();
    }
}