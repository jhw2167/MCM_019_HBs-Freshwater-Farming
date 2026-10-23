package com.holybuckets.farming.config;

import com.holybuckets.farming.Constants;
import com.holybuckets.farming.config.json.CropBiomeMatrixJsonConfig;
import net.blay09.mods.balm.api.config.reflection.Comment;
import net.blay09.mods.balm.api.config.reflection.Config;

@Config(Constants.MOD_ID)
public class FreshwaterFarmingConfig {

    @Comment("Where the crop biome matrix json can be found. This file sets a growth speed modifier for each seed in each biome")
    public String cropBiomeMatrixConfig = CropBiomeMatrixJsonConfig.DEF_CONFIG_FILE_PATH;

}
