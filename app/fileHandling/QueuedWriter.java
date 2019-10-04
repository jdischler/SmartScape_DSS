package fileHandling;

import play.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.*;
import java.nio.*;
import org.apache.commons.io.FileUtils;

import analysis.ModelResult;
import analysis.ScenarioSetupResult; 

//------------------------------------------------------------------------------
/** 
 * Utility that manages streaming model results to disk as needed
 * 
 * @author jdischler
 *
 */
public class QueuedWriter implements Runnable {

	private static final boolean CONDITIONAL_DEBUG_LOGGING = false;
	private static final void conditionalLog(String conditionalMsg) {
		
		if (CONDITIONAL_DEBUG_LOGGING) {
			Logger.debug(conditionalMsg);
		}
	}

	private static Queue<ScenarioSetupResult> mScenarioSetupsToWrite;
	private static Queue<ModelResult> mResultsToWrite;

	private static Thread mThreadHandle;
	private final static Object mSynchronizationObject = new Object();
	
	private static QueuedWriter mWriter;
	
	// NOTE: setting this to false may cause analysis and heatmapping to break. But if you
	//	just want the server to write ASC (or other formats) for analysis, then that might be ok.
	private static final boolean mbWriteBinary_DSS = true; 
	private static final boolean mbWriteText_ASC = false;
	
	private static final boolean mbWriteLandscape = false; // thinking this is mostly for debugging at this point?
	private static AtomicBoolean mRunning = new AtomicBoolean();
	
	//--------------------------------------------------------------------------
	/** 
	 * Creates a thread that watches for model results and then streams them to disk 
	 * @param none 
	 * @return void
	 */
	//--------------------------------------------------------------------------
	public final static void launchQueuedWriter() {
	
		if (mWriter == null) {
			Logger.info("");
			Logger.info(" ... Creating a new file writer queue ...");
			mWriter = new QueuedWriter();
			
			if (mScenarioSetupsToWrite == null) {
				mScenarioSetupsToWrite = new ConcurrentLinkedQueue<ScenarioSetupResult>();
			}
			if (mResultsToWrite == null) {
				mResultsToWrite = new ConcurrentLinkedQueue<ModelResult>();
			}
			mThreadHandle = new Thread(mWriter,"QueuedWriter");
			conditionalLog(" ...Queued writer priority is: " + Integer.toString(mThreadHandle.getPriority()));
			// Sanity check, seems to almost never happen? 
			if (mThreadHandle.getPriority() > Thread.NORM_PRIORITY) {
				conditionalLog(" ...Thread priority pretty, high, reducing to Normal");
				mThreadHandle.setPriority(Thread.NORM_PRIORITY);
				conditionalLog(" ...Queued writer priority is now: " + Integer.toString(mThreadHandle.getPriority()));
			}
			
			mThreadHandle.start();
		}
	}
	
	//--------------------------------------------------------------------------
	public final static void shutdownQueuedWriter() {
		mRunning.set(false);
		mThreadHandle.interrupt();
	}
	
	//--------------------------------------------------------------------------
	public final static void queueResults(List<ModelResult> results) {
		
		mWriter.queueResultsInternal(results);
	}

	//--------------------------------------------------------------------------
	public final static void queueResults(ScenarioSetupResult result) {
		
		mWriter.queueResultsInternal(result);
	}
	
	// Internal funcs, don't use...
	//--------------------------------------------------------------------------
	private final void queueResultsInternal(List<ModelResult> results) {
		
		conditionalLog(" ... Adding ModelResults to writer queue. Notify queue to wake up.....");
		
		mResultsToWrite.addAll(results);
		synchronized(mSynchronizationObject) {
			mSynchronizationObject.notifyAll();
		}
	}

	// Internal funcs, don't use...
	//--------------------------------------------------------------------------
	private final void queueResultsInternal(ScenarioSetupResult result) {
		
		conditionalLog(" ... Adding ScenarioSetupResult to writer queue. Notify queue to wake up.....");
		mScenarioSetupsToWrite.add(result);
		synchronized(mSynchronizationObject) {
			mSynchronizationObject.notifyAll();
		}
	}
	
	//--------------------------------------------------------------------------
	public final static boolean hasFilesQueued() {
		
		synchronized(mSynchronizationObject) {
			return (mResultsToWrite.size() > 0 || mScenarioSetupsToWrite.size() > 0);
		}
	}
	
	// run can't be made private despite the fact that we really want it for internal use only...
	//--------------------------------------------------------------------------
	@SuppressWarnings("unused")
	public void run() {
		
		mRunning.set(true);
		
		Logger.info(" ... Writer thread is watching the writer queue");
		while (mRunning.get()) {
			
			synchronized(mSynchronizationObject) {
				if (mResultsToWrite.size() == 0 && mScenarioSetupsToWrite.size() == 0) {
					conditionalLog("...Writer queue has no results, waiting for up to 60 minutes...");
					try {
						mSynchronizationObject.wait(60 * 60 * 1000);
					}
					catch(Exception e) {
						Logger.error("Writer queue exception: " + e.toString());
					};
				}
			}
			
			conditionalLog("...Writer queue checking for results");
			ModelResult mr_result = mResultsToWrite.poll();
			if (mr_result != null) {
				File writeFolder = new File("./layerData/" + mr_result.mDestinationFolder + "/");
				if (writeFolder.exists() == false) {
					conditionalLog(" ... Writer queue creating directory: " + writeFolder.toString());
					try {
						FileUtils.forceMkdir(writeFolder);
					}
					catch (Exception err) {
						Logger.info(err.toString());
					}
					//writeFolder.mkdirs();
					if (writeFolder.exists() == false) {
						Logger.error(" Error - Writer queue directory creation failed!!");
					}
				}
				
				// ---WRITE BINARY DSS?
				if (mbWriteBinary_DSS) {
					File writeFile = new File(writeFolder, mr_result.mName + ".dss");
					conditionalLog(" ... Writer queue writing DSS: " + writeFile.toString());
					
					Binary_Writer writer = new Binary_Writer(writeFile, mr_result.mWidth, mr_result.mHeight);
					ByteBuffer writeBuffer = writer.writeHeader();
				
					for (int y=0; y < mr_result.mHeight; y++) {
						for (int x=0; x < mr_result.mWidth; x++) {
							writeBuffer.putFloat(x * 4, mr_result.mRasterData[y][x]);
						}
						writer.writeLine();
					}
					writer.close();
				}
				
				// ---WRITE TEXT ASC?
				if (mbWriteText_ASC) {
					PrintWriter ascOut = null;
					int width = mr_result.mWidth, height = mr_result.mHeight;
					try {
						File writeFile = new File(writeFolder, mr_result.mName + ".asc");
						conditionalLog(" ... Writer queue writing ASC: " + writeFile.toString());
						ascOut = new PrintWriter(new BufferedWriter(new FileWriter(writeFile)));
						ascOut.println("ncols         " + Integer.toString(width));
						ascOut.println("nrows         " + Integer.toString(height));
						ascOut.println("xllcorner     -10062652.65061");
						ascOut.println("yllcorner     5249032.6922889");
						ascOut.println("cellsize      30");
						ascOut.println("NODATA_value  -9999");
					} 
					catch (Exception err) {
						Logger.info(err.toString());
					}
					
					if (ascOut != null) {	
						String stringNoData = Integer.toString(-9999);

						for (int y = 0; y < height; y++) {
							StringBuilder ascLine = new StringBuilder(width * 10); // estimate 10 characters per x-raster
							for (int x=0; x < width; x++) {
								float data = mr_result.mRasterData[y][x];
								if (data > -9999.0f) {
									ascLine.append(data);
								}
								else {
									ascLine.append(stringNoData);
								}
								if (x != width - 1) {
									ascLine.append(" ");
								}
							}
							ascOut.println(ascLine.toString());
						}
						try {
							ascOut.close();
						}
						catch (Exception err) {
							Logger.info(err.toString());
						}
					}
				}
				
				mr_result.mRasterData = null;
			}
			
			ScenarioSetupResult ssr_result = mScenarioSetupsToWrite.poll();
			if (ssr_result != null) {
				
				File writeFolder = new File("./layerData/"+ ssr_result.mDestinationFolder + "/");
				try {
					writeFolder = new File(writeFolder.getCanonicalPath());
				}
				catch (Exception err) {
					Logger.info(err.toString());
				}
				
				if (writeFolder.exists() == false) {
					conditionalLog(" ... Writer queue creating directory: " + writeFolder.toString());
					try {
						FileUtils.forceMkdir(writeFolder);
					}
					catch (Exception err) {
						Logger.info(err.toString());
					}
					if (writeFolder.exists() == false) {
						Logger.error(" Error - Writer queue directory creation failed!!");
					}
				}

				// ---WRITE Selection in Binary --- 
				if (true) {
					File writeFile = new File(writeFolder, "selection.sel");
					conditionalLog(" ... Writer queue writing sel: " + writeFile.toString());
					
					Binary_Writer writer = new Binary_Writer(writeFile, ssr_result.mWidth, ssr_result.mHeight);
					ByteBuffer writeBuffer = writer.writeHeader(1);
				
					for (int y=0; y < ssr_result.mHeight; y++) {
						for (int x=0; x < ssr_result.mWidth; x++) {
							writeBuffer.put(x, ssr_result.mSelectionData[y][x]);
						}
						writer.writeLine();
					}
					writer.close();
					ssr_result.mSelectionData = null;
				}
				
				// ---WRITE BINARY DSS for New Transformed Landscape?
				if (mbWriteBinary_DSS && mbWriteLandscape) {
					File writeFile = new File(writeFolder, "cdl_transformed.dss");
					conditionalLog(" ... Writer queue writing DSS: " + writeFile.toString());
					
					Binary_Writer writer = new Binary_Writer(writeFile, ssr_result.mWidth, ssr_result.mHeight);
					ByteBuffer writeBuffer = writer.writeHeader();
				
					for (int y=0; y < ssr_result.mHeight; y++) {
						for (int x=0; x < ssr_result.mWidth; x++) {
							writeBuffer.putInt(x * 4, ssr_result.mLandscapeData[y][x]);
						}
						writer.writeLine();
					}
					writer.close();
				}
				
				// ---WRITE TEXT ASC for New Transformed Landscape?
				if (mbWriteText_ASC && mbWriteLandscape) {
					PrintWriter ascOut = null;
					int width = ssr_result.mWidth, height = ssr_result.mHeight;
					try {
						File writeFile = new File(writeFolder, "cdl_transformed.asc");
						conditionalLog(" ... Writer queue writing ASC: " + writeFile.toString());
						ascOut = new PrintWriter(new BufferedWriter(new FileWriter(writeFile)));
						ascOut.println("ncols         " + Integer.toString(width));
						ascOut.println("nrows         " + Integer.toString(height));
						ascOut.println("xllcorner     -10062652.65061");
						ascOut.println("yllcorner     5249032.6922889");
						ascOut.println("cellsize      30");
						ascOut.println("NODATA_value  -9999");
					} 
					catch (Exception err) {
						Logger.info(err.toString());
					}
					
					if (ascOut != null) {	
						String stringNoData = Integer.toString(-9999);
				
						for (int y = 0; y < height; y++) {
							StringBuilder ascLine = new StringBuilder(width * 10); // estimate 10 characters per x-raster
							for (int x=0; x < width; x++) {
								int data = ssr_result.mLandscapeData[y][x];
								if (data > -9999) {
									ascLine.append(data);
								}
								else {
									ascLine.append(stringNoData);
								}
								if (x != width - 1) {
									ascLine.append(" ");
								}
							}
							ascOut.println(ascLine.toString());
						}
						try {
							ascOut.close();
						}
						catch (Exception err) {
							Logger.info(err.toString());
						}
					}
				}
				ssr_result.mLandscapeData = null;
			}
		}
	}
}

