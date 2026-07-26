import { cp, mkdir, rm, writeFile } from "node:fs/promises";
import path from "node:path";

const root = process.cwd();
const dist = path.join(root, "dist");

async function copyDir(source: string, destination: string): Promise<void> {
  await cp(source, destination, { recursive: true });
}

async function main(): Promise<void> {
  await rm(dist, { recursive: true, force: true });
  await mkdir(dist, { recursive: true });

  const serverBuild = await Bun.build({
    entrypoints: [path.join(root, "src/server.ts")],
    outdir: dist,
    target: "node",
    splitting: false,
    minify: false,
  });

  if (!serverBuild.success) {
    for (const log of serverBuild.logs) {
      console.error(log.message);
    }
    process.exitCode = 1;
    return;
  }

  const clientOut = path.join(dist, "resources", "static", "js");
  await mkdir(clientOut, { recursive: true });

  const clientBuild = await Bun.build({
    entrypoints: [path.join(root, "src/client/site.ts")],
    outdir: clientOut,
    target: "browser",
    splitting: false,
    minify: true,
  });

  if (!clientBuild.success) {
    for (const log of clientBuild.logs) {
      console.error(log.message);
    }
    process.exitCode = 1;
    return;
  }

  await copyDir(path.join(root, "resources/static"), path.join(dist, "resources/static"));
  await copyDir(path.join(root, "resources/content"), path.join(dist, "resources/content"));

  await writeFile(path.join(dist, "package.json"), await Bun.file(path.join(root, "package.json")).text());

  console.log("Build complete:", dist);
}

await main();
