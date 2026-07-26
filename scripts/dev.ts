import { startServer } from "../src/server";

startServer({
  port: Number(process.env.PORT ?? "3000"),
  assetRoot: process.env.WEBSITE_ASSET_ROOT ?? "resources",
});
