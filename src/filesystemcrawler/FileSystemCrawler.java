package filesystemcrawler;

import java.io.File;
import java.util.ArrayList;


/**
 * @author Rahul
 * FileSystemCrawler
 */
public class FileSystemCrawler {
	private WorkQueue workQ;
	static int i = 0;

	private class Worker implements Runnable {

		private WorkQueue queue;

		public Worker(WorkQueue q) {
			queue = q;
		}

		//  since main thread has placed all directories into the workQ, we
		//  know that all of them are legal directories; therefore, do not need
		//  to try ... catch in the while loop below

		public void run() {
			String name;
			while ((name = queue.remove()) != null) {
				File file = new File(name);
				String entries[] = file.list();
				if (entries == null)
					continue;
				for (String entry : entries) {
					if (entry.compareTo(".") == 0)
						continue;
					if (entry.compareTo("..") == 0)
						continue;
					String fn = name + "\\" + entry;
					System.out.println(fn);
				}
			}
		}
	}

	public FileSystemCrawler() {
		workQ = new WorkQueue();
	}

	public Worker createWorker() {
		return new Worker(workQ);
	}


	// need try ... catch below in case the directory is not legal

	public void processDirectory(String dir) {
		try {
			File file = new File(dir);
			
			if (file.isDirectory()) {
				String entries[] = file.list();
				if (entries != null)
					workQ.add(dir);

				for (String entry : entries) {
					String subdir;
					if (entry.compareTo(".") == 0)
						continue;
					if (entry.compareTo("..") == 0)
						continue;
					if (dir.endsWith("\\"))
						subdir = dir+entry;
					else
						subdir = dir+"\\"+entry;
					processDirectory(subdir);
				}
			}
		} catch(Exception e) {
			
		}
	}

	/*
	 * Execution starts from here.
	 */
	public static void main(String Args[]) {

		FileSystemCrawler fc = new FileSystemCrawler();

		//  now start all of the worker threads (5)
		int N = 5;
		ArrayList<Thread> thread = new ArrayList<Thread>(N);
		for (int i = 0; i < N; i++) {
			Thread t = new Thread(fc.createWorker());
			thread.add(t);
			t.start();
		}

		//  now place each directory into the workQ

		if(Args == null || Args.length == 0) {
			System.out.println("Please pass the directory to crawl.");
		} else {
			fc.processDirectory(Args[0]);
		}

		//  indicate that there are no more directories to add

		fc.workQ.finish();

		for (int i = 0; i < N; i++){
			try {
				thread.get(i).join();
			} catch (Exception e) {};
		}
	}
}