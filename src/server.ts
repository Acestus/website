import { createServer } from "node:http";
import path from "node:path";
import { handlePath } from "./site";

export type StartServerOptions = {
  port?: number;
  assetRoot?: string;
};

export function startServer(options: StartServerOptions = {}): ReturnType<typeof createServer> {
  const port = options.port ?? Number(process.env.PORT ?? "3000");
  const assetRoot =
    options.assetRoot ?? process.env.WEBSITE_ASSET_ROOT ?? path.join(process.cwd(), "resources");

  const server = createServer(async (req, res) => {
    try {
      const url = new URL(req.url ?? "/", `http://${req.headers.host ?? "localhost"}`);
      const result = await handlePath(url.pathname, assetRoot);

      res.writeHead(result.status, result.headers);
      res.end(result.body);
    } catch (error) {
      console.error("Request error:", error);
      res.writeHead(500, {
        "Content-Type": "text/plain; charset=utf-8",
        "Strict-Transport-Security": "max-age=31536000; includeSubDomains",
        "X-Content-Type-Options": "nosniff",
        "X-Frame-Options": "SAMEORIGIN",
      });
      res.end("error");
    }
  });

  server.listen(port, () => {
    console.log(`website listening on http://localhost:${port}`);
  });

  return server;
}

if (import.meta.main || process.argv[1]?.endsWith("server.js")) {
  startServer();
}
