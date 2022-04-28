#Programmer Readme file
This file is for programmers who wish to understand my code and/or append it by making their own plugins extending mine.\
Here you can find all of the important data you need about how my code runs, what bosses really are and how you can detect and change them.

###How is a boss made inside my plugin?
 - I listen to the CreatureSpawnEvent. Getting the spawned entity from this event I pair it with my BossDataRetriever object in order to get the corresponding spawn chance from config.
 - Using the config section gotten from the Data Retriever I check whether the boss is allowed to spawn in the world the event location specifies.
 - I then check whether a random double between 0 and 1 is less than the chance retrieved from the DataRetriver Object.
 - If both are true I start changing the mob into a boss by giving it the health and name specified in the config.
 - Then I set the boss tags within the scoreboard tags and the command indexes inside the PDC of the boss.
 - After that I set whether the boss is going to have the glowing potion effect to the value from the config

###How to identity a boss
 - The general boss tag I use is hidden inside the Entity#getScoreboardTags() and is "VB-Boss". Check for Entity#getScoreboardTags().contains("VB-Boss") in order to know whether the entity is a boss created by my plugin\
 - To see which boss it actually is I tag them with an additional Scoreboard tag consisting of "Boss" + Entity name.
    - Examples: {BossBlaze; BossZombified_Piglin; BossEnderman; BossCreeper; BossMagmacube; BossSkeleton}
 - For storage of the command indexes to be executed on boss death I use a new NamespacedKey(Vanillabosses.getInstance(), "CommandIndexes") with the PersistentDataType.INTEGER_ARRAY


If you need more information about how my code runs please do join my discord support server [here](https://discord.gg/stAd5ccDZT)