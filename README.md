<div align="center">

# Cubatar

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

[Live Demo](http://87.251.76.217:8080/v1/avatar/Phemida?size=500)
</div>

---

Cubatar turns Minecraft usernames, UUIDs, or Base64 skins into beautiful, layered 3D avatars. It's built on Java 21 to be simple, fast, and completely hassle-free.

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

## Quick Start

### API Usage

Just make a `GET` request:
```bash
# size is optional (defaults to 64px)
curl "http://localhost:8080/v1/avatar/Notch?size=128"
```

### Run Locally

```bash
docker build -t cubatar .
docker run -p 8080:8080 cubatar

```
