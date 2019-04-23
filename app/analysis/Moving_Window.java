package analysis;

import query.Layer_Base;
import query.Layer_Integer;

// This class calculate proportion of Ag, forest and grass with the user specified rectangle buffer 
// Inputs are location of cell, window size
// Output proportion of Ag, Forest, Grass, Developed, Water (inc. wetlands)

// It uses a cool idea by Amin to zigzag across the cells, subtracting only the old cells
//	that fall out of the moving window...and adding in the new cells at the leading edge
//	of the moving window.

// Since the z window manages the irregular movement process, it returns the coordinates of
//	where it is at to the caller. The caller must only issue a call to advance to move the window
// Version 08/20/2013

// Simple usage example
/*
Moving_Z_Window zWin = new Moving_Z_Window(windowSize, rasterData, rasterWidth, rasterHeight);

boolean moreCells = true;
while (!moreCells) {
	
	Moving_Z_Window.Z_WindowPoint point = zWin.getPoint();
	float agProp = zWin.getPropAg();
	someDataArray[point.y][point.x] = agProp;
	
	moreCells = zWin.advance();
}
*/

//------------------------------------------------------------------------------
public abstract class Moving_Window
{
	// small helper class.....
	public final class WindowPoint
	{
		public int mX, mY;
		
		public WindowPoint(int x, int y) {
			mX = x;
			mY = y;
		}
	}
	
	//
	protected int[][] mRasterData;
	protected int mRasterWidth, mRasterHeight;
	protected int mHalfWindowSize; // half the size, in cells
	
	// Define working variables
	protected int mUpLeft_X, mUpLeft_Y;
	protected int mLowRight_X, mLowRight_Y;
	
	protected int mTotal; // total data cells in window. NoData cells are NOT counted
	
	protected int mCountAg, mAgMask;
	protected int mCountForest, mForestMask;
	protected int mCountGrass, mGrassMask;
	protected int mCountDeveloped, mDevelopedMask;
	protected int mCountWater, mWaterMask; // NOTE: includes wetlands
	
	protected int mAt_X, mAt_Y;
	protected WindowPoint mPoint;
	
	public Moving_Window(int win_sz, int [][] rasterData, int raster_w, int raster_h) {
		
		mRasterData = rasterData;
		mRasterWidth = raster_w;
		mRasterHeight = raster_h;
		mHalfWindowSize = win_sz / 2;
		
		mAt_X = 0;
		mAt_Y = 0;
		
		Layer_Integer wl = (Layer_Integer)Layer_Base.getLayer("wisc_land");
		
		mGrassMask = wl.stringToMask("hay","pasture","cool-season grass","warm-season grass");	
		mForestMask = wl.stringToMask("coniferous","deciduous","mixed woodland");
		mAgMask = wl.stringToMask("continuous corn","cash grain","dairy rotation","other crops");
		mDevelopedMask = wl.stringToMask("urban","suburban");
		mWaterMask = wl.stringToMask("open water","wetland");
		
		mPoint = new WindowPoint(mAt_X, mAt_Y);
		
		calcWindowBounds();
		initCounts();
	}
	
	//--------------------------------------------------------------------------
	protected void calcWindowBounds() {
		
		updateBoundsMoving_X();
		updateBoundsMoving_Y();
	}
	
	//--------------------------------------------------------------------------
	protected final void updateBoundsMoving_X() {
		
		mUpLeft_X = mAt_X - mHalfWindowSize;
		mLowRight_X = mAt_X + mHalfWindowSize;
		
		if (mUpLeft_X < 0) {
			mUpLeft_X = 0;
		}
		if (mLowRight_X > mRasterWidth - 1) {
			mLowRight_X = mRasterWidth - 1;
		}
	}

	//--------------------------------------------------------------------------
	protected final void updateBoundsMoving_Y() {
		
		mUpLeft_Y = mAt_Y - mHalfWindowSize;
		mLowRight_Y = mAt_Y + mHalfWindowSize;
		
		if (mUpLeft_Y < 0) {
			mUpLeft_Y = 0;
		}
		if (mLowRight_Y > mRasterHeight - 1) {
			mLowRight_Y = mRasterHeight - 1;
		}
	}
	
	// Called internally off the constructor
	//--------------------------------------------------------------------------
	protected void initCounts() {
		
		mTotal = 0;	
		
		for (int y = mUpLeft_Y; y <= mLowRight_Y; y++) {
			for (int x = mUpLeft_X; x <= mLowRight_X; x++) {
				int cellValue = mRasterData[y][x]; 
				if (cellValue != 0) {
					mTotal++;
					
					// Calculate count of land cover in the given moving window
					if ((cellValue & mAgMask) > 0) {
						mCountAg++;	
					}
					else if ((cellValue & mGrassMask) > 0) {
						mCountGrass++;
					}
					else if ((cellValue & mForestMask) > 0) {
						mCountForest++;
					}
					else if ((cellValue & mDevelopedMask) > 0) {
						mCountDeveloped++;
					}
					else if ((cellValue & mWaterMask) > 0) {
						mCountWater++;
					}
				}
			}
		}
	}
	
	//--------------------------------------------------------------------------
	public final WindowPoint getPoint() {
		
		mPoint.mX = mAt_X;
		mPoint.mY = mAt_Y;
		return mPoint;
	}
	
	//--------------------------------------------------------------------------
	public final int getWindowCenterValue() {
		return mRasterData[mAt_Y][mAt_X];
		
	}
	// Each call to run advances one cell in the direction the Z-win is moving in...
	//	since it uses a somewhat irregular pattern to move, this function will
	// 	return a class with the X, Y coordinates for where the Z_Window is at...
	// Returns FALSE the Z_Window is finished processing all cells in the raster...
	//--------------------------------------------------------------------------
	public abstract boolean advance();
	
	// If total cells is zero, there is no reasonable proportion we can calculate,
	//	we should check this before trying to get proportions. If false, should probably
	//	put NoData in resulting cell...
	//--------------------------------------------------------------------------
	public final boolean canGetProportions() {
		return (mTotal > 0);
	}
	
	//--------------------------------------------------------------------------
	public final float getProportionAg() {
		return (float)mCountAg / mTotal;
	}
	
	//--------------------------------------------------------------------------
	public final float getProportionForest() {
		return (float)mCountForest / mTotal;
	}

	//--------------------------------------------------------------------------
	public final float getProportionGrass() {
		return (float)mCountGrass / mTotal;
	}
	
	//--------------------------------------------------------------------------
	public final float getProportionDeveloped() {
		return (float)mCountDeveloped / mTotal;
	}
	
	// NOTE: includes wetland
	//--------------------------------------------------------------------------
	public final float getProportionWater() {
		return (float)mCountWater / mTotal;
	}
}

