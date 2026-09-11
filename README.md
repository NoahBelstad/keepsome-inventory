# KeepSomeInventory

**KeepSomeInventory** is a lightweight Fabric Minecraft mod that gives you granular control over item drops on death. Instead of an all-or-nothing approach with vanilla `keepInventory`, this mod lets players keep whitelisted items (like valuable tools, weapons, and armor) while introducing a randomized chance for other inventory items to drop.

---

## Features

* **Custom Whitelist:** Define a list of items that are completely safe and never drop upon death (pre-configured with vanilla tools, armor tiers, and utility items).
* **Randomized Drop Chance:** Non-whitelisted items have a customizable probability (`dropChance`) of dropping when you die.
* **In-Game Commands:** Easily add, remove, or reload configurations on the fly without restarting your game.
* **Integrated Server Support:** Open your config file directly from the game chat while playing in Singleplayer.

---

## Game Rule

The mod introduces a custom game rule to toggle the feature on or off:

* `/gamerule do_keep_some_inventory <true|false>` (Default: `true`)

When enabled, it forces vanilla `keepInventory` to true and applies the mod's custom drop/whitelist logic upon player death.

---

## Commands

All commands require **Operator / Moderator permissions** (`Permissions.COMMANDS_MODERATOR`).

| Command | Description |
| :--- | :--- |
| `/keepsome reload` | Reloads the configuration file from disk into memory. |
| `/keepsome config` | Opens the config file directly on your local machine *(Singleplayer / Integrated Server only)*. |
| `/keepsome add` | Adds the item currently in your main hand to the whitelist. |
| `/keepsome add <itemid>` | Adds a specified item ID (e.g., `diamond`) to the whitelist. |
| `/keepsome remove` | Removes the item currently in your main hand from the whitelist. |
| `/keepsome remove <itemid>` | Removes a specified item ID from the whitelist. |

---

## Configuration

The configuration file is saved as `keepsome-inventory.json` in your config directory.

```json
{
  "dropChance": 0.75,
  "whitelist": [
    "minecraft:netherite_sword",
    "minecraft:diamond_pickaxe",
    "minecraft:elytra"
  ]
}
```
`dropChance`: The probability (from `0.0` to `1.0`) that a non-whitelisted item will drop upon death (e.g., `0.75` means a 75% chance per item stack).

`whitelist`: A list of item registry identifiers that are protected from dropping.
