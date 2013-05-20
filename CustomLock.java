
/**
 * 
 * This class provides a custom lock
 * 
 * @author Yaoli Zheng
 *
 */
public class CustomLock {
	//numbers of reader
	private int readingReader;
	//numbers of writer
	private int writingWriter; 
	//numbers of thread which is waiting for writing
	private int waitingWriter;
	//tend to write
	private boolean perferWrite;

	public synchronized void acquireReadLock() {
		while (writingWriter > 0 || (perferWrite && waitingWriter > 0)) {
			try {
				wait();
			} catch (InterruptedException ignored) {
			}
		}
		readingReader++;
	}

	public synchronized void acquireWriteLock() {
		waitingWriter++;
		try {
			while (readingReader > 0 || writingWriter > 0) {
				wait();
			}
		} catch (InterruptedException ignored) {
		} finally {
			waitingWriter--;
		}
		writingWriter++;
	}

	public synchronized void releaseReadLock() {
		readingReader--;
		perferWrite = true;
		notifyAll();
	}

	public synchronized void releaseWriteLock() {
		writingWriter--;
		perferWrite = false;
		notifyAll();
	}
}