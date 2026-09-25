# VelocityCommandBlocker

> Block dangerous commands for members across **all** backend servers on a Velocity proxy.
> **Created by Muvixo**

Runs on the Velocity proxy only - **no need to install anything on backend servers**.

## Features

- ✅ Velocity-only plugin (applies to every server behind the proxy)
- ✅ Full security blacklist (op, gamemode, give, ban, lp, stop, plugins, ...)
- ✅ Optional **whitelist mode**
- ✅ Namespaced command blocking (`/bukkit:op`, `/minecraft:give`, ...)
- ✅ Dangerous first-argument blocking (`/lp permission set ...`)
- ✅ **Tab-complete blocking** for hidden commands
- ✅ Fully customizable `config.yml` (colors with `&`, prefix, messages)
- ✅ `/cb creator` and `/cb reload`
- ✅ Bypass permission for staff
- ✅ Auto-build with **GitHub Actions**

## Default Prefix

```
MineStorm » 
```
(Aqua + White - can be changed in `config.yml`)

## Permissions (LuckPerms on Velocity)

| Permission | Description | Default |
|---|---|---|
| `commandblocker.bypass` | Skip blocking | false |
| `commandblocker.admin`  | `/cb reload` | op |

```
/lp user <player> permission set commandblocker.bypass true
/lp group admin   permission set commandblocker.bypass true
/lp group default permission set commandblocker.bypass false
```

## Commands

| Command | Permission | Description |
|---|---|---|
| `/cb creator` (aliases `/commandblocker`, `/vcb`, `/cmdblock`) | everyone | Shows plugin author |
| `/cb reload` | `commandblocker.admin` | Reload `config.yml` |

## Installation

1. Build locally with `mvn clean package` **or** download from GitHub Actions → Artifacts.
2. Put `VelocityCommandBlocker.jar` in the Velocity `plugins/` folder.
3. Restart the proxy.
4. Edit `plugins/velocitycommandblocker/config.yml`.
5. Reload with `/cb reload`.

## Building with GitHub Actions (no local setup)

1. Create a new empty repo on GitHub.
2. Push this project:
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/USERNAME/VelocityCommandBlocker.git
   git push -u origin main
   ```
3. Open the **Actions** tab → wait for the green check.
4. Download the JAR from **Artifacts** on the finished run.

## License

MIT
