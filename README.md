What is NotOnMyLawn?
--------------------

NotOnMyLawn is a mod that controls the spawning of mobs around man-made 
structures. It is strongly inspired by sluvine's CivilizedSpawn, but using 
completely new code.

### Background story

CivilizedSpawn was exactly what I was looking for, but it's not compatible
with newer versions of Bukkit. Since I couldn't find any source code to work
from, I reimplemented all the features of CivilizedSpawn based (roughly) on
the description in the forum thread, and I'm comfortable that it should be at
least as stable and fast as the original. Since sluvine probably spent some 
time tweaking the default values, I did use his values on this plugin.

### Description and Features

NotOnMyLawn disallows mob spawns in areas where it detects man-made structures, by detecting the types of blocks in the vicinity of the spawn position and, if enough of them are crafted blocks, cancelling the spawn. 

An optional feature (defaults to enabled) makes the plugin detect structures around the spawning position by searching around the vicinity of detected blocks and only marking that block as detected if there's enough blocks around it that are also detected. This prevents the plugin from cancelling spawns around misplaced blocks, markers or other tiny structures.

Because the normal operation would also disable spawns on generated structures like dungeons and abandoned mineshafts, and to allow the players to choose where they DO want monsters to spawn, there's also an option to allow spawns to happen near Mob Spawner blocks and Cobwebs.

Finally, to prevent monsters from spawning on treetops, when those trees are placed around your structures, there's an option to disallow spawning on a block that's surrounded by a number of leaves.

### Installation and Configuration

NotOnMyLawn does not require any special installation steps, just place the provided NotOnMyLawn.jar file in your bukkit's ```plugins/``` directory and it will create a default configuration file on the first run.

If you want to tweak the settings, open ```plugins/NotOnMyLawn/config.yml``` in your favorite text editor, change the settings, and reload the server (or disable and reenable NotOnMyLawn if you have a server plugin that provides such functionality).

The default setting values are shown below, along with comments describing them:

```
enable: true          # Global enable toggle for the plugin
ignore_animals: true  # If true, the plugin does not prevent animals from spawning
detect:       # Detection-specitic settings
  radius: 15        # Size of the detection square measured around the spawning block
  height: 5         # Vertical size of the detection box, both above and below the spawn
  limit: 10         # Number of blocks matched after which the spawning is disallowed
  depth_limit: 50   # Allows all spawns to happen below this height
  hard_limit: 500   # Hard limit of detected blocks after which the spawn is disallowed
  block_types:      # List of blocks that will be used to detect man-made structures
  - WOOD
  - COBBLESTONE
  - DIAMOND_BLOCK
  - BRICK
  - FENCE
  - GOLD_BLOCK
  - IRON_BLOCK
  - LAPIS_BLOCK
  - SNOW_BLOCK
  - COBBLESTONE_STAIRS
  - WOOD_STAIRS
  - WOODEN_DOOR
  - IRON_DOOR
  - STEP
  - DOUBLE_STEP
  - WOOL
  - BED_BLOCK
  - CHEST
  - FENCE_GATE
  - MOSSY_COBBLESTONE
  - SMOOTH_BRICK
  - SMOOTH_STAIRS
  - THIN_GLASS
  - TRAP_DOOR
  - OBSIDIAN
  - BEDROCK
  - IRON_DOOR_BLOCK
  - IRON_FENCE
  - NETHER_BRICK
  - NETHER_FENCE
structures: # Structure detection settings
  detect: true
  radius: 5
  height: 2
  limit: 3
treetops:   # Treetop spawn prevention
  allow: false
  minimum_leaves: 4 # Number of leaf blocks required to detect a treetop
spawners:   # Spawns near a spawner
  allow: true
cobwebs:    # Spawns near a cobweb
  allow: true
  limit: 1  # Number of cobweb blocks required to allow the spawn
```

### Future

I'm planning on adding new features in the future, the top one would be a way
to customize the spawn allow/prevent checks using configuration options (let
the user specify a list of "if you see at least N of X block within an Y 
radius, prevent spawn"), but so far it's hardcoded to spawners, cobwebs and
leaves, like the original plugin had.

I also want to add commands to change settings, enable and disable NotOnMyLawn from the chat, so that OPs can configure it without requiring access to the server.
