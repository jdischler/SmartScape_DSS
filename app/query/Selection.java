package query;

import play.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

//------------------------------------------------------------------------------
public class Selection
{	
	// Set up to run the query...allocate memory...
	public byte[][] mRasterData;
	public int mHeight, mWidth;
	public boolean isValid = false;
	
	// Constructor...
	//--------------------------------------------------------------------------
	public Selection(int width, int height) {
		
		mHeight = height;
		mWidth = width;
		mRasterData = new byte[mHeight][mWidth];
		// ...and initialize everything to 1 to prep for & (and) logic
		for (byte[] row: mRasterData) {
			Arrays.fill(row, (byte)1);
		}
		isValid = true;
	}
	
	//--------------------------------------------------------------------------
	public Selection(int width, int height, byte fillValue) {
		
		mHeight = height;
		mWidth = width;
		mRasterData = new byte[mHeight][mWidth];
		for (byte[] row: mRasterData) {
			Arrays.fill(row, fillValue);
		}
		isValid = true;
	}

	//--------------------------------------------------------------------------
	public Selection(File createFromFile) {
		isValid = loadSelection(createFromFile);
	}
	
	//--------------------------------------------------------------------------
	public final int getWidth() {
		return mWidth;
	}
	public final int getHeight() {
		return mHeight;
	}
	
	//--------------------------------------------------------------------------
	public boolean isSelected(int atX, int atY) {
		
		return (mRasterData[atY][atX] > 0);
	}

	//--------------------------------------------------------------------------
	public int countSelectedPixels() {
		
		int x, y, count = 0;
		
		for (y = 0; y < mHeight; y++) {
			for (x = 0; x < mWidth; x++) {
				// NOTE: relies on mSelection containing 1's and 0's...
				//count += mRasterData[y][x];
				// Otherwise do something like...
				count += (mRasterData[y][x] > 0 ? 1 : 0);
			}
		}
		return count;
	}
	
	// Takes the selected pixels in otherSel and adds them into this selection (union operation)
	//--------------------------------------------------------------------------
	public void combineSelection(Selection otherSel) {
		
		int x, y;
		for (y = 0; y < mHeight; y++) {
			for (x = 0; x < mWidth; x++) {
				mRasterData[y][x] |= otherSel.mRasterData[y][x];
			}
		}
	}
	
	// Intersection (common elements) of two selections
	//--------------------------------------------------------------------------
	public void intersectSelection(Selection otherSel) {
		int x, y;
		for (y = 0; y < mHeight; y++) {
			for (x = 0; x < mWidth; x++) {
				mRasterData[y][x] &= otherSel.mRasterData[y][x];
			}
		}
	}
	
	// Takes the selected pixels in otherSel and removes them from this selection
	//--------------------------------------------------------------------------
	public void removeSelection(Selection otherSel) {
		
		int x, y;
		for (y = 0; y < mHeight; y++) {
			for (x = 0; x < mWidth; x++) {
				// Flip/invert the first bit... and AND that back in to remove
				mRasterData[y][x] &= (otherSel.mRasterData[y][x] ^ 1);
			}
		}
	}
	
	// Anything that is selected becomes NOT selected. Anything NOT selected
	//	becomes selected
	//--------------------------------------------------------------------------
	public void invertSelection() {
		
		int x, y;
		for (y = 0; y < mHeight; y++) {
			for (x = 0; x < mWidth; x++) {
				// flip the 1st bit
				mRasterData[y][x] ^= 1;
			}
		}
	}
	
	//--------------------------------------------------------------------------
	public boolean loadSelection(File loadFromFile) {

		if (!loadFromFile.exists()) {
			return false;
		}
		
		FileInputStream fileStream = null;
		ReadableByteChannel fileChannel = null;
		ByteBuffer lineBuffer = null;
		int fileVersion = 0;
		
		// Get header....
		try {
			fileStream = new FileInputStream(loadFromFile);
			fileChannel = fileStream.getChannel();
			
			Logger.info("  Reading header...");
			lineBuffer = ByteBuffer.allocateDirect(4); // FIXME: size of int (version)?
			fileChannel.read(lineBuffer); 
			lineBuffer.rewind();
			fileVersion = lineBuffer.getInt();
			Logger.info("  - Binary file version: " + Integer.toString(fileVersion));
				
			lineBuffer = ByteBuffer.allocateDirect(6 * 4); // FIXME: size of header * size of int?
			fileChannel.read(lineBuffer); 
			lineBuffer.rewind();
			
			mWidth = lineBuffer.getInt();
			mHeight = lineBuffer.getInt();
			
			Logger.info("  - Width: " + Integer.toString(mWidth) 
							+ "  Height: " + Integer.toString(mHeight));
		}
		catch (Exception e) {
			Logger.warn(e.toString());
			return false;
		}
		
		// ....get Raster data....
		lineBuffer = ByteBuffer.allocateDirect(mWidth * 1); // FIXME: size of byte
		mRasterData = new byte[mHeight][mWidth];
		
		for (int y = 0; y < mHeight; y++) {
			try {		
				lineBuffer.clear();
				fileChannel.read(lineBuffer);
				lineBuffer.rewind();
			}
			catch (Exception e) {
				Logger.warn(e.toString());
			}
			
			for (int x = 0; x < mWidth; x++) {
				mRasterData[y][x] = lineBuffer.get(x);
			}
		}

		// ...close everything down...
		try {
			fileChannel.close();
			fileStream.close();
			fileStream = null;
		}
		catch (Exception e) {
			Logger.warn(e.toString());
		}
		finally {
			if (fileStream != null) {
				try {
					fileStream.close();
				}
				catch (Exception e) {
					Logger.warn(e.toString());
					return false;
				}
			}
		}
		
		return true;
	}

	// THIS is considering the top layer, UNDER is the selection underneath
	public int countOccludedPixels(Selection under) {
		int x, y, count = 0;
		
		for (y = 0; y < mHeight; y++) {
			for (x = 0; x < mWidth; x++) {
				// occluded pixels can only occur under active THIS cells
				if (mRasterData[y][x] > 0) {
					// and UNDER can only be occluded if it is itself active
					count += (under.mRasterData[y][x] > 0 ? 1 : 0);
				}
			}
		}
		return count;
	}
}

