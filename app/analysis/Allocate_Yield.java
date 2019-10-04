package analysis;

import play.*;
import query.Layer_CDL;
import query.Layer_Integer;
import query.Scenario;
import resources.Farm;
import resources.Farm.Crop;
import java.awt.Point;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.Map.Entry;

//------------------------------------------------------------------------------
public class Allocate_Yield
{
	class FeedBucket {
		public float mFarmDistance; // exists as a short-term sorting helper 
		public float mCornYield,
			mSoyYield,
			mAlfalfaYield,
			mGrassYield;
		
		public List<Integer> mPotentialFarms = null;//new ArrayList<Integer>();
	}
	
	private FeedBucket [][] mAggregatedYield = null;
	private Map<Integer,List<FeedBucket>> mFarmToFeedMap = null;

	private int mGridStride;
	
	private static final boolean SELF_DEBUG_LOGGING = true;
		
	// slot is 0-4, which corresponds to 5 - 12-bit chunks in a 64 bit long
	private final float unpackYield(long lValue, int slot) {
		Long res = (lValue >> (slot * 12)) & 0xfff;
		return res * (25.0f / 4095.0f) 
				* 900.0f / 10000.0f; // and convert from tonnes per hectare to tons per 30x30m cell
	}
	
	// width and height are in original raster dimensions
	// gridStride is some count of original raster cells to aggregate
	//	and is applied to both x and y
	//--------------------------------------------------------------------------
	public void aggregate(Scenario s, long [][] packedYield, int gridStride) {

		int width = s.getWidth(), height = s.getHeight();
		
		int gsW = (int) Math.ceil(width / (double)gridStride);
		int gsH = (int) Math.ceil(height / (double)gridStride);
		
		Layer_Integer cdl = Layer_CDL.get(); 
		int dr = cdl.stringToMask("dairy rotation");
		int cg = cdl.stringToMask("cash grain");
		int [][] rotation = s.mNewRotation;
		
		mAggregatedYield = new FeedBucket[gsH][gsW];
		mGridStride = gridStride;
		for (int y = 0; y < gsH; y++ ) {
			for (int x = 0; x < gsW; x++ ) {
				mAggregatedYield[y][x] = new FeedBucket();
			}
		}
		
		//----------------------------------------------------------------------		
		try {
			for (int y = 0; y < height; y++) {
				int gsY = (int) Math.floor(y / (double)gridStride);
				
				for (int x = 0; x < width; x++) {
					
					long pYield = packedYield[y][x];
					if (pYield < 0) {
						continue;
					}
					int gsX = (int) Math.floor(x / (double)gridStride);
					FeedBucket fb = mAggregatedYield[gsY][gsX];

					// corn
					float yield = unpackYield(pYield, 0);
					if ((rotation[y][x] & cg) > 0) {
						yield *= 0.5f; 
					}
					else if ((rotation[y][x] & dr) > 0) {
						yield *= 0.3333f; 
					}
					fb.mCornYield += yield;
					
					// soy
					yield = unpackYield(pYield, 1);
					if ((rotation[y][x] & cg) > 0) {
						yield *= 0.5f; 
					}
					fb.mSoyYield += yield;
					
					// alfalfa
					yield = unpackYield(pYield, 2);
					if ((rotation[y][x] & dr) > 0) {
						yield *= 0.6667f;
					}
					fb.mAlfalfaYield += yield;
					
					// grass
					fb.mGrassYield += unpackYield(pYield, 3);
				}
			}
		}
		catch(Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			Logger.error(sw.toString());
		}
			
		mFarmToFeedMap = new HashMap<Integer,List<FeedBucket>>();
	}
	
	// NOTE: Point loc is expected to be the cell indexes
	//	in the original 30m raster space.
	// Radius is expected to be the claim radius as a count of 
	//	30m strides, example: 660m = 22 strides at 30m
	//-------------------------------------------------------
	public void stakeClaim(Integer farmId, Point loc, int radius) {
		int gsXmin = (int) Math.floor((loc.x - radius) / (double)mGridStride);
		int gsXmax = (int) Math.ceil((loc.x + radius) / (double)mGridStride);
		int gsYmin = (int) Math.floor((loc.y - radius) / (double)mGridStride);
		int gsYmax = (int) Math.ceil((loc.y + radius) / (double)mGridStride);

		// FIXME: TODO: hardcoded dimensions
		int gsW = (int) Math.ceil(6150 / (double)mGridStride);
		int gsH = (int) Math.ceil(4557 / (double)mGridStride);

		if (gsXmin < 0) gsXmin = 0;
		if (gsYmin < 0) gsYmin = 0;
		if (gsXmax >= gsW) gsXmax = gsW - 1;
		if (gsYmax >= gsH) gsYmax = gsH - 1;

		List<FeedBucket> lfb = new ArrayList<FeedBucket>();
				
		try {
			for (int yy = gsYmin; yy <= gsYmax; yy++) {
				double cellCenterY = (yy + 0.5) * mGridStride;
				
				for (int xx = gsXmin; xx <= gsXmax; xx++) {
	
					double cellCenterX = (xx + 0.5) * mGridStride;
					
					Float dist = (float) loc.distanceSq(cellCenterX, cellCenterY);
					if (dist > (radius + 0.5)*(radius + 0.5)) continue;
					
					FeedBucket fb = mAggregatedYield[yy][xx];
					if (fb.mPotentialFarms == null) {
						fb.mPotentialFarms = new ArrayList<Integer>();
					}
					fb.mPotentialFarms.add(farmId);
					
					fb.mFarmDistance = dist;
					lfb.add(fb);
				}
			}
		}
		catch(Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			Logger.error(sw.toString());
		}
		
		lfb.sort((a, b) -> (int)(a.mFarmDistance - b.mFarmDistance));
			
		mFarmToFeedMap.put(farmId, lfb);		
	}

	// Claims are reordered such that cells closest to the farm base should be 
	//	processed first. This distance sorting is also biased by number of 
	//	conflicts so close + low conflict resources are prioritized
	//-------------------------------------------------------
	public void distribute() {

		// find the size of the largest farms' feed access pool (the one with the most feedbuckets)
		int largest = 0;
		for(Entry<Integer,List<FeedBucket>> e : mFarmToFeedMap.entrySet()) {
			
			List<FeedBucket> lfb = e.getValue();
			largest = Math.max(largest, lfb.size());
		}

		// loop through each farm. Each one gets a shot at the best feedbucket (the one nearest)
		// Once each farm is done with their one shot, we advance to the 2nd best feedbucket.
		//	Given that some farms will have a smaller collection radius, they will stop getting
		//	access to resources at some point...whilst larger collection radius operations will
		//	continue to get processed
		int idx = 0;
		// FIXME: TODO: eventually should exit out but maybe worth checking if it's
		//	possible to 'early-out'?
		while(idx < largest) {
			for(Entry<Integer,List<FeedBucket>> e : mFarmToFeedMap.entrySet()) {
				
				List<FeedBucket> lfb = e.getValue();
				if (idx >= lfb.size()) continue;
				
				FeedBucket f = lfb.get(idx); 
				
				// Allocate Corn 
				//--------------------------------
				f.mPotentialFarms.sort((a, b) -> (int)(Farm.getRemainingNeed(Crop.E_CORN, a) 
						- Farm.getRemainingNeed(Crop.E_CORN, b)));
				
				int sz = f.mPotentialFarms.size();
				for(Integer id: f.mPotentialFarms) {
					double frac = Math.min(
							f.mCornYield / sz, 
							Farm.getRemainingNeed(Crop.E_CORN, id));
					f.mCornYield -= frac;
					Farm.receiveCrop(Crop.E_CORN, id, frac);
					sz--;
				}
				
				// Allocate Soy
				//--------------------------------
				f.mPotentialFarms.sort((a, b) -> (int)(Farm.getRemainingNeed(Crop.E_SOY, a) 
					- Farm.getRemainingNeed(Crop.E_SOY, b)));
				
				sz = f.mPotentialFarms.size();
				for(Integer id: f.mPotentialFarms) {
					double frac = Math.min(
							f.mSoyYield / sz,
							Farm.getRemainingNeed(Crop.E_SOY, id));
					f.mSoyYield -= frac;
					Farm.receiveCrop(Crop.E_SOY, id, frac);
					sz--;
				}
				
				// Allocate Grass
				//--------------------------------
				f.mPotentialFarms.sort((a, b) -> (int)(Farm.getRemainingNeed(Crop.E_GRASS, a) 
					- Farm.getRemainingNeed(Crop.E_GRASS, b)));
				
				sz = f.mPotentialFarms.size();
				for(Integer id: f.mPotentialFarms) {
					double frac = Math.min(
								f.mGrassYield / sz,
								Farm.getRemainingNeed(Crop.E_GRASS, id));
					f.mGrassYield -= frac;
					Farm.receiveCrop(Crop.E_GRASS, id, frac);
					sz--;
				}
				
				// Allocate Alfalfa
				//--------------------------------
				f.mPotentialFarms.sort((a, b) -> (int)(Farm.getRemainingNeed(Crop.E_ALFALFA, a) 
					- Farm.getRemainingNeed(Crop.E_ALFALFA, b)));
				
				sz = f.mPotentialFarms.size();
				for(Integer id: f.mPotentialFarms) {
					double frac = Math.min(
							f.mAlfalfaYield / sz,
							Farm.getRemainingNeed(Crop.E_ALFALFA, id));
					f.mAlfalfaYield -= frac;
					Farm.receiveCrop(Crop.E_ALFALFA, id, frac);
					sz--;
				}
			}
			idx++;
		}
		
	/*	StringBuilder ssb = new StringBuilder();
		float maxx = 0.0f;
		int gsW = (int) Math.ceil(6150 / (double)mGridStride);
		int gsH = (int) Math.ceil(4557 / (double)mGridStride);
		for (int y = 0; y < gsH; y++) {
			for (int x = 0; x < gsW; x++) {
				FeedBucket fb = mAggregatedYield[y][x];
				if (fb.mCornYield > maxx) maxx = fb.mCornYield;
				ssb.append(String.format("{x:%d,y:%d,v:%.2f},", x,y,fb.mCornYield));
			}
		}
		
		Logger.debug(String.format("var maxC = %.3f;", maxx));
		Logger.debug("var cData = [" + ssb.toString() + "]");
	*/		
	}
	
	//-------------------------------------------------------
	public void claimLeftovers(Scenario s) {
		
		int gsW = (int) Math.ceil(s.getWidth() / (double)mGridStride);
		int gsH = (int) Math.ceil(s.getHeight() / (double)mGridStride);
		
		for (int y = 0; y < gsH; y++ ) {
			for (int x = 0; x < gsW; x++ ) {
				FeedBucket fb = mAggregatedYield[y][x];
				
				if (fb.mPotentialFarms == null || fb.mPotentialFarms.size() <= 0) continue;
				
				int sz = fb.mPotentialFarms.size() * 2; // fixme: why * 2??
				for (Integer fid : fb.mPotentialFarms) {
					Farm.receiveExcessCrop(Crop.E_CORN, fid, (double) (fb.mCornYield / sz));
					Farm.receiveExcessCrop(Crop.E_SOY, fid, (double) (fb.mSoyYield / sz));
					Farm.receiveExcessCrop(Crop.E_ALFALFA, fid, (double) (fb.mAlfalfaYield / sz));
					Farm.receiveExcessCrop(Crop.E_GRASS, fid, (double) (fb.mGrassYield / sz));
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------
	@SuppressWarnings("unused")
	private static final void debugLog(String conditionalLog) {
		
		if (SELF_DEBUG_LOGGING) {
			Logger.debug(conditionalLog);
		}
	}
}
