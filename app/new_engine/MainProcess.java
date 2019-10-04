package new_engine;

public class MainProcess implements Runnable {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
/*
public class Farm {
// The internal per-farm configuration should support mixed-type operations despite the fact that most (?)
//	operations will be of one type.
	
// Animal Configs:
	// Dairy config:
	// 	# Animal units
	// 	Herd characteristics: proportion heifers, lactating, dry, other
	//	Feeding regime: conventional OR grass-fed
	//		May internally be refined from a prescribed feeding spec for each sub-herd type
	//		In the future, may allow selected farms to customize the feeding spec for each sub-herd type.
	//			Example: heifers or dry cows might be configured as 15% more grass-fed over the conventional feeding spec

	// Beef config:
	// 	# Animal units
	// 	Herd characteristics if needed
	//	Feeding regime: same as for dairy
	
	// Cash Crop config: ?
	// 	? some way of specifying the yield they should claim for the feed allocation system??
	// 	Or if beef and dairy configs are null, the assumption is cash cropper and
	//		yield acquisition is treated as lower priority than herd feeding and they only get yield
	//		some level of access to crop leftovers??
	
	// Other Operation Configs?:
	//	Example: chickens, turkeys, ducks, pigs...etc??

// Other Configs:
	// Some of these may fit in global assumptions but others may want to be configurable per-operation?
	// Harvesting radius?
	// Manure hauling radius?
	// Acres of land?
	// other??
	// Ability to process feed into concentrates?
	

// Working Output Variables:
	// FEED
	// 	calculated feed requirements
	// 	local feed acquired
	//		(anything greater than required is sold to market, anything less than required is purchased from market)
	// 	harvesting/hauling stats (approx distance)?
	// 	harvesting acres estimates
	// MANURE
	// 	acres manure spread
	// 	manure hauling distance
	//	manure map - application rate at each pixel
	// PRODUCTS
	//	Amount of milk, including some measure of quality (e.g., so grass-fed can get a premium)
	//	Amount of beef, including some measure of quality
	
	
// Routines:	
	// Total Feed Requirements:
	//	forage (silage, grass, hay), concentrate, etc
	calcFeedRequirements() {
		for allFarms...
			aFarm.demand = amt(silage, grass, hay, etc)
	}
	
// DEPENDENCY!  Needs -> Yield
	
	// bin yield into coarser units for later distribution
	// NOTE: should also track yield per acre for each bin so we can later estimate production/harvesting costs
	//	and affix those to the associated farm
	binFeed() {
	}
	
	// distribute feed to all farms
	distributeFeed() {
		
		// first let each farm stake a claim to 1 or more feed bins
		for allFarms 
			// TODO: what is the claim radius?
			aFarm.stakeClaim(claimRadius) // register desire for yield
		
		// then process all farms and let them share resources from the feed bins
		//	Currently this code is in an Allocate_Yield.java class. Keeping it as a separate
		//		module may make sense? Example: in case we want to evaluate different Yield_Allocation schemes
		// TODO: approx distance to resources must be tracked so that hauling costs can be estimated!
		resolveFeed()
	}
	
// FUTURE STEP: OPTIMIZE LAND
	evaluateFeedDistribution() {
		// DEPENDENCY! Most likely needs -> economics?
		// if feed demands aren't met, probably inspect economics and assess which grown feed should be prioritized
		//	Example: if there's excess grass but a shortage of silage, consider removing grass in favor of corn
		//		Possible optimize settings??: aggressive (allow row crops near streams) or safe (keep buffer strips, etc)
		// If changes are suggested, transform as needed, RECALUATE NEW YIELD, and...
		//	loop back to binFeed() and distributeFeed()...
		//	and possibly another evaluateFeedDistribution(). Maybe this cycle can happen 2 or 3 times to refine the solution?
	}
	
// PROVIDES: allocated yield (if needed)
	
// FUTURE STEP: Provides updated landcover due to optimization transform. 
//	Any modules needing landcover (biodiversity modules) should most likely stall execution when optimize landcover is ON 

	calcManure() {
		// may be simple and not even feed based but leaving the option for feeding strategy
		//	to impact the manure volume and/or nutrient profile
	}
	
	// Manure Bins:
	//	bin-ified manure, where? By which process/module/system?
	
	distributeManure() {
		// must track manure placement for p, soil loss, etc
		allocateManureMap
		for each manureBin:
			// get landcover pixels covering bin: corn, soy, alfalfa, etc.
			getCountCells 
			// calculate how much manure each category should ideally get, possibly capping to sane values if needed
			divideManureByCells
			// actually spread manure and track amount to manure map
			distributeManureToCells
			
			// TODO: hauling distance must be tracked so that hauling costs can be estimated!
			
			// TODO: if any manure is left over, what to do with it?
			//  - possibly bin-ify remainder to a coarser manure grid and run the process over? And repeat until it all works out?
			//	- maybe
			
			// TODO: grass/pasture fed might need special handling
		
	}

// PROVIDES: manureMap
	
	calcProducts() {
		// estimate products created, which may also include categories of products by quality
		//	e.g. grass-fed beef/dairy could command a premium
	}

// PROVIDES: animal productions
	
	// May not be the actual costs but some kind of stats which can be transformed into economic values
	calcProductionInputs() {
		// may actually best fit in each sub-process section and tracked as we go vs. done in a single step
		// and it may not actually be the final production costs but rather stats/info that can be used to 
		//	calculate the costs. 
		// E.g., farm A harvested 50 acres of corn, 100 acres of soy, and 20 acres of alfalfa. (we can later calc production costs from this)
		//	cows consumed some proportion of each of those but extra feed was purchased (from this we can calc feed costs and crop profits)
		//  Somehow track/calc the harvesting distances to inform the costs related to that...
		//	Somehow track/calc the manure hauling distances
		//	etc...
	}

// PROVIDES: stats that can be transformed into economic values
	
}
*/
