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

### Future

I'm planning on adding new features in the future, the top one would be a way
to customize the spawn allow/prevent checks using configuration options (let
the user specify a list of "if you see at least N of X block within an Y 
radius, prevent spawn"), but so far it's hardcoded to spawners, cobwebs and
leaves, like the original plugin had.
