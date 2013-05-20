import java.util.LinkedList;

import org.apache.log4j.Logger;

/* Work Queue Implementation
 *
 * Source:
 * http://www.ibm.com/developerworks/library/j-jtp0730.html
 */

public class WorkQueue
{
    private final int nThreads;
    private final PoolWorker[] threads;
    private final LinkedList<Runnable> queue;
	private Logger log = Logger.getLogger(WebCrawler.class.getName());
    
    public WorkQueue(int nThreads)
    {
        this.nThreads = nThreads;
        queue = new LinkedList<Runnable>();
        threads = new PoolWorker[this.nThreads];
        
        for (int i=0; i<nThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].start();
        }
    }

    public void execute(Runnable r) {
        synchronized(queue) {
            queue.addLast(r);
            queue.notify();
        }
    }
    
    public synchronized void stopWorkers() throws InterruptedException
    {
       	for(Thread t : threads)
       	{
       		t.interrupt(); 
       		t.join();
       	}
     }

    private class PoolWorker extends Thread {
        public void run() {
            Runnable r;

            while (true) {
                synchronized(queue) {
                    while (queue.isEmpty()) {
                        try
                        {
                        	log.debug("Thread waiting");
                            queue.wait();
                        }
                        catch (InterruptedException ignored)
                        {
                           log.debug(Thread.currentThread().getName() + " stopping work");
                           return;
                        }
                    }
                    
                    r = queue.removeFirst();
                }

                // If we don't catch RuntimeException, 
                // the pool could leak threads
                try {
                    r.run();
                }
                catch (RuntimeException e) {
                    // You might want to log something here
                }
            }
            
        }
    }
}
