[日本語](README_JP.md)

## under development

## About This Mod
A derivative work of "Private_Dimension" by Chuzume.<br>

This mod is a reimplementation of "Private_Dimension" by Chuzume as a Fabric/NeoForge mod.
All copyrights and other rights related to the original work belong to Chuzume.

The author and distributor take no responsibility for any issues or damages arising from the use of this mod.


## Original Work & References

- Creator: <br>[@Chuzume](https://x.com/Chuzume)
- Repository: <br>[Private_Dimension](https://github.com/Chuzume/Private_Dimension)
- Video: <br>[【マイクラ】"次元の瓶"で、家とか拠点を持ち歩いちゃおう！！！【データパック】](https://www.youtube.com/watch?v=NrwN3NJLuiA)


## How to Use
Recipe
Craftable at a standard crafting table.

[Dimension in a Bottle]

![Image](https://cdn-ak.f.st-hatena.com/images/fotolife/C/Chuzume/20230105/20230105085556.png)

# PrivateDimension

Private Dimension Mod for Fabric / NeoForge

## Overview

Using the **Dimension in a Bottle** item takes you to your own private dimension.
The dimension contains a dedicated 48×48 space with a generated structure.

This mod is a reimplementation of [Private Dimension by Chuzume](https://github.com/Chuzume/Private_Dimension) as a Fabric/NeoForge mod by 29kiyo.

## Supported Versions

| Minecraft | Fabric | NeoForge |
|-----------|--------|----------|
| 1.21.5 – 1.21.8 | ✅ | ✅ |
| 1.21.9 – 1.21.11 | ✅ | ✅ |
| 26.1.1 – 26.1.2 | ✅ | ✅ |

## Features

| Feature | Description |
|---------|-------------|
| 🌀 Dimension Travel | Right-click Dimension in a Bottle to enter your private dimension |
| 🔙 Return | Use the item again inside the dimension to return to your original location |
| 👥 Entity Escort | Sneak + use to bring friendly entities within 3 blocks along with you |
| 🏠 48×48 Plot | Each player is automatically assigned a dedicated 48×48 space |
| 🚫 Plot Boundary | Leaving the plot boundary forces you back to the overworld |
| ☠️ Death Handling | Dying inside the dimension respawns you in the overworld |

## Requirements

- **Fabric Loader** or **NeoForge** (see supported versions)
- **Java** 21+ (Java 25+ for 26.x versions)

## Installation

1. Download the `PrivateDimension-*.jar` for your version
2. Place it in your `.minecraft/mods/` folder
3. Launch the game

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/pd give [player]` | Give the item to a player | OP (level 2+) |
| `/pd info` | Show your plot information | Everyone |
| `/pd reload` | Reload the config | OP (level 2+) |

> Regular players can **craft Dimension in a Bottle and use it with right-click**. The only available command is `/pd info`.

## Configuration (config.json)

The config file is generated at `config/privatedimension/config.json`.

```json
{
  "plotSize": 48,
  "plotSpacing": 128,
  "plotFloorY": 64,
  "pullEntityLimit": 10,
  "pullEntityRadius": 3.0,
  "enableBorderEnforcement": true
}
```
