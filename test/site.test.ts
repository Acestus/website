import { describe, expect, test } from "bun:test";
import { postsByDate } from "../src/data";
import { handlePath, notFoundPage, resumeDoc, resumePage } from "../src/site";

const assetRoot = "resources";

describe("website routes", () => {
  test("landing page renders the site name", async () => {
    const result = await handlePath("/", assetRoot);
    expect(result.status).toBe(200);
    expect(String(result.body)).toContain("acestus.com");
    expect(String(result.body)).toContain("Portfolio");
  });

  test("health route returns ok", async () => {
    const result = await handlePath("/health", assetRoot);
    expect(result.status).toBe(200);
    expect(result.body).toBe("ok");
  });

  test("blog index lists posts", async () => {
    const result = await handlePath("/blog", assetRoot);
    expect(result.status).toBe(200);
    expect(String(result.body)).toContain(`/blog/${postsByDate[0].slug}`);
  });

  test("blog post renders content", async () => {
    const slug = postsByDate[0].slug;
    const result = await handlePath(`/blog/${slug}`, assetRoot);
    expect(result.status).toBe(200);
    expect(String(result.body)).toContain("The Practitioner Is the Primary Source");
  });

  test("missing blog post returns 404", async () => {
    const result = await handlePath("/blog/no-such-post", assetRoot);
    expect(result.status).toBe(404);
    expect(String(result.body)).toContain("404");
  });

  test("resume pages include download links", async () => {
    const result = await handlePath("/resume", assetRoot);
    expect(result.status).toBe(200);
    expect(String(result.body)).toContain("/resume.doc");
    expect(String(result.body)).toContain("Download PDF");
  });

  test("resume doc renders downloadable html", async () => {
    const doc = resumeDoc("ai-platform");
    expect(doc).toContain("application");
    expect(doc).toContain("Senior AI Platform Engineer");
  });

  test("not found page includes 404", () => {
    expect(notFoundPage()).toContain("404");
  });
});

describe("resume variants", () => {
  test("sre resume has the right headline", () => {
    expect(resumePage("sre")).toContain("Site Reliability Engineer");
  });
});
