package utils;

// Sample usage:
//	
//	*Assume CDL raster cell value contains: CELL_VALUE
//
// 	*Find out if CDL cell value has certain options active on that raster cell:
//		if (E_Till.isActiveOn(CELL_VALUE)) {
//			// Tillage is active on this raster cell
//		}
//
//	*Set a management option to be active on the given cell
//		int newCellValue = E_CoverCrop.setOn(CELL_VALUE);
//
//	*Clear a management option from the given cell
//		int newCellValue = E_Manure.clearFrom(CELL_VALUE);
//
//	*Returns 1.5 if the raster cell, CEL_VALUE, has E_Tillage active on it, else returns 1.0f
//     float mult = E_Till.getIfActiveOn(CELL_VALUE, 1.5f, 1.0f);
//
//------------------------------------------------------------------------------
public enum ManagementOptions 
{
	// These correspond to bits encoded in the CDL. 
	// They MUST NOT overlap with the lower CDL indexes that represent things such as:
	//	Corn, Soy, Urban, Water, etc...
	// They also MUST NOT go above 31
    E_Till  		(25), // Tilled if true, No Till if false
    E_Fertilizer	(26), // Fertilizer used if true, No Fertilizer if false
    E_Manure		(27), // ONLY check if E_Fertilizier == true
    E_FallManure	(28), // ONLY check if E_Manure == true
    E_CoverCrop		(29), // Generic cover crop if true, not if false
    E_Contour  		(30), // Contour if true, No Contour if false
    E_Terrace 		(31); // Terrace if true, No Terrace if false

	// TODO: if still needed, consider more sophisticated masks. Example, uses only 2 bits vs. 3 bits above
	//	Biggest problem might be confusion between which to use. Example: E_Fertilized would be used differently...
	//	E_Fertilized 		(0x00000110000000000000000000000000) // mask any using | and test if > 0
	//  E_Synthetic			(0x00000010000000000000000000000000) // mask match using & AND must == E_Synthetic
	//  E_Manure			(0x00000100000000000000000000000000)
	//  E_FallManure		(0x00000110000000000000000000000000)
	
    private final int mIndexMask; // stores the shifted value for easy masking
    
    // private Constructor, cannot be accessed...
    //--------------------------------------------------------------------------
    ManagementOptions(int index) {
    	
        this.mIndexMask = (1 << index); // shift to make a mask, woo
    }
        
    // Ask if the incoming value has this option activated on it
    //--------------------------------------------------------------------------
    public final boolean isActiveOn(int compareValue) {
    	
    	return (compareValue & mIndexMask) > 0;
    }

	// Returns the true value if the given management option is active on compareValue, else the falseValue    
    //--------------------------------------------------------------------------
    public final float getIfActive(int compareValue, float trueValue, float falseValue) {
    	
    	if ((compareValue & mIndexMask) > 0) 
    		return trueValue;
    	else 
    		return falseValue;
    }
    
	// Applies the given option to the incoming base value    
    //--------------------------------------------------------------------------
    public final int setOn(int baseValue) {
    	
    	return baseValue | mIndexMask;
    }
    
	// Clears the given option from the incoming base value    
    //--------------------------------------------------------------------------
    public final int clearFrom(int baseValue) {
    	
    	return baseValue & (~mIndexMask); // invert all bits in the mask and &
    }
    
    // Convenience function if needed, possibly isn't though...
    //--------------------------------------------------------------------------
    public final int getMask() {
    		
    	return mIndexMask;
    }
    
    // Helper function to determine multiplier for fertilizer
    //--------------------------------------------------------------------------
    public final static float getFertilizerMultiplier(int landcover, float noFertilizerMultiplier,
    					float syntheticMultiplier, float fallSpreadManureMultiplier, float otherManureMultiplier) {
    
    	float result = noFertilizerMultiplier;
    	
		if (E_Fertilizer.isActiveOn(landcover)) {
			if (E_Manure.isActiveOn(landcover)) {
				if (E_FallManure.isActiveOn(landcover)) {
					result = fallSpreadManureMultiplier;
				}
				else { // is fertilized, and it's manure, but is not fall spread manure
					result = otherManureMultiplier;
				}
			}
			else { // is fertilized but is not manure, ie, it's synthetic
				result = syntheticMultiplier;
			}
		}
		return result;
    }
}

