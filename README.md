# MagicTeleport

This plugin adds a special block to your minecraft server.

When players place this block, and have access to the command (via permissions) they can later teleport directly to the block, from anywhere in the world.

There is a single command, `magicteleport` aliased to `mtp` for quicker typing.

As a user, typing this command will present a list of blocks I have placed around the world in the chat. Clicking one of the lines will teleport me to that block.

If I have the `magicteleport.admin` permission I can give the teleport block to a player with `mtp give <username>`

The number of blocks each player can place is handle by the `magicteleport.quantity.x` permission. For example a player with `magicteleport.quantity.1` will only be able to place 1 teleport block, but a user with `magicteleport.quantity.6` will be able to place 6 teleport blocks in the world. This does not affect the number of teleport blocks they can have in inventory.

Teleport blocks can be renamed in an anvil, and then place somewhere. The name you have defined will then appear in the block list when choosing where to teleport to. This makes it easier to remember which block will teleport you where.