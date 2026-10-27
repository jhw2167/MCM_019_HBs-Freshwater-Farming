# HB's Freshwater Farming Mod

## Overview

    Freshwater Farming is a mildly complex farming change that makes farming a bit more difficult
    but lends itself to modded play and the ability to automate farming AND also encourages players
    spreading their bases around to benefit from growth speeds of different crops in different biomes.
    
## Basic Changes

    - Crops only grow on hydrated farmland
    - Crops will only grow if exposed to Skylight (excepting some like Netherwart)
    - Farmland will only turn hydrated if it exists in a River Biome or is near Freshwater (new block)
    - Freshwater can be obtained:
        - By placing a Water Bucket in a Cauldron and heating it
        - Breaking an ice block
        - Filling a Cauldron from dripping water
        - Using the "Fresh" enchantment on a Water Bucket
        
    - Freshwater turns back into normal water if you toss Salt or Mud into it
    
    - Crop growth speed is dependent on the biome the crop grows in. In some biomes, crops do not grow at all.
    - The crop growth speed biome matrix is fully JSON configurable and works with modded crops and modded biomes. See configs/HBsBiomeCropMatrix.json for the default configuration.
    - Crops will proces growth even when the chunk is unloaded
    - Crops grow much slower, over several days

## Architecture Changes and Overview

    The figures in this mod are centered around wheat time to grow (initially). The *VANILLA* math is:
    - Wheat has 7 stages of growth from seeds to fully mature
    - Wheat is randomly ticked once per game tick (20 times per second) with a 1/4096 chance (each random tick has a chance of applying to any block in the section)
    - Once it accepts a random tick, it has a 1/3 chance of growing to the next stage
    - This is an expected growth rate of about 24 minutes (10-30)
    
    This mod slows crop growth down to encourage players to find the best biomes to grow their crops:
    - So now this mod SKIPS about 1 in 5 random ticks for wheat, pushing the growth rate to about 2 hours
    - Bone meal, fertilized soil, and growing in the right biome can all speed this up!
