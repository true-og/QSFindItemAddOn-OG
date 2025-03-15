# QSFindItemAddOn-OG
## Shop Search AddOn For QuickShop
### Version: 2.0.7.4.1

An unofficial add-on for the [QuickShop-OG](https://github.com/true-og/QuickShop-OG) paper plugin maintained for 1.19.4 by [TrueOG Network](https://true-og.net).

Adds a `/finditem` command in game for searching through all the shops on the server.

## Building
./mvnw clean package

.jar will be in target/

## Features
- Search items based on buying/selling shops
- Search items by item type
- Supports query based item search
- Supports item custom model data for custom items
- Configurable shop sorting methods
- Displays enchantments in the result for enchanted items
- Hides item enchants if item has hide_enchants flag
- Displays potion effects in the result for Potion items
- Hide certain shops from appearing in search lists
- Supports completely safe direct shop teleportation (configurable in config.yml)
- Ignores shops that are out of stock by default
- Support for world blacklisting (Shops in blacklisted worlds are ignored in the search result)
- Support for material blacklisting that prevents players for searching for specific items

## Changes over QSFindItemAddOn
- Downgraded plugin to support 1.19.4.
- Removed PlayerWarps and BentoBox support.
- Switch to maven wrapper.
- Fix build.

**Hexcode color support**
- All messages in the config.yml support hexcodes.

**View all shops on server**
- You can do `/finditem TO_BUY *` or `/finditem TO_SELL *` to view all shops on the server. As of this version, the sequence is always randomized. Sorting options are a work in progress.

**Shop visit count**
- You can choose to display shop visits count in the shop lore in Search GUI. Just add the placeholder `{SHOP_VISITS}` in the `shop-gui-item-lore` in config.yml. To prevent visit spamming, a new config property `shop-player-visit-cooldown-in-minutes` has been added. Please don't use decimals here. 😁

**Customizable command aliases**
- You can find a property in config.yml called `command-alias` where you can specify your own list of command aliases for /finditem command. If you don't wish to add any, just make it as:
```yaml
command-alias: []
```

## Integrations
- Supports EssentialsX Warps integration for fetching nearest warps.
  - Global warps list for essentials is updated in batches every 15 minutes due to technical limitations, which is then used in every search query.
  - If you added a new warp and want it to get updated immediately, run **/finditemadmin reload**
  - Remember, this applies only to Essential Warps.
- WorldGuard region support for fetching the WorldGuard region the shop is in (if overlapping regions, highest priority will be chosen)
- Residence support for fetching the residence the shop is in (including subzones)

>Check out the sample config.yml [here](https://github.com/myzticbean/QSFindItemAddOn/wiki/Sample-config.yml).

## Assumptions
- A compatible economy plugin is installed.
- [Vault](https://github.com/true-og/Vault-OG) is installed.

### How to use `/finditem`?
![/finditem_usage](https://cdn.modrinth.com/data/asp13ugE/images/bb37966809c9d7ab3201988ef58b2060688584f3.png)
![alt text](https://cdn.modrinth.com/data/asp13ugE/images/878e9b703343a65c963d790d875ad5dbe6ac309d.png)
### Multiple search result pages:
![alt text](https://cdn.modrinth.com/data/asp13ugE/images/33cb7d96cabb709bc630685c9e6fdc1b9cd7b3bb.png)
### Shows item enchantments:
![alt text](https://cdn.modrinth.com/data/asp13ugE/images/8ac5643bc042b897e549400e29186d87024b3a71.png)
### Shows Potion colors and effects:
![alt text](https://cdn.modrinth.com/data/asp13ugE/images/786ce10d42c5e92cbbd12b7f1ee81011796acbe0.png)
### Shows custom item names and lore:
![alt text](https://cdn.modrinth.com/data/asp13ugE/images/0c30b767bfc9df1f4a79afef677c0fc262fa62c5.png)
