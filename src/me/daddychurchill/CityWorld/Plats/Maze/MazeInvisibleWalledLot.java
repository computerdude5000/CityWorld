package me.daddychurchill.CityWorld.Plats.Maze;

import org.bukkit.Material;

import me.daddychurchill.CityWorld.CityWorldGenerator;
import me.daddychurchill.CityWorld.Support.PlatMap;
import me.daddychurchill.CityWorld.Support.RealChunk;

public class MazeInvisibleWalledLot extends MazeNatureLot {

	public MazeInvisibleWalledLot(PlatMap platmap, int chunkX, int chunkZ) {
		super(platmap, chunkX, chunkZ);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Material getWallMaterial() {
		return Material.BARRIER;
	}
	
	@Override
	protected void generateWallPart(CityWorldGenerator generator, RealChunk chunk, int x1, int x2, int z1, int z2) {
		int mazeY = generator.streetLevel - mazeDepth;
		chunk.setBlocks(x1, x2, mazeY, mazeY + mazeDepth + 1, z1, z2, Material.OBSIDIAN);
		chunk.setBlocks(x1, x2, mazeY + mazeDepth + 2, mazeY + mazeHeight, z1, z2, getWallMaterial());
	}

	@Override
	protected void generateHallPart(CityWorldGenerator generator, RealChunk chunk, int x1, int x2, int z1, int z2) {
		int mazeY = generator.streetLevel - mazeDepth;
		chunk.setBlocks(x1, x2, mazeY, mazeY + mazeDepth + 1, z1, z2, Material.OBSIDIAN);
	}
	
}
