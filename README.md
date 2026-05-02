# acestus.com

Unified blog, contact, and resume site — Clojure + Azure Functions Flex Consumption.

## Routes

| Route | Description |
|-------|-------------|
| `GET /` | Landing page |
| `GET /blog` | Blog post listing |
| `GET /blog/:slug` | Individual blog post |
| `GET /contact` | Contact / linktree card |
| `GET /resume` | Resume |
| `GET /static/*` | Static assets |
| `GET /health` | Health check |

## Dev

```bash
# Build content from markdown
clojure -X:prep

# Run tests
clojure -M:test

# Build uberjar
clojure -T:build uber
```

## Deploy

Pushes to any branch except `main` deploy to Azure Functions Flex Consumption via GitHub Actions.
