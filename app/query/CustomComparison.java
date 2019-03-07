package query;

import play.*;
import java.util.*;
import utils.*;

//------------------------------------------------------------------------------
public class CustomComparison 
{
	// CustomComparison can be cached for sharing amongst other threads
	private static Map<String, CustomComparison>	mCachedCustomComparison;
	private long mCachedAtTime;
	
	public Selection mSelection1, mSelection2; 
	public String mBasePath1, mBasePath2;
	public int mRefCounts;
	
	//--------------------------------------------------------------------------
	public CustomComparison(int refCountStart, String basePath1, Selection sel1, 
							String basePath2, Selection sel2) {
	
		mRefCounts = refCountStart;
		mBasePath1 = basePath1;
		if (sel1 == null) { // allow null selections, which would typically be the case for a DEFAULT comparison
			mSelection1 = sel2;
		}
		else {
			mSelection1 = sel1;
		}
		mBasePath2 = basePath2;
		if (sel2 == null) { // allow null selections, which would typically be the case for a DEFAULT comparison
			mSelection2 = sel1;
		}
		else {
			mSelection2 = sel2;
		}
	}

	// TODO: should we validate that the height/width are the same?
	//--------------------------------------------------------------------------
	public final int getWidth() {
		if (mSelection1 != null) {
			return mSelection1.getWidth();
		}
		else if (mSelection2 != null) {
			return mSelection2.getWidth();
		}
		return 0;
	}

	// TODO: should we validate that the height/width are the same?
	//--------------------------------------------------------------------------
	public final int getHeight() {
		if (mSelection1 != null) {
			return mSelection1.getHeight();
		}
		else if (mSelection2 != null) {
			return mSelection2.getHeight();
		}
		return 0;
	}
	
	// Returns a cacheStringID, which should be saved and returned to free the comparison...
	//--------------------------------------------------------------------------
	public static final String cacheCustomComparions(CustomComparison theComparison, String clientID) {
		
		if (mCachedCustomComparison == null) {
			mCachedCustomComparison = new HashMap<String, CustomComparison>();
		}
		
		int tryCount = 0;
		while(tryCount < 1000) {
			String comparisonCacheID = RandomString.get(5) + 
						clientID + 
						((tryCount > 0) ? Integer.toString(tryCount) : "");
			if (!mCachedCustomComparison.containsKey(comparisonCacheID)) {
				mCachedCustomComparison.put(comparisonCacheID, theComparison);
				theComparison.mCachedAtTime = System.currentTimeMillis();
				return comparisonCacheID;
			}
			tryCount++;
		}
		
		return null;
	}
	
	//--------------------------------------------------------------------------
	public static final void checkPurgeStaleComparisons() {
		
		if (mCachedCustomComparison == null) {
			return;
		}

		// giving 5 minutes 		
		long expireHours = 0 * 10 * 60 * 1000; // 0 hour -> minutes -> seconds -> milliseconds
		long roughlyNow = System.currentTimeMillis();
		for (Map.Entry<String, CustomComparison> entry : mCachedCustomComparison.entrySet()) {
			Logger.warn("Have possibly stale comparison objects hanging around...");
			CustomComparison value = entry.getValue();
			if (roughlyNow - value.mCachedAtTime > expireHours) {
				Logger.error("Error - removing potentially stale customComparison. " +
					"Anything caching a customComparison should be remove cached customComparison when " +
					"done using that customComparison!");
				String key = entry.getKey();
				releaseCachedComparison(key);
			}
		}
	}
	
	//--------------------------------------------------------------------------
	public static final CustomComparison getCachedComparison(String cacheStringID) {
		
		if (mCachedCustomComparison == null) {
			Logger.info("Attempting to fetch a comparison but the cache has not been initialized!");
			return null;
		}
		CustomComparison res = mCachedCustomComparison.get(cacheStringID);
		if (res == null) {
			Logger.info("Attempting to fetch comparison named <" + cacheStringID + 
							"> but that does not appear to be cached");
			return null;
		}
		
		return res;
	}
	
	//--------------------------------------------------------------------------
	public static final void releaseCachedComparison(String cacheStringID) {
		
		if (mCachedCustomComparison == null) {
			Logger.info("Attempting to uncache a comparison but the cache has not been initialized!");
			return;
		}
		
		CustomComparison res = mCachedCustomComparison.get(cacheStringID);
		if (res == null) {
			Logger.info("Attempting to uncache comparison named <" + cacheStringID + 
							"> but that does not appear to be cached");
			return;
		}
		res.mRefCounts--;
		if(res.mRefCounts > 0) {
			return;
		}
		
		Logger.info(" - releasing cache for comparison, cache string named <" + cacheStringID + ">");
		CustomComparison cc = mCachedCustomComparison.remove(cacheStringID);
		cc.mSelection1 = null;
		cc.mSelection2 = null;
	}
}

