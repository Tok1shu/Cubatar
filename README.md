<div align="center">

# Cubatar

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
</div>

---

Cubatar turns Minecraft usernames, UUIDs, or raw skin URLs into beautiful, layered 3D avatars. Built with Java 21 to be fast, stable, and easy to integrate.

> **New:** full-body avatars (front & back view) via `/v1/body/{input}`, plus a `/v1/skin/{input}` endpoint to fetch the raw resolved skin texture — on top of the existing head icons.

## Why Cubatar?

- **Perfect Renders:** Full support for the second skin layer (hats, glasses, jackets).
- **Smart Inputs:** Just throw a Nickname, UUID, or Base64 at the API — it'll figure it out.
- **Blazing Fast:** Built-in Caffeine caching means zero Mojang rate-limits and instant responses.

## Gallery

<div align="center">
  <img src="examples/Notch.png" width="100" />
  <img src="examples/Jeb_.png" width="100" />
  <img src="examples/Phemida.png" width="100" />
  <img src="examples/Tok1shu.png" width="100" />
  <img src="examples/oVastix.png" width="100" />
  <img src="examples/Syn7esis.png" width="100" />
</div>

### Full body (front & back)

<div align="center">
  <img src="examples/Tok1shu_front.png" width="100" />
  <img src="examples/Syn7esis_front.png" width="100" />
  <img src="examples/oVastix_front.png" width="100" />
  <img src="examples/zzefirr_front.png" width="100" />
  <img src="examples/Sojahn09_front.png" width="100" />
  <br/>
  <img src="examples/Tok1shu_back.png" width="100" />
  <img src="examples/Syn7esis_back.png" width="100" />
  <img src="examples/oVastix_back.png" width="100" />
  <img src="examples/zzefirr_back.png" width="100" />
  <img src="examples/Sojahn09_back.png" width="100" />
</div>

## Quick Start

### API Usage

Just make a `GET` request:
```bash
# Head icon (size is optional, defaults to 64px)
curl "http://localhost:8080/v1/avatar/Notch?size=128"

# Full body, front or back view (size defaults to 128px)
curl "http://localhost:8080/v1/body/Notch?size=128&back=false"
curl "http://localhost:8080/v1/body/Notch?size=128&back=true"

# Raw skin texture, no processing (useful to inspect what was resolved)
curl "http://localhost:8080/v1/skin/Notch"

# Using a direct URL (or base64 encoded URL) — works on all three endpoints
curl "http://localhost:8080/v1/avatar/https%3A%2F%2Fexample.com%2Fskin.png"
```

Legacy 64x32 skins (pre-1.8, no second layer / no slim arms) are detected automatically and rendered without the jacket/sleeves overlay, same as in-game.

### Run Locally

```bash
docker build -t cubatar .
docker run -p 8080:8080 cubatar

```
