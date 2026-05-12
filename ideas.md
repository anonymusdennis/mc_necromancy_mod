This document is to collect some ideas i have   
ignore this file unless told to implement one of thse ideas:  
  
 *add a way to deconstruct / cage a minion => requires to apply anesthetics to the mob first ( blowdart and anesthetic fluid and blowgun ) *  
  
*=> add a cage that allows capturing any vanilla friendly mob and any minion *  
*=> add a new table called "operation table" *  
*=> operation table allows to modify any minion *  
*=> requires anesthetics to operate*  
*=> anesthetics => *  
*made like this: *  
* Sedative Base 

Brewing stand:

- awkward potion
- poppy petals / poppy extract

Result:  
→ **Sedative Potion**

Effect:

- slowness
- reduced damage sensitivity
- slight blur

## Step 2 — Ether Compound

Brew sedative potion with:

- fermented spider eye
- sugar

Result:  
→ **Crude Anesthetic**

This makes it feel like:  
 makes additionally blackscreen  
Step 3 — Refined Anesthetic

Add:

- ghast tear OR chorus fruit

Result:  
→ **Refined Anesthetic**

Effects:

- near-total immobilization
- pain suppression
- enables surgery mechanics

each requires the previous potion   
1 refined anesthetics plus 8 blowdarts makes 8 tranquilizer darts  
if you just put the potion inside the craftingtable => turns potion into 16 [effect] blade-oil   
=> if held in offhand and attacking a mob with any sword => applies the effect via the blade oil  
=> both darts and blade oil need dynamic coloring => inherit the original potions color   
=> make this work with any and all potions and add a description to all items inside their tooltips  
1 dart makes a mob of 10 hp(configurable) fall into a 20 minute coma => make mob use the dinnerbone effect and make mob braindead for that time   
on players make it 1 minute and force player into a "out of body perspective" => make spectate the shooter  
if self-hit => circle around camera above players body  
disable all inputs  
disable all sounds   
if the dose is below the max health of a mob => only makes mob and player feel nauseaus and add a blur efffect to screen and make player slower and deafen all sounds 

shift rightclick multiple times ( number of "stuck" darts + 1 for helping them up ) to help a player or entity back up before the timer runs out   
  
time is semi extendable by hitting it again with an blowdart => only resets the timer to the max time =>   
=> on hit => check if the "Tranquilized" effects "level" (0-255) is below the health value needed for tranquilization   
if not => increase effect-level   
if yes => reset time to 20 minutes ( configurable) for mobs and 1 minute( configurable) for players   
  
=> full fledged anesthetics system   
  
when a mob is anestesised you can use a cage on it to catch it ( keep all nbt and everything stored => if released mob stays anesthesised ) => no time inside cage / full item-mode  
or you can use a scalpel ( new item made from iron and diamond) to dissect a mob  inherit sword enchantability   
=> this gives a chance for the following (affected by looting ) :  
dropping 1-3 bodyparts ( never more than the actual creature => max looting gives us e.g. for a cow: 2 cow arms 2 cow legs 1 torso 1 head)  
and 1-3 of the inner organs => also check to not spawn 2 brains for 1 cow/ mob   
  
=> if used on a minion prompt player "activate biological kill-switch?"  
=> if yes is pressed => all bodyparts drop   
=> if no is pressed => does not kill minion only hurt it ( always keep 1 hp )  
back to the operating table  
this table allow to operate on your creature   
you can modify all bodyparts in a similar fashion that cyberpunk allows you to do this 

we will add a bunch of droppable bodyparts / organs of a bunch of creatures 

also we will add a dissection table that allows dissection of e.g. a arm to get muscles etc.   
this will be built-out as the development progresses   


















idea 2:  
  
add a way too overengineered operation table   
this has 2 parts to it   
=> in-world rendered live mob editing   
=> not yet explained

=> gui to allow upgrading and shit 

let me explain this idea   
we will completely change how our mobs work   
each bodypart will have so called attachment-points   
each bodypart will be manually configured using a dev-only configuration gui in which i can move and add attachment points   
in which i can define and move the hitbox of all bodyparts   
in the end i want every bodypart to have one or more dedicated attachmentpoints each with a different priority   
the original minion altar will be reworked   
=> player starts with a 3*3 grid instead of "arm" "head" etc. allow to add any bodypart  
=> once a bodypart is inserted => bodybuilding starts   
connected parts will not be able to take out of ui to prefent the bodybuilding from breaking => visualize this inside the slots by greying them out   
only " endpoints" will be able to be taken out of ui making their connectee ungreyed if no more >1 connections are present  
some bodyparts will have rotated variations => cow torso => vertical / horizontal => cycle through those on right-click if present an bodypart is currently "free"  
  
if the player inserts another bodypart attach it to the nearest connection-point inside the 3x3 grid   
if player picks item up and re-inserts in the same slot => cycle through all possibe near connection-points => allows funky body-constructs  
if no head is present => make creature braindead and static   
if no bodypart with "movement" option is present and touching the ground => creature is static   
  
  
ok thats that   
i want to have a uncraftable "developer block"   
inside there   
i want to be able to insert a bodypart and change every value of it   
on "save to disk " this will be output into /config/necormancy/bodypartconfigs/part.json  
overwrite existing files   
=> THESE ARE MANDATORY   
REFUSE CRAFTING / USAGE OF BODYPARTS IF NOT YET CONFIGURED  
  
we will rebuild the mod to have a dynamic hitbox much like the enderdragons layered hitbox   
and to have a dynamic box in general => allow hitting next to creature to not hit creature   
=> aviod huge hitboxes => each bodypart is an actual configured hitbox  
=> allow check for "is leg touching ground?"   
=> we need to make sure ground collision is checked for all bodyparts => if an arm is touching a block => collision   
=> inside the dev block i want a big ui allowing e to configure all the below:  
hitbox:  
offset & position   
size   
on pressing any buttons live preview ( explained below ) gets updated  
connection points => allow adding /removing/ moving / positioning  of connection point   
allow checking / unchecking flags => checkboxes  
primarly   
Head   
ARM  
LEG  
TORSO  
SPECIAL  
If bodypart is flagged as a arm or an leg => allow to also configure the rotation axis / position of the axis for each connection point   
  
this ui also allows us to select which bodypart we want to configure   
if a config is already present => load it insead of resetting to a new config  
once a part is selected closing the ui should save all edits currently made and reopen the same as when closed => retain gui state   
spawn a static bodypart entiy used only for debug purposes => allows to view a singular bodypart   
=> show the actual hitbox of said bodypart   
=> shows/renders all connection points visually and also their rotation axis ( head has 3 )   
when inside the ui of the block allow me to hide unhide specific thing such as hiding a singular connection point allow hiding the texture / model and only rendering the hitbox too   
if you think a rendering only sulution is better here you are allowed to choose the easiest option   
  
  
  


