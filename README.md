# SimpleChestLogging

An extremely simple plugin for small to medium Paper servers that logs every player interaction with vanilla
containers (barrels, chests, copper chests, ender chests, shulker boxes, dispensers, droppers, and hoppers).

### Requirements

Server must have Paper installed. This plugin currently officially supports stable builds of Paper built for versions
`1.21.11` through `26.2` of Minecraft. Use with alpha/beta builds of Paper at your own risk and discretion.

### Commands

- `/simplecl on`: requires operator - turns SimpleChestLogging on
- `/simplecl off`: requires operator - turns SimpleChestLogging off

And that's it! Chest interactions will show up in the logs in the following format:

```
Notch put 3x iron_ingot in CHEST at (-640, 63, 216)
Notch took 64x cooked_beef from SHULKER_BOX at (-88, 65, -192)
```