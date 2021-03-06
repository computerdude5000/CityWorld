package me.daddychurchill.CityWorld.Context;

import me.daddychurchill.CityWorld.CityWorldGenerator;
import me.daddychurchill.CityWorld.Plats.PlatLot;
import me.daddychurchill.CityWorld.Plats.RoadLot;
import me.daddychurchill.CityWorld.Plats.Nature.BunkerLot;
import me.daddychurchill.CityWorld.Plats.Nature.MineEntranceLot;
import me.daddychurchill.CityWorld.Plats.Nature.MountainShackLot;
import me.daddychurchill.CityWorld.Plats.Nature.MountainTentLot;
import me.daddychurchill.CityWorld.Plats.Nature.OilPlatformLot;
import me.daddychurchill.CityWorld.Plats.Nature.OldCastleLot;
import me.daddychurchill.CityWorld.Plats.Nature.RadioTowerLot;
import me.daddychurchill.CityWorld.Support.HeightInfo;
import me.daddychurchill.CityWorld.Support.HeightInfo.HeightState;
import me.daddychurchill.CityWorld.Support.Odds;
import me.daddychurchill.CityWorld.Support.PlatMap;
import me.daddychurchill.CityWorld.Support.SupportChunk;

public class NatureContext extends UncivilizedContext {

	public NatureContext(CityWorldGenerator generator) {
		super(generator);
	}
	
	private final static double oddsOfBunkers = Odds.oddsLikely;

	@Override
	public void populateMap(CityWorldGenerator generator, PlatMap platmap) {

		//TODO, Nature doesn't handle schematics quite right yet
		// let the user add their stuff first, then plug any remaining holes with our stuff
		//mapsSchematics.populate(generator, platmap);
		
		// random stuff?
		Odds platmapOdds = platmap.getOddsGenerator();
		boolean doBunkers = generator.settings.includeBunkers && platmapOdds.playOdds(oddsOfBunkers);
		boolean didBunkerEntrance = false;
		
		// where it all begins
		int originX = platmap.originX;
		int originZ = platmap.originZ;
		HeightInfo heights;
		
		// highest special place
		int maxHeight = Integer.MIN_VALUE;
		int maxHeightX = -1;
		int maxHeightZ = -1;
		HeightState maxState = HeightState.BUILDING;
		
		// lowest special place
		int minHeight = Integer.MAX_VALUE;
		int minHeightX = -1;
		int minHeightZ = -1;
		HeightState minState = HeightState.BUILDING;
		
		// is this natural or buildable?
		for (int x = 0; x < PlatMap.Width; x++) {
			for (int z = 0; z < PlatMap.Width; z++) {
				PlatLot current = platmap.getLot(x, z);
				if (current == null) {
					
					// what is the world location of the lot?
					int chunkX = originX + x;
					int chunkZ = originZ + z;
					int blockX = chunkX * SupportChunk.chunksBlockWidth;
					int blockZ = chunkZ * SupportChunk.chunksBlockWidth;
					
					// get the height info for this chunk
					heights = HeightInfo.getHeightsFaster(generator, blockX, blockZ);
					if (!heights.isBuildable()) {
						
						// our inner chunks?
						if (x > 0 && x < PlatMap.Width - 1 && z > 0 && z < PlatMap.Width - 1) {
							
							// extreme changes?
							if (heights.minHeight < minHeight) {
								minHeight = heights.minHeight;
								minHeightX = x;
								minHeightZ = z;
								minState = heights.state;
							}
							if (heights.maxHeight > maxHeight) {
								maxHeight = heights.maxHeight;
								maxHeightX = x;
								maxHeightZ = z;
								maxState = heights.state;
							}
							
							// innermost chunks?
							boolean potentialRoads = (x == (RoadLot.PlatMapRoadInset - 1) || x == (PlatMap.Width - RoadLot.PlatMapRoadInset)) && 
													 (z == (RoadLot.PlatMapRoadInset - 1) || z == (PlatMap.Width - RoadLot.PlatMapRoadInset));
							boolean doBunkerEntrance = doBunkers && !didBunkerEntrance && !potentialRoads;
							
							// what type of height are we talking about?
							switch (heights.state) {
							case MIDLAND: 
								
//								// if not one of the innermost or the height isn't tall enough for bunkers
//								if (!innermost || minHeight < BunkerLot.calcBunkerMinHeight(generator)) {
//									if (heights.isSortaFlat() && generator.shapeProvider.isIsolatedConstructAt(originX + x, originZ + z, oddsOfIsolatedConstructs))
//										current = createSurfaceBuildingLot(generator, platmap, originX + x, originZ + z, heights);
//									
//									else if (doBunkers && innermost) {
//										current = createBuriedBuildingLot(generator, platmap, chunkX, chunkZ, !didBunkers);
//										didBunkers = true;
//									}
//								} 
								
								if (doBunkers && minHeight > BunkerLot.calcBunkerMinHeight(generator)) {
									current = createBuriedBuildingLot(generator, platmap, chunkX, chunkZ, doBunkerEntrance);
									
									
								} else if (heights.isSortaFlat() && generator.shapeProvider.isIsolatedConstructAt(originX + x, originZ + z, oddsOfIsolatedConstructs)) {
									current = createSurfaceBuildingLot(generator, platmap, originX + x, originZ + z, heights);
								}
								
								break;
							case HIGHLAND:
							case PEAK:
								if (doBunkers) {
									current = createBuriedBuildingLot(generator, platmap, chunkX, chunkZ, doBunkerEntrance);
								}
								break;
							default:
								break;
							}
							
							// did we do it?
							if (doBunkerEntrance && current != null)
								didBunkerEntrance = true;
						}
						
						// did current get defined?
						if (current != null)
							platmap.setLot(x, z, current);
						else
							platmap.recycleLot(x, z);
					}
				}
			}
		}
		
		// any special things to do?
		populateSpecial(generator, platmap, maxHeightX, maxHeight, maxHeightZ, maxState);
		populateSpecial(generator, platmap, minHeightX, minHeight, minHeightZ, minState);
	}
	
	protected PlatLot createBuriedBuildingLot(CityWorldGenerator generator, PlatMap platmap, int x, int z, boolean firstOne) {
		if (generator.settings.includeBunkers)
			return new BunkerLot(platmap, x, z, firstOne);
		return null;
	}
	
	protected PlatLot createSurfaceBuildingLot(CityWorldGenerator generator, PlatMap platmap, int x, int z, HeightInfo heights) {
		if (generator.settings.includeHouses)
			if (platmap.getOddsGenerator().flipCoin())
				return new MountainShackLot(platmap, x, z);
			else
				return new MountainTentLot(platmap, x, z);
		return null;
	}
	
	protected void populateSpecial(CityWorldGenerator generator, PlatMap platmap, int x, int y, int z, HeightState state) {

		// what type of height are we talking about?
		if (state != HeightState.BUILDING && 
			generator.shapeProvider.isIsolatedConstructAt(platmap.originX + x, platmap.originZ + z, oddsOfIsolatedConstructs / 2)) {
			
			// what to make?
			switch (state) {
			case DEEPSEA:
				// Oil rigs
				if (generator.settings.includeBuildings) {
					platmap.setLot(x, z, new OilPlatformLot(platmap, platmap.originX + x, platmap.originZ + z));
				}
				break;
//			case SEA:
				//TODO boat!
//				break;
//			case BUILDING:
//				break;
//			case LOWLAND:
				//TODO Statue overlooking the city?
				//TODO hot air balloon!
//				break;
			case MIDLAND: 
				// Mine entrance
				if (generator.settings.includeMines)
					platmap.setLot(x, z, new MineEntranceLot(platmap, platmap.originX + x, platmap.originZ + z));
				break;
			case HIGHLAND: 
				// Radio towers
				if (generator.settings.includeBuildings)
					platmap.setLot(x, z, new RadioTowerLot(platmap, platmap.originX + x, platmap.originZ + z));
				break;
			case PEAK:
				// Old castle
				if (generator.settings.includeBuildings)
					platmap.setLot(x, z, new OldCastleLot(platmap, platmap.originX + x, platmap.originZ + z));
				break;
			default:
				break;
			}
		}
	}

	public void validateMap(CityWorldGenerator generator, PlatMap platmap) {
	}
}
