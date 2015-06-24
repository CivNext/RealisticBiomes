package com.untamedears.realisticbiomes.persist;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.CropState;
import org.bukkit.Material;
import org.bukkit.NetherWartsState;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.CocoaPlant.CocoaPlantSize;
import org.bukkit.material.Crops;
import org.bukkit.material.MaterialData;
import org.bukkit.material.NetherWarts;

import com.untamedears.utils.Fruits;

// handles force-growing of crop-type blocks based on a fractional growth amount
public class BlockGrower {
	
	public static Logger LOG = Logger.getLogger("RealisticBiomes");
	
	PlantManager plantManager;
	
	// store the total growth stages of plants
	public static HashMap<Material, Integer> growthStages = new HashMap<Material, Integer>();
	static {
		growthStages.put(Material.CROPS, 8);
		growthStages.put(Material.CARROT, 8);
		growthStages.put(Material.POTATO, 8);
		
		growthStages.put(Material.MELON_STEM, 8);
		growthStages.put(Material.PUMPKIN_STEM, 8);
		
		growthStages.put(Material.COCOA, 3);
		
		growthStages.put(Material.NETHER_WARTS, 4);
		
		growthStages.put(Material.SAPLING, 1);
	}
	
	public BlockGrower(PlantManager plantManager) {
		this.plantManager = plantManager;
	}
	
	/**
	 * grow the crop or stem found at the given block's coordinates with the amount
	 * between 0 and 1, with 1 being totally mature
	 * @param block Block to grow
	 * @param growth Block's growth, between 0 and 1
	 * @param fruitGrowth Fruit growth, -1 if fruitless of 0 - 1 if stem
	 */
	@SuppressWarnings("deprecation")
	public void growBlock(Block block, float growth, float fruitGrowth) {
		Integer stages = growthStages.get(block.getType());
		if (stages == null)
			return;
		
		if (growth > 1.0f) {
			growth = 1.0f;
		}
		
		byte stage = (byte)(((float)(stages-1))*growth);
		BlockState state = block.getState();
		MaterialData data = state.getData();
		if (data instanceof CocoaPlant) {
			// trust that enum order is sanely declared in order SMALL, MEDIUM, LARGE
			CocoaPlantSize cocoaSize = CocoaPlantSize.values()[stage]; 
			((CocoaPlant)data).setSize(cocoaSize);
		} else if (data instanceof Crops) {
			// trust that enum order is sanely declared in order
			CropState cropSize = CropState.values()[stage]; 
			((Crops)data).setState(cropSize);
		} else if (data instanceof NetherWarts) {
			// trust that enum order is sanely declared in order
			NetherWartsState cropSize = NetherWartsState.values()[stage]; 
			((NetherWarts)data).setState(cropSize);
		} else {
			data.setData(stage);
		}
		state.setData(data);
		state.update(true, false);
		
		if (fruitGrowth != -1.0) {
			this.growFruit(block, fruitGrowth);
		}
	}
	
	private void growFruit(Block block, float fruitGrowth) {
		if (Fruits.hasFruit(block)) {
			return;
		}
		
		if (fruitGrowth < 1.0) {
			return;
		}
		
		Block freeBlock = Fruits.getFreeBlock(block, null);
		if (freeBlock != null) {
			freeBlock.setType(Fruits.getFruit(block.getType()));
		}
	}
	
	@SuppressWarnings("deprecation")
	public static double getGrowthFraction(Block block) {
		if (block.getType() == Material.SAPLING) {
			return 0.0;
		} else if (!growthStages.containsKey(block.getType())) {
			return 0.0;
		}
		
		byte stage;
		MaterialData data = block.getState().getData();
		if (data instanceof CocoaPlant) {
			CocoaPlantSize cocoaSize = ((CocoaPlant) data).getSize();
			switch (cocoaSize) {
				case SMALL:
					stage = 0;
					break;
				case MEDIUM:
					stage = 1;
					break;
				case LARGE:
				default:
					stage = 2;
					break;
			}
			
		} else {
			stage = data.getData();
		}
		return (double)stage/(double)(growthStages.get(block.getType())-1);
	}

	public static float getFruitGrowthFraction(Block block, Block fruitBlockToIgnore) {
		return Fruits.hasFruit(block, fruitBlockToIgnore) ? 1.0f : 0.0f;
	}
}
