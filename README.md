# Vanilla Bosses Plugin
Spawns bosses naturally

## Contents:

- [Description](#Description)
- [Important information](#Important-information:)
- [Users' Guide](#How-to-use-this-plugin:)
- [Config guide](#Inside-the-config-file:)
- [Available Bosses](#Available-Bosses:)
- [Available Items](#Available-Items:)
- [Commands](#Commands:)


### Description

This plugin adds many types of bosses to the game. The bosses are edited Vanilla mobs.\
These bosses have special abilities and some wear armor and/or carry weapons\
This plugin also adds a bunch of new Items which have special abilities of their own!


### Important information:

My code is protected by German copyright.\
Do not republish my code without permission.\
This plugin is free to use, so I am not obligated to provide support or bugfixes.\
I still do this, but reserve my right to stop doing so at any point. (In the near future however I cannot see myself
abandoning this project)

I provide support on my discord server. Join [here](https://discord.gg/stAd5ccDZT) \
The plugin is downloadable via Spigot. Get it [here](https://www.spigotmc.org/resources/vanilla-bosses.95205/)

## How to use this plugin:
If you have used an old version I recommend saving your config to a different place and deleting the old config in the folder.
I say this because I had to change how some values worked so a chance of 10 in the old plugin which was 10% is 100% in this version of the plugin.
I changed chances to be values between 0 and 1.

In case you have never installed a plugin on your spigot server:
 - download the plugin
 - open the plugins folder in your server folder
 - put the plugin.jar in the plugins folder

### Inside the config file:
 - CommandsExecutedOnBossDeath: // Copy and paste the standard values directly below the others in order to make a new command. In this new command you can now edit the command and the delay for the command to take effect. To edit the delay you just have to change the number behind the 'DELAY:' to the delay you want. If you want no delay you may leave out the 'DELAY:5' or put 'DELAY:0'.\
The first command is an empty command by default and has the key '0'. After this you can begin counting up. the first non-empty command (so the second actual command) has the key '1'\
Please ignore the "" inside the <> for placeholders, .md does not show the contents otherwise\
The commands have certain Placeholders, such as:\
<"killer"> is replaced with the name of the killer (the last person to damage it)\
<"25"> executes the command for all players within the radius 25, can put that anywhere in the command basically\
<"mostDamage"> executes the command for the player who did the most damage to the boss\
<"damager"> executes the command for all players who damaged the boss\
 
 - RespawningBosses: //This feature allows you to put a boss at a certain location whenever the server starts up and make it respawn if killed. the respawn delay is configurable, as are the commands this specific boss should execute when killed.
  - "{\"type\":\"ZOMBIE\",\"worldName\":\"world\",\"x\":\"0.0\",\"y\":\"70.0\",\"z\":\"0.0\",\"respawnTime\":
    \"20\",\"commands\":[\"1\",\"2\"],\"enableBoss\":\"false\"}"\
    This is all you need to make a respawning boss. Put the "enableBoss" to true and you will get a respawning Zombie
    boss at the coordinates 0,0,0 in the world "world" with a respawn time of 20 seconds. This boss will try to execute
    the commands 1 and 2 when it dies. If the command does not exist, the boss will not attempt to execute it (
    obviously).

 - killedMessage: // You will find this setting for each boss. this specifies what should be written in chat once a boss is defeated.
   Placeholders for this are:
 <"mostDamage"> read above
 <"killedName"> replaces this placeholder with the name of the killed boss
   <"killer"> read above

 - spawnNaturally: true/false //specifies, whether this boss should spawn naturally in the world without being a
   respawning boss or being spawned via command or egg
 - Items:
 - DisableRepairAndEnchant: true/false true: prevents players from repairing and enchanting the plugin items at an anvil
   false: Players can only repair plugin items using other plugin items of the same type. Players can enchant plugin
   items at anvils

 - itemMaterial: //Specifies which material the item should be made of. this enables you to switch the material of the
   cloakOfInvisibility to Netherite_boots for example
 - dropChance: //Specifies the chance with which the item is dropped when the corresponding boss dies

### Available Bosses:

- Blaze (Shoots projectiles different from a normal blaze)
- Creeper (does not die when it explodes, shoots tnt in every direction when doing so)
- Enderman (Spawns endermites when teleporting, can get random potion effects as specified by the config)
- Magma cube (When hit has a chance to light everything around it on fire after a few seconds)
- Skeleton (A Skeleton with armor and a Skeletor as a weapon. see below for Skeletor. Has a few abilities)
- Slime (When hit has a chance to jump high up in the air and crash back down throwing every Entity which is touching
  the ground away)
- Spider (Has a few abilities)
- Witch (throws strong potions, can make entities switch places when hit)
- Wither (For now just a normal wither with more HP)
 - Zombie (A Zombie with armor on and a Baseball bat in its hand. see below for Baseball bat)
 - Zombified Piglin (A Zombie pigman with armor on and a Butchers axe in its hand. see below for Butchers axe)

### Available Items:
 - Baseball bat (Has a chance to "concuss" (blind) hit enemies. Dropped by the Zombie boss)
 - Blazer (When the wearer is hit the attacker has a chance to be set on fire. Dropped by the Blaze boss)
 - Boss eggs (spawns the boss specified by the egg. Not currently dropped)
 - Butchers Axe (Has a chance to "Bind" (slow) hit enemies. Dropped by the Zombified Piglin Boss)
 - Heated Magma Cream (Consumable, sets everything in a radius on fire. Has 3 levels. Dropped by the Magma cube boss)
 - Invisibility Cloak (gives the wearer the Invisibility Potion effect. This damages the cloak slowly. Dropped by the Enderman boss)
 - Skeletor (when arrows hit an entity TNT spawns at the location of the hit. When TNT is carried in the off hand while shooting the arrow is replaced by a flying block of lit TNT. Dropped by the skeleton boss)
 - Slime Boots (Reduces Fall Damage significantly. Dropped by the Slime boss)
 - Slingshot (Launches the player in the direction they are looking. right click while crouching to activate. Dropped by the Spider boss)

### Commands:
A ? in front of a parameter means that this one is optional
 - /vbAdmin giveItem itemName/material ?playerName ?amount // gives the sender the specified item, or gives the specified player the item. 
 - /vbAdmin specialItem bossegg playerName amount Type // gives the specified player the specified amount of boss eggs for the specified type
 - /vbAdmin specialItem hmc playerName amount Level // gives the specified player the specified amount of heated magma cream of the specified level
 - /vbAdmin spawnBoss BossType worldName;X;Y;Z // spawns a boss at the specified Location
 - /vbAdmin spawnBoss BossType ?PlayerName  // spawns a boss on the player specified
 - /vbAdmin bossInfo BossType //displays most info about this boss in chat for the sender
 - /vbAdmin respawningBoss Type respawnTime commands // commands has to be like this: 1,2,3 in order to get the commands 1, 2 and 3 /Planning on giving this command more functionality in the future
