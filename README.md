# VelocityCommandBlocker

> Block dangerous commands for members across **all** backend servers on a Velocity proxy.
> **Created by Muvixo**

Runs on the Velocity proxy only - **no need to install anything on backend servers**.

## Features

- ✅ Blacklist-only mode (no whitelist, no allowed-commands)
- ✅ Anything **not** blacklisted is allowed by default
- ✅ **Smart permission check**: if a player already has permission from
  another plugin (`essentials.vanish`, `minecraft.command.gamemode`,
  `bukkit.command.op`, ...), the command is **not** blocked
- ✅ Namespaced blocking (`/bukkit:op`, `/minecraft:give`)
- ✅ Dangerous first-argument blocking
- ✅ Tab-complete blocking
- ✅ Fully customizable `config.yml` (colors with `&`, prefix, messages)
- ✅ `/cb creator` and `/cb reload`
- ✅ Bypass permission for staff
- ✅ Auto-build with **GitHub Actions**

## Default Prefix

```
MineStorm »
```
(Aqua + White - change in `config.yml`)

## Permissions (LuckPerms on Velocity)

| Permission | Description | Default |
|---|---|---|
| `commandblocker.bypass` | Skip all blocking | false |
| `commandblocker.admin`  | `/cb reload` | op |

```
/lp user <player> permission set commandblocker.bypass true
/lp group admin   permission set commandblocker.bypass true
/lp group default permission set commandblocker.bypass false
```

**Note:** players who already have the command's own permission (from
Essentials, LuckPerms, etc.) are automatically allowed, e.g. a staff
member with `essentials.vanish` keeps `/vanish` even though `vanish` is
in the blacklist.

## Commands

| Command | Permission | Description |
|---|---|---|
| `/cb creator` | everyone | Shows plugin author |
| `/cb reload`  | `commandblocker.admin` | Reload `config.yml` |

Aliases: `/commandblocker`, `/vcb`, `/cmdblock`.

## Installation

1. Build with `mvn clean package` **or** download the JAR from GitHub Actions → Artifacts.
2. Put `VelocityCommandBlocker.jar` in the Velocity `plugins/` folder.
3. Restart the proxy.
4. Edit `plugins/velocitycommandblocker/config.yml`.
5. Reload with `/cb reload`.

## Building with GitHub Actions

```bash
git init
git add .
git commit -m "Initial commit by Muvixo"
git branch -M main
git remote add origin https://github.com/USERNAME/VelocityCommandBlocker.git
git push -u origin main
```

Then open the **Actions** tab → wait for green → download the JAR from **Artifacts**.

## License

MIT
